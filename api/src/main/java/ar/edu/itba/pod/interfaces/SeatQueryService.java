package ar.edu.itba.pod.interfaces;

import ar.edu.itba.pod.models.ResponseRow;
import ar.edu.itba.pod.models.RowCategory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface SeatQueryService extends Remote {
    List<ResponseRow> query(String flightCode) throws RemoteException;

    List<ResponseRow> query(String flightCode, RowCategory rowCategory) throws RemoteException;

    ResponseRow query(String flightCode, int row) throws RemoteException;
}
