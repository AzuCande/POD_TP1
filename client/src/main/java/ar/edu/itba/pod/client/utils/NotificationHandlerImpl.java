package ar.edu.itba.pod.client.utils;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.models.Notification;

import java.rmi.RemoteException;

public class NotificationHandlerImpl implements NotificationHandler {
    //You are following Flight AA100 with destination JFK.
    @Override
    public void notifyRegister(Notification notification) throws RemoteException {
        System.out.printf("You are following Flight %s with destination %s\n",
                notification.getOldCode(), notification.getDestination());

    }


    //Your Flight AA100 with destination JFK was confirmed and your seat is PREMIUM_ECONOMY 15D.
    @Override
    public void notifyConfirmFlight(Notification notification) throws RemoteException {
        StringBuilder response = new StringBuilder("Your Flight ");
        response.append(notification.getOldCode()).append(" with destination ")
                .append(notification.getDestination()).append(" was confirmed");

        if (notification.getCurrentRow() != null) {
            response.append(String.format(" and your seat is %s %d%c", notification.getOldCategory(),
                    notification.getCurrentRow(), notification.getCurrentCol()));
        }

        System.out.println(response);
    }

    //Your seat changed to BUSINESS 2C from BUSINESS 1B for Flight AA100 with destination JFK.
    @Override
    public void notifyCancelFlight(Notification notification) throws RemoteException {
        StringBuilder response = new StringBuilder("Your Flight ");
        response.append(notification.getOldCode()).append(" with destination ")
                .append(notification.getDestination()).append(" was cancelled");

        if (notification.getCurrentRow() != null) {
            response.append(String.format(" and your seat is %s %d%c", notification.getOldCategory(),
                    notification.getCurrentRow(), notification.getCurrentCol()));
        }

        System.out.println(response);
    }

    //Your seat is BUSINESS 1B for Flight AA100 with destination JFK.
    @Override
    public void notifyAssignSeat(Notification notification) throws RemoteException {
        System.out.printf("Your seat is %s %d%c for Flight %s with destination %s\n",
                notification.getOldCategory(), notification.getCurrentRow(),
                notification.getCurrentCol(), notification.getOldCode(),
                notification.getDestination());
    }

    //Your seat changed to BUSINESS 2C from BUSINESS 1B for Flight AA100 with destination JFK.
    @Override
    public void notifyChangeSeat(Notification notification) throws RemoteException {
        System.out.printf("Your seat changed to %s %d%c from %s %d%c for Flight %s with destination %s\n",
                notification.getNewCategory(), notification.getNewRow(), notification.getNewCol(),
                notification.getOldCategory(), notification.getCurrentRow(),
                notification.getCurrentCol(), notification.getOldCode(),
                notification.getDestination());
    }

    //Your ticket changed to Flight AA101 with destination JFK from Flight AA100 with destination JFK.
    @Override
    public void notifyChangeTicket(Notification notification) throws RemoteException {
        System.out.printf("Your ticket changed to Flight %s with destination %s from Flight %s " +
                        "with destination %s\n",
                notification.getNewCode(), notification.getDestination(),
                notification.getOldCode(), notification.getDestination());
    }
}
