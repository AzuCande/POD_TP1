package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.interfaces.SeatQueryService;
import ar.edu.itba.pod.models.*;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.FlightNotFoundException;
import ar.edu.itba.pod.server.utils.ServerStore;
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
    public ArrayList<ResponseRow> query(String flightCode) throws RemoteException {
        ArrayList<ResponseRow> responseRows = new ArrayList<>();
        Flight flight;

        synchronized (store.getFlightCodes()) {
            FlightState state = store.getFlightCodes().get(flightCode);

            Map<String, Flight> flights = store.getFlightsByState(state);
            synchronized (flights) {
                flight = flights.get(flightCode);
                flight.getSeatsLock().lock();
            }
        }

        Row[] rows = flight.getRows();

        for (Row row : rows) {
            responseRows.add(new ResponseRow(row.getRowCategory(), getPassengerInitials(row)));
        }
        flight.getSeatsLock().unlock();
        return responseRows;
    }

    @Override
    public ArrayList<ResponseRow> query(String flightCode, RowCategory rowCategory) throws RemoteException {
        ArrayList<ResponseRow> responseRows = new ArrayList<>();

        Flight flight;
        synchronized (store.getFlightCodes()) {
            FlightState state = store.getFlightCodes().get(flightCode);

            Map<String, Flight> flights = store.getFlightsByState(state);
            synchronized (flights) {
                flight = flights.get(flightCode);
                flight.getSeatsLock().lock();
            }
        }
        List<Row> rows = Arrays.stream(getFlight(flightCode).getRows())
                .filter(row -> row.getRowCategory() == rowCategory).collect(Collectors.toList());

        for (Row row : rows) {
            responseRows.add(new ResponseRow(row.getRowCategory(), getPassengerInitials(row)));
        }
        flight.getSeatsLock().unlock();
        return responseRows;
    }

    @Override
    public ResponseRow query(String flightCode, int rowNum) throws RemoteException {
        Flight flight;
        synchronized (store.getFlightCodes()) {
            FlightState state = store.getFlightCodes().get(flightCode);

            Map<String, Flight> flights = store.getFlightsByState(state);
            synchronized (flights) {
                flight = flights.get(flightCode);
                flight.getSeatsLock().lock();
            }
        }
        Row row = getFlight(flightCode).getRows()[rowNum];
        flight.getSeatsLock().unlock();
        return new ResponseRow(row.getRowCategory(), getPassengerInitials(row));
    }

    private Flight getFlight(String flightCode) {
        synchronized (store.getFlightCodes()) {
            FlightState state = Optional.ofNullable(store.getFlightCodes().get(flightCode)).orElseThrow(FlightNotFoundException::new);
            return store.getFlightsByState(state).get(flightCode);
        }
    }

    private char[] getPassengerInitials(Row row) {
        char[] initials = new char[row.getPassengerNames().length];
        for (int j = 0; j < row.getPassengerNames().length; j++) {
            initials[j] = row.getPassengerNames()[j] == null ? '*' :
                    row.getPassengerNames()[j].charAt(0);
        }
        return initials;
    }
}
