package ar.edu.itba.pod.callbacks;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotificationHandler extends Remote {

    void notifyMethod() throws RemoteException;
}
