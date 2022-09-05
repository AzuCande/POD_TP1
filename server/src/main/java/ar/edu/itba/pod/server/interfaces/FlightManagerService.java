package ar.edu.itba.pod.server.interfaces;

import ar.edu.itba.pod.server.model.FlightState;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FlightManagerService extends Remote {
    void addFlight() throws RemoteException;

    FlightState getFlightState(String flight) throws RemoteException;

    void setFlightState(FlightState newState) throws RemoteException;

    void listAlternativeFlights() throws RemoteException;

}
