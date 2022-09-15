package ar.edu.itba.pod.callbacks;

import ar.edu.itba.pod.models.Notification;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotificationHandler extends Remote {
    void notifyRegister(Notification notification) throws RemoteException; 

    void notifyCancelFlight(Notification notification) throws RemoteException;

    void notifyConfirmFlight(Notification notification) throws RemoteException;

    void notifyAssignSeat(Notification notification) throws RemoteException;

    void notifyChangeSeat(Notification notification) throws RemoteException;

    void notifyChangeTicket(Notification notification) throws RemoteException;

}
