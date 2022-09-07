package ar.edu.itba.pod.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SeatQueryService extends Remote {

    // TODO: enum + clase wrapper?
    // TODO: return????
    void query(String flightCode, String queryParams) throws RemoteException;
}
