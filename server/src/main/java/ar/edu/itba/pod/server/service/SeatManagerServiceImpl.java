package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.interfaces.SeatManagerService;
import ar.edu.itba.pod.models.*;
import ar.edu.itba.pod.models.FlightResponse;
import ar.edu.itba.pod.server.ServerStore;
import ar.edu.itba.pod.server.models.Flight;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


public class SeatManagerServiceImpl implements SeatManagerService {

    private final ServerStore store;

    private final Lock seatLock = new ReentrantLock();


    public SeatManagerServiceImpl(ServerStore store) {
        this.store = store;
    }

    @Override
    public boolean isAvailable(String flightCode, int row, char seat) throws RemoteException {
        validateFlightCode(flightCode);
        seatLock.lock(); // FIXME: el seat debería lockear
        boolean isAvailable = store.getFlights().get(flightCode).getPlane().checkSeat(row, seat);
        seatLock.unlock();
        return isAvailable;
    }

    @Override
    public void assign(String flightCode, String passenger, int row, char seat) throws RemoteException {
        //TODO: chequear
        Flight flight = getValidatedFlight(flightCode, passenger, row, seat);
        RowCategory category = flight.getPlane().getRows()[row].getRowCategory();
        Ticket ticket = new Ticket(category, passenger, flight.getDestination());
        seatLock.lock();
        flight.getPlane().assignSeat(row, seat, ticket);
        seatLock.unlock();

        store.getNotifications().getOrDefault(flightCode, new HashMap<>())
                .getOrDefault(passenger, new ArrayList<>())
                .forEach(handler -> {
                    try {
                        Ticket t = flight.getTickets().stream().filter(tic -> tic.getPassenger()
                                .equals(passenger)).findFirst().orElseThrow(IllegalArgumentException::new);
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
        seatLock.lock();
        flight.changeSeat(freeRow, freeSeat, passenger);
        seatLock.unlock();


        store.getNotifications().getOrDefault(flightCode, new HashMap<>())
                .getOrDefault(passenger, new ArrayList<>())
                .forEach(handler -> {
                    try {
                        Ticket ticket = flight.getTickets().stream().filter(t -> t.getPassenger()
                                .equals(passenger)).findFirst().orElseThrow(IllegalArgumentException::new);
                        handler.notifyChangeSeat(flightCode, flight.getDestination(),
                                ticket.getCategory(), ticket.getRow(), ticket.getCol(), RowCategory.ECONOMY,
                                freeRow, freeSeat);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });
    }

    private Flight getValidatedFlight(String flightCode, String passenger, int row, char seat) {
        validateFlightCode(flightCode);
        Flight flight = store.getFlights().get(flightCode);
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
        // TODO: do it
        store.getNotifications().getOrDefault(oldFlightCode, new HashMap<>())
                .getOrDefault(passenger, new ArrayList<>())
                .forEach(handler -> {
                    try {
                        /*
                        Ticket ticket = flight.getTickets().stream().filter(t -> t.getPassenger()
                                .equals(passenger)).findFirst().orElseThrow(IllegalArgumentException::new);
                        handler.notifyChangeSeat(flightCode, flight.getDestination(), ticket.getCategory(), freeRow, freeSeat); // TODO: es el viejo
                         */
                        throw new RemoteException();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });

    }

    private void validateFlightCode(String flightCode) {
        store.getFlightsLock().lock();

        try {
            if (!store.getFlights().containsKey(flightCode))
                throw new NoSuchElementException("Flight does not exists");
        } finally {
            store.getFlightsLock().unlock();
        }
    }

    private Ticket getTicket(Flight flight, String passenger) {
        return flight.getTickets().stream().filter(t -> t.getPassenger().equals(passenger))
                .findFirst().orElseThrow(RuntimeException::new);
    }
}
