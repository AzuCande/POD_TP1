package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.interfaces.SeatManagerService;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.server.ServerStore;

import java.rmi.RemoteException;
import java.util.NoSuchElementException;

public class SeatManagerServiceImpl implements SeatManagerService {

    private final ServerStore store;

    public SeatManagerServiceImpl(ServerStore store) {
        this.store = store;
    }

    @Override
    public boolean isAvailable(String flightCode, int row, char seat) throws RemoteException {

        validateFlightCode(flightCode);

        return store.getFlights().get(flightCode).getPlane().checkSeat(row, seat);
    }

    @Override
    public void assign(String flightCode, String passenger, int row, char seat) throws RemoteException {

        validateFlightCode(flightCode);
        Flight flight = store.getFlights().get(flightCode);

        flight.getPlane().assignSeat(row, seat, passenger);
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
        if (!store.getFlights().containsKey(flightCode)) {
            throw new NoSuchElementException("Flight does not exists");
        }
    }
}
