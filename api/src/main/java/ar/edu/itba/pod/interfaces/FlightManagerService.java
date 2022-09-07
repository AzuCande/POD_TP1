package ar.edu.itba.pod.interfaces;

import ar.edu.itba.pod.models.FlightState;
import ar.edu.itba.pod.models.PlaneModel;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.RowCategory;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

public interface FlightManagerService extends Remote {
    void addPlaneModel(String model, Map<String, int[]> seatCategories) throws RemoteException;

    void addFlight(String planeModel, String flightCode, String destination, Map<RowCategory, Set<String>> ticketMap) throws RemoteException;

    FlightState getFlightState(String flightCode) throws RemoteException;

    void confirmFlight(String flightCode) throws RemoteException;

    void cancelFlight(String flightCode) throws RemoteException;

    void changeCancelledFlights() throws RemoteException;

    void listAlternativeFlights() throws RemoteException;

    Set<PlaneModel> getPlaneModels() throws RemoteException;

    Set<Flight> getFlights() throws RemoteException;

    boolean isPlaneModelAvailable(String PlaneModel) throws RemoteException;

    boolean isFlightCodeAvailable(String flightCode) throws RemoteException;
}
