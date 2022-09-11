package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.interfaces.SeatManagerService;
import ar.edu.itba.pod.models.*;
import ar.edu.itba.pod.models.FlightResponse;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.FlightNotFoundException;
import ar.edu.itba.pod.models.exceptions.planeExceptions.IllegalPlaneStateException;
import ar.edu.itba.pod.server.ServerStore;
import ar.edu.itba.pod.server.models.Flight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class SeatManagerServiceImpl implements SeatManagerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeatManagerServiceImpl.class);
    private final ServerStore store;
//    private final Lock seatLock = new ReentrantLock();

    public SeatManagerServiceImpl(ServerStore store) {
        this.store = store;
    }

    @Override
    public boolean isAvailable(String flightCode, int row, char seat) throws RemoteException {
        Flight flight = validateFlightCode(flightCode);

        flight.getStateLock().lock(); // TODO poner el informe que se podria ser mas granular y solo lockear por row
        // IGUAL no se podria ya que podrian cambiar el estado
        try {
            return flight.checkSeat(row, seat);
        } finally {
            flight.getStateLock().unlock();
        }
    }

    @Override
    public void assign(String flightCode, String passenger, int row, char seat) throws RemoteException {
        // TODO: chequear
        Flight flight;// = validateFlightCode(flightCode);

        synchronized (store.getPendingFlights()) {
            flight = Optional.ofNullable(store.getPendingFlights().get(flightCode))
                    .orElseThrow(IllegalPlaneStateException::new);
            flight.getStateLock().lock();
        }

        flight.getSeatsLock().lock();
        try {
            flight.assignSeat(row, seat, passenger);
        } finally {
            flight.getSeatsLock().unlock();
            flight.getStateLock().unlock();
        }

        syncNotify(flightCode, passenger, handler -> {
            try {
                Ticket t = flight.getTicket(passenger);
                handler.notifyAssignSeat(flightCode, flight.getDestination(), t.getCategory(), row, seat);
            } catch (RemoteException e) {
                LOGGER.info(e.getMessage());
            }
        });
    }

    @Override
    public void changeSeat(String flightCode, String passenger, int freeRow, char freeSeat) throws RemoteException {
        Flight flight;// = validateFlightCode(flightCode);

        synchronized (store.getPendingFlights()) {
            flight = Optional.ofNullable(store.getPendingFlights().get(flightCode))
                    .orElseThrow(IllegalPlaneStateException::new);
            flight.getStateLock().lock();
        }

        flight.getSeatsLock().lock();
        try {
            flight.changeSeat(freeRow, freeSeat, passenger);
        } finally {
            flight.getSeatsLock().unlock();
            flight.getStateLock().unlock();
        }

        syncNotify(flightCode, passenger, handler -> {
            try {
                Ticket ticket = flight.getTicket(passenger);
                handler.notifyChangeSeat(flightCode, flight.getDestination(),
                        ticket.getCategory(), ticket.getRow(), ticket.getCol(), RowCategory.ECONOMY,
                        freeRow, freeSeat);
            } catch (RemoteException e) {
                e.printStackTrace();
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

// JFK | AA101 | 7 BUSINESS
// JFK | AA119 | 3 BUSINESS
// JFK | AA103 | 18 PREMIUM_ECONOMY
    @Override
    public List<FlightResponse> listAlternativeFlights(String flightCode, String passenger) throws RemoteException { //TODO : return string ?
        //Flight flight = store.getFlight(flightCode);//;validateFlightCode(flightCode);

        Flight flight;
        String destination;
        RowCategory category;

        synchronized (store.getFlightCodes()) {
            FlightState state = store.getFlightCodes().get(flightCode);
            if (state.equals(FlightState.CONFIRMED))
                throw new IllegalPlaneStateException();

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
        } // TODO: ver de lockear el estado de cada uno

        List<FlightResponse> toReturn = new ArrayList<>();

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
                toReturn.add(new FlightResponse(alternative.getCode(), destination, availableSeats));
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
                throw new IllegalPlaneStateException();

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
                        .orElseThrow(IllegalPlaneStateException::new);
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

        // TODO notis
    }

    private void syncNotify(String flightCode, String passenger, Consumer<NotificationHandler> handlerConsumer) {
        Map<String, List<NotificationHandler>> flightNotifications;

        store.getNotificationsLock().lock();
        flightNotifications = store.getNotifications().get(flightCode);
        store.getNotificationsLock().unlock();

        if (flightNotifications == null)
            return;

        List<NotificationHandler> handlers;
        synchronized (flightNotifications) {
            handlers = flightNotifications.get(passenger);
        }

        if (handlers == null)
            return;

        synchronized (handlers) {
            for (NotificationHandler handler : handlers) {
                store.submitNotificationTask(() -> handlerConsumer.accept(handler));
                handlerConsumer.accept(handler);
            }
        }
    }
}
