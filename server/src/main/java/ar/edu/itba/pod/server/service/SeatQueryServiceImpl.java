package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.interfaces.SeatQueryService;
import ar.edu.itba.pod.models.*;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.FlightNotFoundException;
import ar.edu.itba.pod.models.exceptions.planeExceptions.IllegalPlaneStateException;
import ar.edu.itba.pod.server.ServerStore;
import ar.edu.itba.pod.server.models.Flight;
import ar.edu.itba.pod.server.models.Row;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

public class SeatQueryServiceImpl implements SeatQueryService {

    private final ServerStore store;

    public SeatQueryServiceImpl(ServerStore store) {
        this.store = store;
    }

    @Override
    public Map<Integer, ResponseRow> query(String flightCode) throws RemoteException {
        Map<Integer, ResponseRow> responseMap = new HashMap<>();
        Row[] rows = getFlight(flightCode).getRows();

        int i = 0;
        for (Row row : rows) {
            responseMap.put(i++, new ResponseRow(row.getRowCategory(), getPassengerInitials(row)));
        }
        return responseMap;
    }

    @Override
    public Map<Integer, ResponseRow> query(String flightCode, RowCategory rowCategory) throws RemoteException {
        Map<Integer, ResponseRow> responseMap = new HashMap<>();
        List<Row> rows = Arrays.stream(getFlight(flightCode).getRows()).filter(row -> row.getRowCategory() == rowCategory).collect(Collectors.toList());

        int i = 0;
        for (Row row : rows) {
            responseMap.put(i++, new ResponseRow(row.getRowCategory(), getPassengerInitials(row)));
        }
        return responseMap;
    }

    @Override
    public ResponseRow query(String flightCode, int rowNum) throws RemoteException {
        Row row = getFlight(flightCode).getRows()[rowNum];
        return new ResponseRow(row.getRowCategory(), getPassengerInitials(row));
    }

    private Flight getFlight(String flightCode) {
        store.getFlightsLock().lock();
        try {
            return Optional.ofNullable(store.getFlights().get(flightCode)).orElseThrow(FlightNotFoundException::new);
        } finally {
            store.getFlightsLock().lock();
        }
    }

    private char[] getPassengerInitials(Row row) {
        char[] initials = new char[row.getPassengerNames().length];
        for (int j = 0; j < row.getPassengerNames().length; j++) {
            initials[j] = row.getPassengerNames()[j] == null ? '*' : row.getPassengerNames()[j].charAt(0);
        }
        return initials;
    }
}
