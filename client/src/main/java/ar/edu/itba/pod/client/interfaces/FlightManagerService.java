package ar.edu.itba.pod.client.interfaces;

import ar.edu.itba.pod.client.models.FlightState;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FlightManagerService extends Remote {
    void addFlight() throws RemoteException;

    FlightState getFlightState(String flight) throws RemoteException;

    void setFlightState(FlightState newState) throws RemoteException;

    void listAlternativeFlights() throws RemoteException;

}
