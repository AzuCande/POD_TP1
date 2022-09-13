package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.interfaces.SeatManagerService;
import ar.edu.itba.pod.models.*;
import ar.edu.itba.pod.models.AlternativeFlightResponse;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.FlightNotFoundException;
import ar.edu.itba.pod.models.exceptions.flightExceptions.IllegalFlightStateException;
import ar.edu.itba.pod.server.utils.ServerStore;
import ar.edu.itba.pod.server.models.Flight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class SeatManagerServiceImpl implements SeatManagerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeatManagerServiceImpl.class);
    private final ServerStore store;

    public SeatManagerServiceImpl(ServerStore store) {
        this.store = store;
    }

    @Override
    public boolean isAvailable(String flightCode, int row, char seat) throws RemoteException {
        Flight flight = validateFlightCode(flightCode);

        flight.getStateLock().lock();
        try {
            return flight.checkSeat(row, seat);
        } finally {
            flight.getStateLock().unlock();
        }
    }

    @Override
    public void assign(String flightCode, String passenger, int row, char seat) throws RemoteException {
        Flight flight;
        synchronized (store.getPendingFlights()) {
            flight = Optional.ofNullable(store.getPendingFlights().get(flightCode))
                    .orElseThrow(IllegalFlightStateException::new);
            flight.getStateLock().lock();
        }

        flight.getSeatsLock().lock();
        try {
            flight.assignSeat(row, seat, passenger);
        } finally {
            flight.getSeatsLock().unlock();
            flight.getStateLock().unlock();
        }

        LOGGER.info("Assigned seat" + row + seat + " to passenger " + passenger + " on flight " +
                flightCode);

        syncNotify(flightCode, passenger, handler ->
                store.submitNotificationTask(() -> {
                    try {
                        handler.notifyAssignSeat(new Notification(flightCode,
                                flight.getDestination(), flight.getRows()[row].getRowCategory(),
                                row, seat));
                    } catch (RemoteException e) {
                        LOGGER.info("Could not notify");
                    }
                }));
    }

    @Override
    public void changeSeat(String flightCode, String passenger, int freeRow, char freeSeat) throws RemoteException {
        Flight flight;
        synchronized (store.getPendingFlights()) {
            flight = Optional.ofNullable(store.getPendingFlights().get(flightCode))
                    .orElseThrow(IllegalFlightStateException::new);
            flight.getStateLock().lock();
        }

        Integer row;
        Character col;

        flight.getSeatsLock().lock();
        try {
            Ticket ticket = flight.getTicket(passenger);
            row = ticket.getRow();
            col = ticket.getCol();
            flight.changeSeat(freeRow, freeSeat, passenger);
        } finally {
            flight.getSeatsLock().unlock();
            flight.getStateLock().unlock();
        }

        LOGGER.info("Changed " + passenger + " seat from " + row + col + " to " + freeRow +
                freeSeat + " on flight " + flightCode);

        syncNotify(flightCode, passenger, handler -> {
            try {
                RowCategory category = null;
                if (row != null)
                    category = flight.getRows()[row].getRowCategory();

                handler.notifyChangeSeat(new Notification(flightCode, flight.getDestination(),
                        category, row, col,
                        flight.getRows()[freeRow].getRowCategory(),
                        freeRow, freeSeat));
            } catch (RemoteException e) {
                LOGGER.info(e.getMessage());
            }
        });
    }

    private Flight validateFlightCode(String flightCode) {
        synchronized (store.getFlightCodes()) {
            FlightState state = Optional.ofNullable(store.getFlightCodes().get(flightCode))
                    .orElseThrow(FlightNotFoundException::new);

            synchronized (store.getFlightsByState(state)) {
                return Optional.ofNullable(store.getFlightsByState(state).get(flightCode))
                        .orElseThrow(FlightNotFoundException::new);
            }
        }
    }

    @Override
    public List<AlternativeFlightResponse> listAlternativeFlights(String flightCode, String passenger) throws RemoteException {
        Flight flight;
        String destination;
        RowCategory category;

        synchronized (store.getFlightCodes()) {
            FlightState state = store.getFlightCodes().get(flightCode);
            if (state.equals(FlightState.CONFIRMED))
                throw new IllegalFlightStateException();

            Map<String, Flight> flights = store.getFlightsByState(state);
            synchronized (flights) {
                flight = flights.get(flightCode);
                flight.getSeatsLock().lock();
            }
        }

        category = flight.getTicket(passenger).getCategory();
        flight.getSeatsLock().unlock();

        destination = flight.getDestination();

        List<Flight> alternativeFlights;

        synchronized (store.getPendingFlights()) {
            alternativeFlights = store.getPendingFlights().values().stream()
                    .filter(f -> f.getDestination().equals(destination)).collect(Collectors.toList());
        }

        List<AlternativeFlightResponse> toReturn = new ArrayList<>();

        alternativeFlights.forEach(alternative -> {
            alternative.getSeatsLock().lock();
            Map<RowCategory, Integer> availableSeats = new HashMap<>();
            for (int i = category.ordinal(); i >= 0; i--) {
                int available = alternative.getAvailableByCategory(RowCategory.values()[i]);
                if (i > 0) {
                    availableSeats.put(RowCategory.values()[i], available);
                }
            }
            alternative.getSeatsLock().unlock();

            if (availableSeats.keySet().size() > 0)
                toReturn.add(new AlternativeFlightResponse(alternative.getCode(), destination, availableSeats));
        });

        return toReturn;
    }

    @Override
    public void changeFlight(String passenger, String oldFlightCode, String newFlightCode) throws RemoteException {
        Flight oldFlight;
        Flight newFlight = null;
        synchronized (store.getFlightCodes()) {
            FlightState state = store.getFlightCodes().get(oldFlightCode);
            if (state.equals(FlightState.CONFIRMED))
                throw new IllegalFlightStateException();

            Map<String, Flight> flights = store.getFlightsByState(state);
            synchronized (flights) {
                oldFlight = flights.get(oldFlightCode);
                oldFlight.getStateLock().lock();
                oldFlight.getSeatsLock().lock();
            }
        }

        try {
            synchronized (store.getPendingFlights()) {
                newFlight = Optional.ofNullable(store.getPendingFlights().get(newFlightCode))
                        .orElseThrow(IllegalFlightStateException::new);
                newFlight.getStateLock().lock();
                newFlight.getSeatsLock().lock();
            }

            oldFlight.changeFlight(passenger, newFlight);
        } finally {
            if (newFlight != null) {
                newFlight.getSeatsLock().unlock();
                newFlight.getStateLock().unlock();
            }
            oldFlight.getSeatsLock().unlock();
            oldFlight.getStateLock().unlock();
        }

        Notification notification = new Notification(oldFlightCode, oldFlight.getDestination(),
                newFlightCode);

        List<NotificationHandler> notificationHandlers = store.changeFlightNotifications(
                notification, passenger);

        synchronized (notificationHandlers) {
            notificationHandlers.forEach(handler -> {
                store.submitNotificationTask(() -> {
                    try {
                        handler.notifyChangeTicket(notification);
                    } catch (RemoteException e) {
                        LOGGER.info("Could not notify");
                    }
                });
            });
        }

        store.registerUser(new Notification(notification.getNewCode(),
                notification.getDestination()), passenger, notificationHandlers);
    }

    private void syncNotify(String flightCode, String passenger, Consumer<NotificationHandler> handlerConsumer) {
//        Map<String, List<NotificationHandler>> flightNotifications = store
//                .getFlightNotifications(flightCode);
//
//        if (flightNotifications == null)
//            return;
//
//        List<NotificationHandler> handlers;
//        synchronized (flightNotifications) {
//            handlers = flightNotifications.get(passenger);
//        }
//
//        if (handlers == null)
//            return;
        List<NotificationHandler> handlers = store.getHandlers(flightCode, passenger);

        synchronized (handlers) {
            for (NotificationHandler handler : handlers) {
                store.submitNotificationTask(() -> handlerConsumer.accept(handler));
            }
        }
    }
}
