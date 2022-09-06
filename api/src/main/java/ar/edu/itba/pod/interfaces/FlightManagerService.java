package ar.edu.itba.pod.interfaces;

import ar.edu.itba.pod.model.FlightState;
import ar.edu.itba.pod.model.PlaneModel;
import ar.edu.itba.pod.model.Flight;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

public interface FlightManagerService extends Remote {
    void addFlight(String planeModel, String flightCode, String destination, Map<String, Set<String>> ticketMap) throws RemoteException;

    void addPlaneModel(String model, Map<String, int[]> seatCategories) throws RemoteException;

    FlightState getFlightState(String flight) throws RemoteException;

    void setFlightState(FlightState newState) throws RemoteException;

    void listAlternativeFlights() throws RemoteException;

    Set<PlaneModel> getPlaneModels() throws RemoteException;

    Set<Flight> getFlights() throws RemoteException;

    boolean isPlanemodelAvailable(String PlaneModel) throws RemoteException;

    boolean isFlightcodeAvailable(String flightCode) throws RemoteException;
}
