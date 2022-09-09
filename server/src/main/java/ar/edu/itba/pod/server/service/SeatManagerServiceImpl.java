package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.interfaces.SeatManagerService;
import ar.edu.itba.pod.models.*;
import ar.edu.itba.pod.models.FlightResponse;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.TicketNotFoundException;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.FlightNotFoundException;
import ar.edu.itba.pod.server.ServerStore;
import ar.edu.itba.pod.server.models.Flight;

import java.rmi.RemoteException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class SeatManagerServiceImpl implements SeatManagerService {

    private final ServerStore store;

//    private final Lock seatLock = new ReentrantLock();

    public SeatManagerServiceImpl(ServerStore store) {
        this.store = store;
    }

    @Override
    public boolean isAvailable(String flightCode, int row, char seat) throws RemoteException {
        Flight flight = validateFlightCode(flightCode);
        synchronized (flight) {
            return flight.getPlane().checkSeat(row, seat);
        }
    }

    @Override
    public void assign(String flightCode, String passenger, int row, char seat) throws RemoteException {
        // TODO: chequear
        Flight flight = getValidatedFlight(flightCode, passenger, row, seat);
        Ticket ticket = getTicket(flight, passenger);

        synchronized (flight) {
            flight.getPlane().assignSeat(row, seat, ticket);
        }

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
        Flight flight = getValidatedFlight(flightCode, passenger, freeRow, freeSeat);

        synchronized (flight) {
            flight.changeSeat(freeRow, freeSeat, passenger); // TODO: check
        }

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

    private Flight getValidatedFlight(String flightCode, String passenger, int row, char seat) {
        Flight flight = validateFlightCode(flightCode);
        RowCategory ticketCategory = getTicket(flight, passenger).getCategory();
        RowCategory category = flight.getPlane().getRows()[row].getRowCategory();
        if (category.ordinal() > ticketCategory.ordinal())
            throw new RuntimeException();
        return flight;
    }

    @Override
    public List<FlightResponse> listAlternativeFlights(String flightCode, String passenger) throws RemoteException { //TODO : return string ?
        validateFlightCode(flightCode);

        //TODO: listAlternativeFlights

        //      JFK | AA101 | 7 BUSINESS
        //      JFK | AA119 | 3 BUSINESS
        //      JFK | AA103 | 18 PREMIUM_ECONOMY

        Flight flight = store.getFlights().get(flightCode);
        Ticket ticket = getTicket(flight, passenger);
        String destination = ticket.getDestination();

        List<Flight> alternativeFlights = store.getFlights().values()
                .stream().filter(f -> f.getDestination().equals(destination)
                        && f.getState() == FlightState.PENDING)
                .collect(Collectors.toList());
//        alternativeFlights = alternativeFlights.stream().filter();

        //List<FlightResponse> toRet = FlightResponse.compactFlights(alternativeFlights);

//        return alternativeFlights;
        //return toRet;
        return new ArrayList<>();

//        StringBuilder stringBuilder = new StringBuilder();
//        for (Flight f : alternativeFlights) {
//            stringBuilder.append(f.getDestination()).append(" | ");
//            stringBuilder.append(f.getCode()).append(" | ");
//            for (int category = ticket.getCategory().ordinal();
//                 category >= 0; category--) {
//                RowCategory cat = RowCategory.values()[category];
//                stringBuilder.append(f.getAvailableSeats(cat)).append(" ").append(cat).append('\n');
//            }
//
//        }


    }

    @Override
    public void changeFlight(String passenger, String oldFlightCode, String newFlightCode) throws RemoteException {
        Flight oldFlight;
        Flight newFlight;

        store.getFlightsLock().lock();
        try {
            oldFlight = Optional.ofNullable(store.getFlights().get(oldFlightCode))
                    .orElseThrow(FlightNotFoundException::new);
            newFlight = Optional.ofNullable(store.getFlights().get(newFlightCode))
                    .orElseThrow(FlightNotFoundException::new);
        } finally {
            store.getFlightsLock().unlock();
        }

        final Flight[] locks = Stream.of(oldFlight, newFlight)
                .sorted(Comparator.comparing(Flight::getCode))
                .toArray(Flight[]::new);

        Ticket ticket;
        synchronized (locks[0]) {
            synchronized (locks[1]) {
                ticket = getTicket(oldFlight, passenger);
                oldFlight.getTickets().remove(ticket);
                newFlight.getTickets().add(ticket);
            }
        }

        syncNotify(oldFlightCode, passenger, handler -> {
            try {
                handler.notifyChangeTicket(oldFlightCode, oldFlight.getDestination(), newFlightCode);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

    }

    private Flight validateFlightCode(String flightCode) {
        store.getFlightsLock().lock();
        try {
            return Optional.ofNullable(store.getFlights().get(flightCode))
                    .orElseThrow(FlightNotFoundException::new); // TODO: nuestra excepcion
        } finally {
            store.getFlightsLock().unlock();
        }
    }

    private Ticket getTicket(Flight flight, String passenger) {
        synchronized (flight) {
            return flight.getTickets().stream().filter(t -> t.getPassenger().equals(passenger))
                    .findFirst().orElseThrow(TicketNotFoundException::new);
        }
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
