package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.interfaces.SeatQueryService;
import ar.edu.itba.pod.models.FlightState;
import ar.edu.itba.pod.models.ResponseRow;
import ar.edu.itba.pod.models.RowCategory;
import ar.edu.itba.pod.models.exceptions.IllegalRowException;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.FlightNotFoundException;
import ar.edu.itba.pod.server.models.Flight;
import ar.edu.itba.pod.server.models.Row;
import ar.edu.itba.pod.server.utils.ServerStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.rmi.RemoteException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SeatQueryServiceImpl implements SeatQueryService {
    private final ServerStore store;
    private static final Logger LOGGER = LoggerFactory.getLogger(SeatQueryServiceImpl.class);

    public SeatQueryServiceImpl(ServerStore store) {
        this.store = store;
    }

    @Override
    public List<ResponseRow> query(String flightCode) throws RemoteException {
        List<ResponseRow> toReturn = createResponse(flightCode, f -> Arrays.asList(f.getRows()));
        LOGGER.info("Seat map query made for flight " + flightCode);
        return toReturn;
    }

    @Override
    public List<ResponseRow> query(String flightCode, RowCategory rowCategory) throws RemoteException {
        List<ResponseRow> toReturn = createResponse(flightCode, (flight) -> Arrays.stream(flight
                        .getRows()).filter(row -> row.getRowCategory() == rowCategory)
                .collect(Collectors.toList()));
        LOGGER.info("Seat map query made for " + rowCategory + " on flight " + flightCode);
        return toReturn;
    }

    @Override
    public ResponseRow query(String flightCode, int rowNum) throws RemoteException {
        Flight flight = getFlight(flightCode);
        flight.getSeatsLock().lock();

        List<ResponseRow> toReturn = createResponse(flightCode, f -> Collections.singletonList(Optional
                .ofNullable(f.getRows()[rowNum])
                .orElseThrow(() -> new IllegalRowException(rowNum))));
        LOGGER.info("Seat map query made for row " + rowNum + " on flight " + flightCode);
        return toReturn.stream().findFirst().orElseThrow(() -> new IllegalRowException(rowNum));
    }

    public List<ResponseRow> createResponse(String flightCode, Function<Flight, List<Row>> supplier) {
        Flight flight = getFlight(flightCode);
        List<ResponseRow> responseRows = new ArrayList<>();
        flight.getSeatsLock().lock();
        try {
            List<Row> rows = supplier.apply(flight);
            for (Row row : rows) {
                responseRows.add(new ResponseRow(row.getRowCategory(), getPassengerInitials(row)));
            }
        } finally {
            flight.getSeatsLock().unlock();
        }
        return responseRows;
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