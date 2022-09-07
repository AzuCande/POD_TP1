package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.interfaces.SeatManagerService;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.RowCategory;
import ar.edu.itba.pod.server.ServerStore;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SeatManagerServiceImpl implements SeatManagerService {

    private final ServerStore store;

    private Lock seatLock = new ReentrantLock();

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

        validateFlightCode(flightCode);
        Flight flight = store.getFlights().get(flightCode);

        //Empezamos haciendo que un ticket puede ser asignado solamente a su categoría y no a una inferior, TODO : implementar que se pueda asignar a una inferior
        RowCategory category = flight.getPlane().getRows()[row].getRowCategory();
        Set<String> permittedPassengers = flight.getTicketMap().get(category);

        //TODO: chequear
        if (permittedPassengers.contains(passenger))
            flight.getPlane().assignSeat(row, seat, passenger);
        else {
            throw new RuntimeException();
        }

    }

    @Override
    public void changeSeat(String flightCode, String passenger, int freeRow, char freeSeat) throws RemoteException {
        validateFlightCode(flightCode);

        store.getFlights().get(flightCode).getPlane().changeSeat(freeRow, freeSeat, passenger);
    }

    @Override
    public void listAlternativeFlights(String flightCode, String passenger) throws RemoteException {
        validateFlightCode(flightCode);

        //TODO: listAlternativeFlights

    }

    @Override
    public void changeTicket(String passenger, String oldFlightCode, String newFlightCode) throws RemoteException {
    }

    private void validateFlightCode(String flightCode) {

        store.getFlightsLock().lock();
        if (!store.getFlights().containsKey(flightCode)) {
            store.getFlightsLock().unlock();
            throw new NoSuchElementException("Flight does not exists");
        }
        store.getFlightsLock().unlock();

    }
}
