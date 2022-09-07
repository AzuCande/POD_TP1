package ar.edu.itba.pod.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SeatManagerService extends Remote {
    boolean isAvailable(String flightCode, int row, char seat) throws RemoteException;

    void assign(String flightCode, String passenger, int row, char seat) throws RemoteException;

    void changeSeat(String flightCode, String passenger, int freeRow, char freeSeat) throws RemoteException;

    //TODO: es void?
    void listAlternativeFlights(String flightCode, String passenger) throws RemoteException;

    void changeFlight(String passenger, String oldFlightCode, String newFlightCode) throws RemoteException;
}
