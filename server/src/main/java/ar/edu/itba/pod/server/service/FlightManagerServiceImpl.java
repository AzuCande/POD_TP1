package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.models.*;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.FlightNotFoundException;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.TicketNotFoundException;
import ar.edu.itba.pod.interfaces.FlightManagerService;
import ar.edu.itba.pod.models.exceptions.planeExceptions.IllegalPlaneStateException;
import ar.edu.itba.pod.models.exceptions.planeExceptions.ModelAlreadyExistsException;
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
            // TODO: preguntar si es mucho tiempo porque la alternativa seria lockear 2 veces
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

        // TODO hace falta que esten anidados?
        synchronized (store.getFlightCodes()) {
            synchronized (store.getPendingFlights()) {
                if (store.getFlightCodes().containsKey(flightCode))
                    throw new RuntimeException();

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
                    throw new IllegalPlaneStateException();

                flight = store.getPendingFlights().remove(flightCode);
                flight.setState(state); // TODO check si no se rompe nada
            }
            Map<String, Flight> flights = store.getFlightsByState(state);
            synchronized (flights) {
                store.getFlightCodes().put(flightCode, state);
                flights.put(flightCode, flight);
            }
        }

        // TODO: modularizar notis
        Map<String, List<NotificationHandler>> flightNotifications;
        store.getNotificationsLock().lock();
        flightNotifications = store.getNotifications().get(flightCode);
        store.getNotificationsLock().unlock();

        if (flightNotifications == null)
            return;

        synchronized (flightNotifications) { // TODO: preguntar si es excesivo
            flightNotifications.forEach((passenger, handlers) -> {
                synchronized (handlers) {
                    Ticket ticket = flight.getTickets().values().stream()
                            .filter(t -> t.getPassenger().equals(passenger)).findFirst()
                            .orElseThrow(TicketNotFoundException::new); // TODO check

                    for (NotificationHandler handler : handlers) {
                        store.submitNotificationTask(() -> {
                            try {
                                switch (flight.getState()) {
                                    case CONFIRMED:
                                        handler.notifyConfirmFlight(flightCode, flight.getDestination(),
                                                state, ticket.getCategory(), ticket.getRow(), ticket.getCol());
                                        break;
                                    case CANCELED:
                                        handler.notifyCancelFlight(flightCode, flight.getDestination(),
                                                state, ticket.getCategory(), ticket.getRow(), ticket.getCol());
                                        break;
                                }
                            } catch (RemoteException e) {
                                throw new RuntimeException(e); // TODO: excepcion propia
                            }
                        });
                    }
                }
            });
        }
    }


    @Override
    public void changeCancelledFlights() throws RemoteException { // TODO: modularizar
        Collection<Flight> cancelledFlights;
        synchronized (store.getCancelledFlights()) {
            cancelledFlights = store.getCancelledFlights().values();
        }

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
                    }; // TODO: REFACTOR

                    Flight newFlight;

                    synchronized (store.getPendingFlights()) {
                        newFlight = store.getPendingFlights().values().stream().filter(flight ->
                                        flight.getDestination().equals(ticket.getDestination()) &&
                                                flight.getAvailableByCategory(ticket.getCategory()) != -1)
                                .min(comparator)
                                .orElse(null);

                        if (newFlight == null) {
                            logger.info("No alternative for ticket blah"); // TODO: ver bien que se hace con esto
                            continue;
                        }

                        // PRIMERO ESTADO, DESPUÉS ASIENTO
                        newFlight.getStateLock().lock();
                        newFlight.getSeatsLock().lock();

                    }

                    cancelled.changeFlight(ticket.getPassenger(), newFlight); // TODO ver si puede fallar

                    newFlight.getSeatsLock().unlock();
                    newFlight.getStateLock().unlock();

                    // TODO notificaciones
                    // NOTIFICACIONES
                    Map<String, List<NotificationHandler>> flightNotifications;
                    store.getNotificationsLock().lock();
                    flightNotifications = store.getNotifications().get(cancelled.getCode());
                    store.getNotificationsLock().unlock();

                    if (flightNotifications == null)
                        continue;

                    synchronized (flightNotifications) {
                        flightNotifications.forEach((passenger, handlers) -> {
                            synchronized (handlers) {
                                for (NotificationHandler handler : handlers) {
                                    store.submitNotificationTask(() -> {
                                        try {
                                            handler.notifyChangeTicket(cancelled.getCode(), cancelled.getDestination(), newFlight.getCode());
                                        } catch (RemoteException e) {
                                            throw new RuntimeException(e); // TODO: excepcion propia
                                        }
                                    });
                                }
                            }
                        });
                    }

                }
            } finally {
                cancelled.getSeatsLock().unlock();
            }
        }
    }
}
