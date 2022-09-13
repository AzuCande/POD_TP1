package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.models.FlightState;
import ar.edu.itba.pod.models.PlaneModel;
import ar.edu.itba.pod.models.RowCategory;
import ar.edu.itba.pod.models.Ticket;
import ar.edu.itba.pod.models.exceptions.IllegalPassengerCategoryException;
import ar.edu.itba.pod.models.exceptions.IllegalRowException;
import ar.edu.itba.pod.models.exceptions.PassengerAlreadySeatedException;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.TicketNotFoundException;
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

    private final Lock stateLock = new ReentrantLock();

    private final Lock seatsLock = new ReentrantLock();

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
        return state;
    }

    public void setState(FlightState state) {
        this.state = state;
    }

    public boolean checkSeat(int row, char seat) {
        checkValidRow(row);
        return rows[row].isAvailable(seat);
    }

    public void assignSeat(int rowNumber, char seat, String passenger) {
        Ticket ticket = checkValidSeat(passenger, rowNumber, seat);

        if (ticket.isSeated()) {
            throw new PassengerAlreadySeatedException();
        }

        seatPassenger(rowNumber, seat, ticket);
    }

    public void changeSeat(int freeRow, char freeSeat, String passenger) {
        Ticket ticket = checkValidSeat(passenger, freeRow, freeSeat);
        Row oldRow = rows[ticket.getRow()];
        oldRow.removePassenger(passenger);
        ticket.setSeat(null, null);
        availableSeats[oldRow.getRowCategory().ordinal()]++;
        seatPassenger(freeRow, freeSeat, ticket);
    }

    private Ticket checkValidSeat(String passenger, int row, char seat) {
        if (!tickets.containsKey(passenger))
            throw new TicketNotFoundException();

        checkValidRow(row);
        Row newRow = rows[row];
        newRow.checkValidSeat(seat);
        if (!newRow.isAvailable(seat))
            throw new SeatAlreadyTakenException(row, seat);

        Ticket ticket = getTicket(passenger);

        if (rows[row].getRowCategory().ordinal() > ticket.getCategory().ordinal()) {
            throw new IllegalPassengerCategoryException();
        }

        return ticket;
    }

    private void seatPassenger(int rowNumber, char seat, Ticket ticket) {
        Row row = rows[rowNumber];
        row.assignSeat(seat, ticket.getPassenger());
        ticket.setSeat(rowNumber, seat);
        availableSeats[row.getRowCategory().ordinal()]--;
    }

    private void checkValidRow(int row) {
        if (row < 0 || row >= rows.length) {
            throw new IllegalRowException(row);
        }
    }

    public void changeFlight(String passenger, Flight other) {
        Ticket ticket = tickets.remove(passenger);
        if (ticket.isSeated()) {
            int row = ticket.getRow();
            rows[row].removePassenger(passenger);
            availableSeats[rows[row].getRowCategory().ordinal()]++;
        }
        ticket.setSeat(null, null);

        other.getTickets().put(passenger, ticket);
    }



    public int getAvailableByCategory(RowCategory category) {
        int toReturn;
        stateLock.lock();
        seatsLock.lock();
        toReturn = availableSeats[category.ordinal()];
        seatsLock.unlock();
        stateLock.unlock();

        return toReturn;

    }

    public int getAvailableCategory(RowCategory category) {
        int toReturn = -1;
        stateLock.lock();
        seatsLock.lock();
        for (int i = category.ordinal(); i >= 0; i--) {
            if (availableSeats[i] > 0) {
                toReturn = i;
                break;
            }
        }
        seatsLock.unlock();
        stateLock.unlock();
        return toReturn;
    }

    public Ticket getTicket(String passenger) {
        return tickets.get(passenger);
    }

    public Row[] getRows() {
        return rows;
    }

    public Lock getStateLock() {
        return stateLock;
    }

    public Lock getSeatsLock() {
        return seatsLock;
    }
}
