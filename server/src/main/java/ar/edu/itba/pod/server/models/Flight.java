package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.models.FlightState;
import ar.edu.itba.pod.models.PlaneModel;
import ar.edu.itba.pod.models.RowCategory;
import ar.edu.itba.pod.models.Ticket;
import ar.edu.itba.pod.models.exceptions.IllegalPassengerCategoryException;
import ar.edu.itba.pod.models.exceptions.IllegalRowException;
import ar.edu.itba.pod.models.exceptions.PassengerAlreadySeatedException;
import ar.edu.itba.pod.models.exceptions.PassengerNotSeatedException;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.TicketNotFoundException;
import ar.edu.itba.pod.models.exceptions.planeExceptions.IllegalPlaneStateException;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class Flight {
    private final String code;
    private final String destination;
    private FlightState state = FlightState.PENDING;

    private final List<Ticket> tickets;

    private final Row[] rows;

    private final int[] availableSeats = {0, 0, 0};

    private final Lock flightLock = new ReentrantLock();

    public Flight(PlaneModel model, String code, String destination, List<Ticket> tickets) {
        this.code = code;
        this.destination = destination;
        this.tickets = tickets;

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

    public List<Ticket> getTickets() {
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

    public void assignSeat(int rowNumber, char seat, Ticket ticket) { // TODO: concurrencia
        checkValidRow(rowNumber);

        flightLock.lock();
        Row row = rows[rowNumber];
        row.checkValidSeat(seat);
        row.checkSeatAvailable(seat);

        try { // TODO: granularidad
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

        synchronized (ticket) {
            ticket.setSeat(rowNumber, seat);
        }

        availableSeats[row.getRowCategory().ordinal()]--;
    }

    private void checkValidRow(int row) {
        if (row < 0 || row >= rows.length) {
            throw new IllegalRowException(row);
        }
    }

    public void changeFlight(String passenger, Flight other) {
        final Flight[] locks = Stream.of(this, other)
                .sorted(Comparator.comparing(Flight::getCode))
                .toArray(Flight[]::new);

        Ticket ticket;
        synchronized (locks[0]) {
            synchronized (locks[1]) {
                ticket = getTicket(passenger);
                this.getTickets().remove(ticket); // TODO: check
                other.getTickets().add(ticket);
            }
        }
    }

    /*
    public void findSeat(Ticket ticket) {
        plane.findSeat(ticket);
        tickets.add(ticket);
    }

    public void removeTicket(Ticket ticket) {
        plane.removeTicket(ticket);
        tickets.remove(ticket);
    }

     */

    public void changeSeat(int freeRow, char freeSeat, String passenger) {
        Ticket ticket = tickets.stream().filter(t -> t.getPassenger().equals(passenger))
                .findFirst().orElseThrow(TicketNotFoundException::new);

        checkValidRow(freeRow);

        flightLock.lock();

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
                    return;
                }
            }
        } finally {
            flightLock.unlock();
        }

        throw new PassengerNotSeatedException();
    }

    public int getAvailableByCategory(RowCategory category) {
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

    public int getAvailableSeats(RowCategory category) {
        for (int i = category.ordinal(); i >= 0; i--) {
            if (availableSeats[i] > 0) return i;
        }
        return -1;
    }

    public Ticket getTicket(String passenger){
        return tickets.stream().filter(t -> t.getPassenger().equals(passenger))
                .findFirst().orElseThrow(TicketNotFoundException::new);
    }

    public Row[] getRows() {
        return rows;
    }
}
