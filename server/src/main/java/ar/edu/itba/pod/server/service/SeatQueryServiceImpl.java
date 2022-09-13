package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.interfaces.SeatQueryService;
import ar.edu.itba.pod.models.*;
import ar.edu.itba.pod.models.exceptions.IllegalRowException;
import ar.edu.itba.pod.models.exceptions.flightExceptions.IllegalFlightStateException;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.FlightNotFoundException;
import ar.edu.itba.pod.server.utils.ServerStore;
import ar.edu.itba.pod.server.models.Flight;
import ar.edu.itba.pod.server.models.Row;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

public class SeatQueryServiceImpl implements SeatQueryService {

    private final ServerStore store;
    private static final Logger LOGGER = LoggerFactory.getLogger(SeatQueryServiceImpl.class);

    public SeatQueryServiceImpl(ServerStore store) {
        this.store = store;
    }

    @Override
    public ArrayList<ResponseRow> query(String flightCode) throws RemoteException {
        Flight flight = getFlight(flightCode);
        ArrayList<ResponseRow> responseRows = new ArrayList<>();
        flight.getSeatsLock().lock();
        Row[] rows = flight.getRows();
        for (Row row : rows) {
            responseRows.add(new ResponseRow(row.getRowCategory(), getPassengerInitials(row)));
        }
        flight.getSeatsLock().unlock();
        LOGGER.info("Seat map query made for flight " + flightCode);
        return responseRows;
    }

    @Override
    public ArrayList<ResponseRow> query(String flightCode, RowCategory rowCategory) throws RemoteException {
        Flight flight = getFlight(flightCode);
        ArrayList<ResponseRow> responseRows = new ArrayList<>();
        flight.getSeatsLock().lock();
        List<Row> rows = Arrays.stream(getFlight(flightCode).getRows())
                .filter(row -> row.getRowCategory() == rowCategory).collect(Collectors.toList());

        for (Row row : rows) {
            responseRows.add(new ResponseRow(row.getRowCategory(), getPassengerInitials(row)));
        }
        flight.getSeatsLock().unlock();
        LOGGER.info("Seat map query made for " + rowCategory + " on flight " + flightCode);
        return responseRows;
    }

    @Override
    public ResponseRow query(String flightCode, int rowNum) throws RemoteException {
        Flight flight = getFlight(flightCode);
        flight.getSeatsLock().lock();
        Row row;
        try {
            row = Optional.ofNullable(getFlight(flightCode).getRows()[rowNum])
                    .orElseThrow(() -> new IllegalRowException(rowNum));
        } finally {
            flight.getSeatsLock().unlock();
        }
        LOGGER.info("Seat map query made for row " + rowNum + " on flight " + flightCode);
        return new ResponseRow(row.getRowCategory(), getPassengerInitials(row));
    }

    private Flight getFlight(String flightCode) {
        synchronized (store.getFlightCodes()) {
            FlightState state = Optional.ofNullable(store.getFlightCodes().get(flightCode))
                    .orElseThrow(FlightNotFoundException::new);

            Map<String, Flight> flights = store.getFlightsByState(state);
            synchronized (flights) {
                return flights.get(flightCode);
            }
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