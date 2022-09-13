package ar.edu.itba.pod.client.utils;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.models.FlightState;
import ar.edu.itba.pod.models.Notification;
import ar.edu.itba.pod.models.RowCategory;

import java.rmi.RemoteException;

public class NotificationHandlerImpl implements NotificationHandler {
    //You are following Flight AA100 with destination JFK.
    @Override
    public void notifyRegister(Notification notification) throws RemoteException {
        System.out.println("You are following Flight " + notification.getOldCode() +
                " with destination " + notification.getDestination());
    }


    //Your Flight AA100 with destination JFK was confirmed and your seat is PREMIUM_ECONOMY 15D.
    @Override
    public void notifyConfirmFlight(Notification notification) throws RemoteException {
        // TODO: check for nulls on row / col / category
        System.out.println("Your Flight " + notification.getOldCode() + " with destination "
                + notification.getDestination() + " was confirmed and your seat is "
                + notification.getOldCategory() + " " + notification.getCurrentRow() +
                notification.getCurrentCol());
    }

    //Your seat changed to BUSINESS 2C from BUSINESS 1B for Flight AA100 with destination JFK.
    @Override
    public void notifyCancelFlight(Notification notification) throws RemoteException {
        // TODO: check for nulls on row / col / category
        System.out.println("Your Flight " + notification.getOldCode() + " with destination "
                + notification.getDestination() + " was cancelled and your seat is " +
                notification.getOldCategory() + " " + notification.getCurrentRow() +
                notification.getCurrentCol());
    }

    //Your seat is BUSINESS 1B for Flight AA100 with destination JFK.
    @Override
    public void notifyAssignSeat(Notification notification) throws RemoteException {
        System.out.println("Your seat is " + notification.getOldCategory() + " "
                + notification.getCurrentRow() + notification.getCurrentCol() + " for Flight " +
                notification.getOldCode() + " with destination " + notification.getDestination());
    }

    //Your seat changed to BUSINESS 2C from BUSINESS 1B for Flight AA100 with destination JFK.
    @Override
    public void notifyChangeSeat(Notification notification) throws RemoteException {
        System.out.println("Your seat changed to " + notification.getNewCategory() + " " +
                notification.getNewRow() + notification.getNewCol() + " from " +
                notification.getOldCategory() + " " + notification.getCurrentRow() +
                notification.getCurrentCol() + " from Flight " + notification.getOldCode() +
                " with destination " + notification.getDestination());
    }

    //Your ticket changed to Flight AA101 with destination JFK from Flight AA100 with destination JFK.
    @Override
    public void notifyChangeTicket(Notification notification) throws RemoteException {
        System.out.println("Your ticket changed to Flight " + notification.getNewCode() + " with destination " + notification.getDestination()
                + " from Flight " + notification.getOldCode() + " with destination " +
                notification.getDestination());
    }
}
