package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.interfaces.SeatManagerService;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.FlightState;
import ar.edu.itba.pod.models.RowCategory;
import ar.edu.itba.pod.models.Ticket;
import ar.edu.itba.pod.server.ServerStore;

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
        seatLock.lock();
        boolean isAvailable = store.getFlights().get(flightCode).getPlane().checkSeat(row, seat);
        seatLock.unlock();
        return isAvailable;
    }

    @Override
    public void assign(String flightCode, String passenger, int row, char seat) throws RemoteException {

        //TODO: chequear
        Flight flight = getValidatedFlight(flightCode, passenger, row, seat);
        seatLock.lock();
        flight.getPlane().assignSeat(row, seat, passenger);
        seatLock.unlock();
    }

    @Override
    public void changeSeat(String flightCode, String passenger, int freeRow, char freeSeat) throws RemoteException {
        Flight flight = getValidatedFlight(flightCode, passenger, freeRow, freeSeat);
        seatLock.lock();
        flight.getPlane().changeSeat(freeRow, freeSeat, passenger);
        seatLock.unlock();
    }

    private Flight getValidatedFlight(String flightCode, String passenger, int row, char seat) {
        validateFlightCode(flightCode);
        Flight flight = store.getFlights().get(flightCode);
        RowCategory ticketCategory = getTicket(flight, passenger).getCategory();
        RowCategory category = flight.getPlane().getRows()[row].getRowCategory();
        if (category.getValue() > ticketCategory.getValue())
            throw new RuntimeException();
        return flight;
    }

    @Override
    public void listAlternativeFlights(String flightCode, String passenger) throws RemoteException { //TODO : return string ?
        validateFlightCode(flightCode);

        //TODO: listAlternativeFlights

        Flight flight = store.getFlights().get(flightCode);
        Ticket ticket = getTicket(flight, passenger);
        String destination = ticket.getDestination();

        List<Flight> alternativeFlights = store.getFlights().values()
                .stream().filter(f -> Objects.equals(flight.getDestination(), destination) && f.getState() == FlightState.PENDING
                )
                .collect(Collectors.toList());
//        alternativeFlights = alternativeFlights.stream().filter();
    }

    @Override
    public void changeFlight(String passenger, String oldFlightCode, String newFlightCode) throws RemoteException {
    }

    private void validateFlightCode(String flightCode) {

        store.getFlightsLock().lock();
        if (!store.getFlights().containsKey(flightCode)) {
            store.getFlightsLock().unlock();
            throw new NoSuchElementException("Flight does not exists");
        }
        store.getFlightsLock().unlock();

    }

    private Ticket getTicket(Flight flight, String passenger) {
        return flight.getTickets().stream().filter(t -> Objects.equals(t.getPassenger(), passenger)).findFirst().orElseThrow(RuntimeException::new);
    }


}
