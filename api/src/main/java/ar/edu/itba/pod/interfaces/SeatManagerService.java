package ar.edu.itba.pod.interfaces;

import ar.edu.itba.pod.models.FlightResponse;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface SeatManagerService extends Remote {
    boolean isAvailable(String flightCode, int row, char seat) throws RemoteException;

    void assign(String flightCode, String passenger, int row, char seat) throws RemoteException;

    void changeSeat(String flightCode, String passenger, int freeRow, char freeSeat) throws RemoteException;

    List<FlightResponse> listAlternativeFlights(String flightCode, String passenger) throws RemoteException;

    void changeFlight(String passenger, String oldFlightCode, String newFlightCode) throws RemoteException;
}
