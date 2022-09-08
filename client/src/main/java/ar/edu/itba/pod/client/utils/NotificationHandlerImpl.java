package ar.edu.itba.pod.client.utils;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.models.FlightState;
import ar.edu.itba.pod.models.RowCategory;

import java.rmi.RemoteException;

public class NotificationHandlerImpl implements NotificationHandler {
    //You are following Flight AA100 with destination JFK.
    @Override
    public void notifyRegister(String flightCode, String destination, RowCategory category, int row, char col) throws RemoteException {
        System.out.println("You are following Flight " + flightCode + " with destination " + destination);
        // TODO: preguntar por que no usa nada
        // + " in " + category + " in row " + row + " and col " + col);
    }


    //Your Flight AA100 with destination JFK was confirmed and your seat is PREMIUM_ECONOMY 15D.
    @Override
    public void notifyConfirmFlight(String flightCode, String destination, FlightState state, RowCategory category, int row, char col) throws RemoteException {
        System.out.println("Your Flight " + flightCode + " with destination " + destination +
                " was confirmed and your seat is " + category + " " + row + col);
    }

    //Your seat changed to BUSINESS 2C from BUSINESS 1B for Flight AA100 with destination JFK.
    @Override
    public void notifyCancelFlight(String flightCode, String destination, FlightState state, RowCategory category, int row, char col) throws RemoteException {
        System.out.println("Your Flight " + flightCode + " with destination " + destination +
                " was cancelled and your seat is " + category + " " + row + col);
    }

    //Your seat is BUSINESS 1B for Flight AA100 with destination JFK.
    @Override
    public void notifyAssignSeat(String flightCode, String destination, RowCategory category, int row, char col) throws RemoteException {

        System.out.println("Your seat is " + category + " " + row + col + " for Flight" + flightCode + " with destination" + destination);
    }

    //Your seat changed to BUSINESS 2C from BUSINESS 1B for Flight AA100 with destination JFK.
    @Override
    public void notifyChangeSeat(String flightCode, String destination, RowCategory category,
                                 int row, char col, RowCategory newCategory, int newRow, char newCol) throws RemoteException {
        System.out.println("Your seat changed to " + newCategory + " " + newRow + newCol +
                " from " + category + " " + row + col + " from Flight " + flightCode + " with destination " + destination); //TODO: standby
    }

    //Your ticket changed to Flight AA101 with destination JFK from Flight AA100 with destination JFK.
    @Override
    public void notifyChangeTicket(String flightCode, String destination, String newFlightCode) throws RemoteException {
        System.out.println("Your ticket changed to Flight " + newFlightCode + " with destination " + destination + "from Flight " + flightCode + " with destination " + destination);
    }
}
