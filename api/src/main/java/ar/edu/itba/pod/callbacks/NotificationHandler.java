package ar.edu.itba.pod.callbacks;

import ar.edu.itba.pod.models.FlightState;
import ar.edu.itba.pod.models.RowCategory;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotificationHandler extends Remote {
    void notifyRegister(String flightCode, String destination, RowCategory category,
                        int row, char col) throws RemoteException; //OK
    void notifyCancelFlight(String flightCode, String destination, FlightState state,
                                 RowCategory category, int row, char col) throws RemoteException; //OK

    void notifyConfirmFlight(String flightCode, String destination, FlightState state,
                                 RowCategory category, int row, char col) throws RemoteException; //OK

    void notifyAssignSeat(String flightCode, String destination, RowCategory category,
                          int row, char col) throws RemoteException; //OK

    void notifyChangeSeat(String flightCode, String destination, RowCategory category,
                          int row, char col, RowCategory newCategory, int newRow, char newCol ) throws RemoteException;

    void notifyChangeTicket(String flightCode, String destination, String newFlightCode) throws RemoteException; //OK

}
