package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.interfaces.SeatManagerService;
import ar.edu.itba.pod.models.*;
import ar.edu.itba.pod.models.FlightResponse;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.TicketNotFoundException;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.FlightNotFoundException;
import ar.edu.itba.pod.models.exceptions.planeExceptions.IllegalPlaneStateException;
import ar.edu.itba.pod.server.ServerStore;
import ar.edu.itba.pod.server.models.Flight;

import java.rmi.RemoteException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class SeatManagerServiceImpl implements SeatManagerService {
    private final ServerStore store;
//    private final Lock seatLock = new ReentrantLock();

    public SeatManagerServiceImpl(ServerStore store) {
        this.store = store;
    }

    @Override
    public boolean isAvailable(String flightCode, int row, char seat) throws RemoteException {
        Flight flight = validateFlightCode(flightCode);

        return flight.checkSeat(row, seat);
    }

    @Override
    public void assign(String flightCode, String passenger, int row, char seat) throws RemoteException {
        // TODO: chequear
        //Flight flight = getValidatedFlight(flightCode, passenger, row, seat);
        Flight flight = validateFlightCode(flightCode);
        Ticket ticket = flight.getTicket(passenger);
        flight.assignSeat(row, seat, ticket);

        syncNotify(flightCode, passenger, handler -> {
            try {
                Ticket t = flight.getTickets().stream().filter(tic -> tic.getPassenger()
                        .equals(passenger)).findFirst().orElseThrow(FlightNotFoundException::new);
                handler.notifyAssignSeat(flightCode, flight.getDestination(), t.getCategory(),
                        row, seat);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void changeSeat(String flightCode, String passenger, int freeRow, char freeSeat) throws RemoteException {
        // Flight flight = getValidatedFlight(flightCode, passenger, freeRow, freeSeat);

        Flight flight = validateFlightCode(flightCode);
        flight.changeSeat(freeRow, freeSeat, passenger); // TODO: check


        syncNotify(flightCode, passenger, handler -> {
            try {
                Ticket ticket = flight.getTickets().stream().filter(t -> t.getPassenger()
                        .equals(passenger)).findFirst().orElseThrow(TicketNotFoundException::new);
                handler.notifyChangeSeat(flightCode, flight.getDestination(),
                        ticket.getCategory(), ticket.getRow(), ticket.getCol(), RowCategory.ECONOMY,
                        freeRow, freeSeat);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private Flight validateFlightCode(String flightCode) {
        store.getFlightsLock().lock();
        try {
            return Optional.ofNullable(store.getFlights().get(flightCode))
                    .orElseThrow(FlightNotFoundException::new); // TODO: nuestra excepcion;
        } finally {
            store.getFlightsLock().unlock();
        }
    }

    @Override
    public List<FlightResponse> listAlternativeFlights(String flightCode, String passenger) throws RemoteException { //TODO : return string ?
        Flight flight = validateFlightCode(flightCode);

        if (flight.getState().equals(FlightState.CONFIRMED))
            throw new IllegalPlaneStateException();

        //      JFK | AA101 | 7 BUSINESS
        //      JFK | AA119 | 3 BUSINESS
        //      JFK | AA103 | 18 PREMIUM_ECONOMY
        Ticket ticket = flight.getTicket(passenger);
        String destination = ticket.getDestination();

        List<Flight> alternativeFlights = store.getFlights().values()
                .stream().filter(f -> !f.equals(flight) && f.getDestination().equals(destination)
                        && f.getState() == FlightState.PENDING)
                .collect(Collectors.toList());

        List<FlightResponse> toReturn = new ArrayList<>();

        alternativeFlights.forEach(alternative -> {
            Map<RowCategory, Integer> availableSeats = new HashMap<>();
            for (int i = ticket.getCategory().ordinal(); i >= 0; i--) {
                int available = alternative.getAvailableByCategory(RowCategory.values()[i]);
                if (i > 0)
                    availableSeats.put(RowCategory.values()[i], available);
            }

            if (availableSeats.keySet().size() > 0)
                toReturn.add(new FlightResponse(alternative.getCode(), destination, availableSeats));
        });

        return toReturn;
    }

    @Override
    public void changeFlight(String passenger, String oldFlightCode, String newFlightCode) throws RemoteException {
        Flight oldFlight;
        Flight newFlight;

        store.getFlightsLock().lock(); // TODO refactor
        try {
            oldFlight = Optional.ofNullable(store.getFlights().get(oldFlightCode))
                    .filter(f -> !f.getState().equals(FlightState.CONFIRMED))
                    .orElseThrow(FlightNotFoundException::new);
            newFlight = Optional.ofNullable(store.getFlights().get(newFlightCode))
                    .filter(f -> f.getState().equals(FlightState.PENDING))
                    .orElseThrow(FlightNotFoundException::new);
        } finally {
            store.getFlightsLock().unlock();
        }

        oldFlight.changeFlight(passenger, newFlight);

        syncNotify(oldFlightCode, passenger, handler -> {
            try {
                handler.notifyChangeTicket(oldFlightCode, oldFlight.getDestination(), newFlightCode);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

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
                handlerConsumer.accept(handler);
            }
        }
    }
}
