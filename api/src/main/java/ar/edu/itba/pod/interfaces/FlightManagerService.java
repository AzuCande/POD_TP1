package ar.edu.itba.pod.interfaces;

import ar.edu.itba.pod.models.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FlightManagerService extends Remote {
    void addPlaneModel(String model, Map<String, int[]> seatCategories) throws RemoteException;

    void addFlight(String planeModel, String flightCode, String destination, List<Ticket> tickets) throws RemoteException;

    FlightState getFlightState(String flightCode) throws RemoteException;

    void confirmFlight(String flightCode) throws RemoteException;

    void cancelFlight(String flightCode) throws RemoteException;

    void changeCancelledFlights() throws RemoteException;
}
