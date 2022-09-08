package ar.edu.itba.pod.callbacks;

import ar.edu.itba.pod.models.FlightState;
import ar.edu.itba.pod.models.RowCategory;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotificationHandler extends Remote {
    void notifyRegister(String flightCode, String destination, RowCategory category, int row, int col) throws RemoteException;
    void notifyFlightStateChange(String flightCode, String destination, FlightState state, RowCategory category, int row, int col) throws RemoteException;
    void notifyAssignSeat(String flightCode, String destination, RowCategory category, int row, int col) throws RemoteException;
    void notifyChangeSeat(String flightCode, String destination, RowCategory category, int row, int col) throws RemoteException;
    void notifyChangeTicket(String flightCode, String destination) throws RemoteException;

}
