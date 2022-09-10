package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.models.FlightState;
import ar.edu.itba.pod.models.PlaneModel;
import ar.edu.itba.pod.models.RowCategory;
import ar.edu.itba.pod.models.Ticket;
import ar.edu.itba.pod.models.exceptions.IllegalPassengerCategoryException;
import ar.edu.itba.pod.models.exceptions.IllegalRowException;
import ar.edu.itba.pod.models.exceptions.PassengerAlreadySeatedException;
import ar.edu.itba.pod.models.exceptions.planeExceptions.IllegalPlaneStateException;
import ar.edu.itba.pod.models.exceptions.seatExceptions.SeatAlreadyTakenException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Flight {
    private final String code;
    private final String destination;
    private FlightState state = FlightState.PENDING;

    private final Map<String, Ticket> tickets;

    private final Row[] rows;

    private final int[] availableSeats = {0, 0, 0};

    private final Lock flightLock = new ReentrantLock();

    public Flight(PlaneModel model, String code, String destination, List<Ticket> tickets) {
        this.code = code;
        this.destination = destination;
        this.tickets = tickets.stream().collect(Collectors.toMap(Ticket::getPassenger, t -> t));

        int[] business = model.getCategoryConfig(RowCategory.BUSINESS);
        int[] premium = model.getCategoryConfig(RowCategory.PREMIUM_ECONOMY);
        int[] economy = model.getCategoryConfig(RowCategory.ECONOMY);

        int totRows = business[0] + premium[0] + economy[0];

        this.rows = new Row[totRows];
        int iter = 0;
        for (int i = 0; i < business[0]; iter++, i++) {
            rows[iter] = new Row(RowCategory.BUSINESS, business[1]);
        }

        for (int i = 0; i < premium[0]; iter++, i++) {
            rows[iter] = new Row(RowCategory.PREMIUM_ECONOMY, premium[1]);
        }

        for (int i = 0; i < economy[0]; iter++, i++) {
            rows[iter] = new Row(RowCategory.ECONOMY, economy[1]);
        }

        availableSeats[RowCategory.BUSINESS.ordinal()] = business[0] * business[1];
        availableSeats[RowCategory.PREMIUM_ECONOMY.ordinal()] = premium[0] * premium[1];
        availableSeats[RowCategory.ECONOMY.ordinal()] = economy[0] * economy[1];
    }

    public String getDestination() {
        return destination;
    }

    public String getCode() {
        return code;
    }

    public Map<String, Ticket> getTickets() {
        return tickets;
    }

    public FlightState getState() {
        flightLock.lock();
        try {
            return state;
        } finally {
            flightLock.unlock();
        }
    }

    public void setState(FlightState state) {
        flightLock.lock();
        this.state = state;
        flightLock.unlock();
    }

    public boolean checkSeat(int row, char seat) {
        checkValidRow(row);
        flightLock.lock();
        try {
            if (state != FlightState.PENDING) {
                throw new IllegalPlaneStateException();
            }
            return rows[row].isAvailable(seat);
        } finally {
            flightLock.unlock();
        }
    }

    public void assignSeat(int rowNumber, char seat, Ticket ticket) {
        checkValidRow(rowNumber);

        flightLock.lock(); // TODO poner el informe que se podria ser mas granular y solo lockear por row
        // IGUAL no se podria ya que podrian cambiar el estado
        Row row = rows[rowNumber];
        try {
            row.checkValidSeat(seat);
            if (!row.isAvailable(seat))
                throw new SeatAlreadyTakenException(seat);// TODO habria que pasarle row tambien

            if (state != FlightState.PENDING) {
                throw new IllegalPlaneStateException();
            }

            if (rows[rowNumber].getRowCategory().ordinal() < ticket.getCategory().ordinal()) {
                throw new IllegalPassengerCategoryException();
            }

            for (Row r : rows) {
                if (r.passengerHasSeat(ticket.getPassenger())) {
                    throw new PassengerAlreadySeatedException();
                }
            }
            seatPassenger(rowNumber, seat, ticket);

        } finally {
            flightLock.unlock();
        }
    }

    private void seatPassenger(int rowNumber, char seat, Ticket ticket) {
        Row row = rows[rowNumber];
        row.assignSeat(seat, ticket.getPassenger()); //TODO: tira exception si estás pisando a alguien en un asiento

        ticket.setSeat(rowNumber, seat);

        availableSeats[row.getRowCategory().ordinal()]--;
    }

    private void checkValidRow(int row) {
        if (row < 0 || row >= rows.length) {
            throw new IllegalRowException(row);
        }
    }

    public void changeFlight(String passenger, Flight other) {
        Ticket ticket = getTicket(passenger);
        tickets.remove(passenger);
        other.getTickets().put(passenger, ticket);
    }

    public void changeSeat(int freeRow, char freeSeat, String passenger) {
        checkValidRow(freeRow);
        flightLock.lock();

        Ticket ticket = getTicket(passenger);
        try {
            if (state != FlightState.PENDING) {
                throw new IllegalPlaneStateException();
            }
            for (Row row : rows) {
                if (row.passengerHasSeat(ticket.getPassenger())) {
                    if (row.getRowCategory().ordinal() < ticket.getCategory().ordinal())
                        throw new IllegalPassengerCategoryException();

                    row.removePassenger(ticket.getPassenger());
                    availableSeats[row.getRowCategory().ordinal()]++;
                    seatPassenger(freeRow, freeSeat, ticket);
                    break;
                }
            }
        } finally {
            flightLock.unlock();
        }
    }

    public int getAvailableByCategory(RowCategory category) {
        int toReturn;

        flightLock.lock();
        toReturn = availableSeats[category.ordinal()];
        flightLock.unlock();

        return toReturn;

    }

    public int getAvailableCategory(RowCategory category) {
        int toReturn = -1;
        flightLock.lock();
        for (int i = category.ordinal(); i >= 0; i--) {
            if (availableSeats[i] > 0) {
                toReturn = i;
                break;
            }
        }
        flightLock.unlock();
        return toReturn;
    }

    public Ticket getTicket(String passenger) {
        synchronized (tickets) {
            return tickets.get(passenger);
        }
    }

    public Row[] getRows() {
        return rows;
    }

    public Lock getFlightLock() {
        return flightLock;
    }
}
