package ar.edu.itba.pod.interfaces;

import ar.edu.itba.pod.callbacks.NotificationHandler;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotificationService extends Remote {
    void registerPassenger(String flightCode, String passenger, NotificationHandler handler) throws RemoteException;
}
