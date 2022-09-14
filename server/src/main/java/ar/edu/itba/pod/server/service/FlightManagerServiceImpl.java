package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.models.*;
import ar.edu.itba.pod.models.exceptions.flightExceptions.FlightAlreadyExistsException;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.FlightNotFoundException;
import ar.edu.itba.pod.interfaces.FlightManagerService;
import ar.edu.itba.pod.models.exceptions.flightExceptions.IllegalFlightStateException;
import ar.edu.itba.pod.models.exceptions.flightExceptions.ModelAlreadyExistsException;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.ModelNotFoundException;
import ar.edu.itba.pod.server.utils.FlightComparator;
import ar.edu.itba.pod.server.utils.ServerStore;
import ar.edu.itba.pod.server.models.Flight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class FlightManagerServiceImpl implements FlightManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlightManagerServiceImpl.class);
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
        LOGGER.info("Added plane model: " + model);
    }

    @Override
    public void addFlight(String planeModel, String flightCode, String destination, List<Ticket> tickets) throws RemoteException {
        PlaneModel model;
        modelsLock.lock();
        try {
            model = Optional.ofNullable(store.getPlaneModels().get(planeModel))
                    .orElseThrow(ModelNotFoundException::new);
        } finally {
            modelsLock.unlock();
        }

        synchronized (store.getFlightCodes()) {
            if (store.getFlightCodes().containsKey(flightCode))
                throw new FlightAlreadyExistsException();

            synchronized (store.getPendingFlights()) {
                store.getPendingFlights().put(flightCode, new Flight(model, flightCode,
                        destination, tickets));
                store.getFlightCodes().put(flightCode, FlightState.PENDING);
            }
        }
        LOGGER.info("Added flight " + flightCode + " with model " + model);
    }

    @Override
    public FlightState getFlightState(String flightCode) throws RemoteException {
        synchronized (store.getFlightCodes()) {
            return Optional.ofNullable(store.getFlightCodes().get(flightCode))
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
        LOGGER.info("Flight " + flightCode + " state changed to " + state);

        Map<String, List<NotificationHandler>> flightNotifications = store
                .getFlightNotifications(flightCode);
        if (flightNotifications == null)
            return;

        synchronized (flightNotifications) { // Too specific to modularize
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
                                        store.removeFlightNotifications(flightCode);
                                        break;
                                    case CANCELED:
                                        handler.notifyCancelFlight(notification);
                                        break;
                                }
                            } catch (RemoteException e) {
                                LOGGER.info("Could not send notification", e);
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public ResponseCancelledList changeCancelledFlights() throws RemoteException {
        Collection<Flight> cancelledFlights;
        synchronized (store.getCancelledFlights()) {
            cancelledFlights = store.getCancelledFlights().values();
        }
        ArrayList<CancelledTicket> unchangedTickets = new ArrayList<>();
        int changedCounter = 0;

        cancelledFlights = cancelledFlights.stream()
                .sorted(Comparator.comparing(Flight::getCode)).collect(Collectors.toList());

        Map<String, Notification> notificationsToSend = new HashMap<>();

        for (Flight cancelled : cancelledFlights) {
            LOGGER.info("Reticketing for flight " + cancelled.getCode());
            changedCounter += reticketCancelledFlight(cancelled, unchangedTickets, notificationsToSend);
        }

        notificationsToSend.forEach(store::changeTicketsNotification);
        return new ResponseCancelledList(changedCounter, unchangedTickets);
    }

    private int reticketCancelledFlight(Flight cancelled, List<CancelledTicket> unchangedTickets, Map<String, Notification> notificationsToSend) {
        int toReturn = 0;
        cancelled.getSeatsLock().lock();
        try {
            List<Ticket> tickets = cancelled.getTickets().values().stream()
                    .sorted(Comparator.naturalOrder()).collect(Collectors.toList());

            for (Ticket ticket : tickets) {
                Comparator<Flight> comparator = new FlightComparator(ticket);
                Flight newFlight;

                synchronized (store.getPendingFlights()) {
                    newFlight = store.getPendingFlights().values().stream().filter(flight ->
                                    flight.getDestination().equals(ticket.getDestination()) &&
                                            flight.getAvailableByCategory(ticket.getCategory()) != -1)
                            .min(comparator)
                            .orElse(null);

                    if (newFlight == null) {
                        unchangedTickets.add(new CancelledTicket(cancelled.getCode(),
                                ticket.getPassenger()));
                        continue;
                    }
                    newFlight.getStateLock().lock();
                    newFlight.getSeatsLock().lock();
                }
                cancelled.changeFlight(ticket.getPassenger(), newFlight);
                toReturn = 1;

                newFlight.getSeatsLock().unlock();
                newFlight.getStateLock().unlock();

                notificationsToSend.put(ticket.getPassenger(),
                        new Notification(cancelled.getCode(), cancelled.getDestination(),
                                newFlight.getCode()));
            }
        } finally {
            cancelled.getSeatsLock().unlock();
        }

        return toReturn;
    }
}