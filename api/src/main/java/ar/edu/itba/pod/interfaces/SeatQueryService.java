package ar.edu.itba.pod.interfaces;

import ar.edu.itba.pod.models.ResponseRow;
import ar.edu.itba.pod.models.RowCategory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface SeatQueryService extends Remote {

    // TODO: enum + clase wrapper?
    // TODO: return????
    Map<Integer, ResponseRow> query(String flightCode) throws RemoteException;

    Map<Integer, ResponseRow> query(String flightCode, RowCategory rowCategory) throws RemoteException;

    ResponseRow query(String flightCode, int row) throws RemoteException;
}
