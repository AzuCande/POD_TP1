package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.models.*;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.FlightNotFoundException;
import ar.edu.itba.pod.interfaces.FlightManagerService;
import ar.edu.itba.pod.models.exceptions.flightExceptions.IllegalFlightStateException;
import ar.edu.itba.pod.models.exceptions.flightExceptions.ModelAlreadyExistsException;
import ar.edu.itba.pod.server.ServerStore;
import ar.edu.itba.pod.server.models.Flight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class FlightManagerServiceImpl implements FlightManagerService {

    private static final Logger logger = LoggerFactory.getLogger(FlightManagerServiceImpl.class);
    private final ServerStore store;

    final Lock modelsLock = new ReentrantLock();

    public FlightManagerServiceImpl(ServerStore store) {
        this.store = store;
    }

    @Override
    public void addPlaneModel(String model, Map<String, int[]> seatCategories) {
        modelsLock.lock();
        try {
            if (store.getPlaneModels().containsKey(model))
                throw new ModelAlreadyExistsException(model);

            store.getPlaneModels().put(model, new PlaneModel(model, seatCategories));
        } finally {
            modelsLock.unlock();
        }
        logger.info("Added model " + model);
    }

    @Override
    public void addFlight(String planeModel, String flightCode, String destination, List<Ticket> tickets) throws RemoteException {
        PlaneModel model;
        modelsLock.lock();
        try {
            model = Optional.of(store.getPlaneModels().get(planeModel))
                    .orElseThrow(RuntimeException::new);
        } finally {
            modelsLock.unlock();
        }

        synchronized (store.getFlightCodes()) {
            if (store.getFlightCodes().containsKey(flightCode))
                throw new RuntimeException();

            synchronized (store.getPendingFlights()) {
                store.getPendingFlights().put(flightCode, new Flight(model, flightCode,
                        destination, tickets));
                store.getFlightCodes().put(flightCode, FlightState.PENDING);
            }
        }
    }

    @Override
    public FlightState getFlightState(String flightCode) throws RemoteException {
        synchronized (store.getFlightCodes()) {
            return Optional.of(store.getFlightCodes().get(flightCode))
                    .orElseThrow(FlightNotFoundException::new);
        }
    }

    @Override
    public void confirmFlight(String flightCode) throws RemoteException {
        changeFlightState(flightCode, FlightState.CONFIRMED);
    }

    @Override
    public void cancelFlight(String flightCode) throws RemoteException {
        changeFlightState(flightCode, FlightState.CANCELED);
    }

    private void changeFlightState(String flightCode, FlightState state) {
        Flight flight;
        synchronized (store.getFlightCodes()) {
            synchronized (store.getPendingFlights()) {
                if (!store.getPendingFlights().containsKey(flightCode))
                    throw new IllegalFlightStateException();

                flight = store.getPendingFlights().remove(flightCode);

                flight.getStateLock().lock();
            }

            Map<String, Flight> flights = store.getFlightsByState(state);
            synchronized (flights) {
                store.getFlightCodes().put(flightCode, state);
                flights.put(flightCode, flight);
            }
        }

        flight.setState(state);
        flight.getStateLock().unlock();

        // TODO: modularizar notis y mandar todo esto a un thread aparte
        Map<String, List<NotificationHandler>> flightNotifications;
        store.getNotificationsLock().lock();
        flightNotifications = store.getNotifications().get(flightCode);
        store.getNotificationsLock().unlock();

        if (flightNotifications == null)
            return;

        synchronized (flightNotifications) { // TODO: preguntar si es excesivo
            flightNotifications.forEach((passenger, handlers) -> {
                synchronized (handlers) {
                    flight.getSeatsLock().lock();
                    Ticket ticket = flight.getTickets().get(passenger);
                    Integer row = ticket.getRow();
                    Character col = ticket.getCol();
                    flight.getSeatsLock().unlock();

                    for (NotificationHandler handler : handlers) {
                        store.submitNotificationTask(() -> {
                            try {
                                RowCategory category = null;
                                if (ticket.isSeated()) {
                                    category = flight.getRows()[row].getRowCategory();
                                }
                                Notification notification = new Notification(flightCode,
                                        flight.getDestination(), category, row, col);
                                switch (state) {
                                    case CONFIRMED:
                                        handler.notifyConfirmFlight(notification);
                                        break;
                                    case CANCELED:
                                        handler.notifyCancelFlight(notification);
                                        break;
                                }
                            } catch (RemoteException e) {
                                logger.info("Could not send notification");
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public ResponseCancelledList changeCancelledFlights() throws RemoteException { // TODO: modularizar
        Collection<Flight> cancelledFlights;
        synchronized (store.getCancelledFlights()) {
            cancelledFlights = store.getCancelledFlights().values();
        }

        ArrayList<CancelledTicket> unchangedTickets = new ArrayList<>();
        int changedCounter = 0;

        cancelledFlights = cancelledFlights.stream()
                .sorted(Comparator.comparing(Flight::getCode)).collect(Collectors.toList());

        for (Flight cancelled : cancelledFlights) {
            cancelled.getSeatsLock().lock();
            try {
                List<Ticket> tickets = cancelled.getTickets().values().stream()
                        .sorted(Comparator.naturalOrder()).collect(Collectors.toList());

                // TODO en el informe: seat lock es obligatorio porque pueden cambiar tickets individuales
                for (Ticket ticket : tickets) {
                    Comparator<Flight> comparator = (flight1, flight2) -> {
                        int cat1 = flight1.getAvailableCategory(ticket.getCategory());
                        int cat2 = flight2.getAvailableCategory(ticket.getCategory());
                        if (cat1 != cat2)
                            return cat2 - cat1;

                        RowCategory category = RowCategory.values()[cat1];

                        return flight2.getAvailableByCategory(category) - flight1.getAvailableByCategory(category);
                    }; // TODO: REFACTOR clase comparator

                    Flight newFlight;

                    synchronized (store.getPendingFlights()) {
                        newFlight = store.getPendingFlights().values().stream().filter(flight ->
                                        flight.getDestination().equals(ticket.getDestination()) &&
                                                flight.getAvailableByCategory(ticket.getCategory()) != -1)
                                .min(comparator)
                                .orElse(null);

                        if (newFlight == null) {
                            unchangedTickets.add(new CancelledTicket(cancelled.getCode(), ticket.getPassenger()));
                            continue;
                        }

                        // PRIMERO ESTADO, DESPUÉS ASIENTO
                        newFlight.getStateLock().lock();
                        newFlight.getSeatsLock().lock();

                    }
                    cancelled.changeFlight(ticket.getPassenger(), newFlight);
                    changedCounter++;

                    newFlight.getSeatsLock().unlock();
                    newFlight.getStateLock().unlock();

                    // TODO: Modular NOTIFICACIONES
                    store.getNotificationsLock().lock();

                    Map<String, List<NotificationHandler>> cancelledNotifications = store
                            .getNotifications().get(cancelled.getCode());

                    if (cancelledNotifications == null) {
                        store.getNotificationsLock().unlock();
                        continue;
                    }

                    Map<String, List<NotificationHandler>> newNotifications = store.getNotifications()
                            .computeIfAbsent(newFlight.getCode(), k -> new HashMap<>());

                    store.getNotificationsLock().unlock();

                    List<NotificationHandler> notificationHandlers;

                    synchronized (cancelledNotifications) {
                        notificationHandlers = cancelledNotifications.remove(ticket.getPassenger());
                    }

                    if (notificationHandlers == null)
                        continue;

                    synchronized (newNotifications) {
                        newNotifications.put(ticket.getPassenger(), notificationHandlers);
                    }

                    synchronized (notificationHandlers) {
                        notificationHandlers.forEach(handler -> {
                            store.submitNotificationTask(() -> {
                                try {
                                    handler.notifyChangeTicket(new Notification(cancelled.getCode(),
                                            cancelled.getDestination(), newFlight.getCode()));
                                } catch (RemoteException e) {
                                    logger.info("Could not notify");
                                }
                            });
                        });
                    }
                }
            } finally {
                cancelled.getSeatsLock().unlock();
            }
        }
        return new ResponseCancelledList(changedCounter, unchangedTickets);
    }
}
