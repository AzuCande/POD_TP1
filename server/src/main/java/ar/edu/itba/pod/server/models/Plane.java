package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.models.FlightState;
import ar.edu.itba.pod.models.PlaneModel;
import ar.edu.itba.pod.models.RowCategory;
import ar.edu.itba.pod.models.Ticket;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Plane implements Serializable {
    private final PlaneModel model;
    private final Row[] rows;
    private final FlightState state = FlightState.PENDING;

    private final Lock seatLock = new ReentrantLock();
    private final int[] availableSeats = {0, 0, 0};

    public Plane(PlaneModel model) {
        this.model = model;

        int[] business = model.getCategoryConfig(RowCategory.BUSINESS);
        int[] premium = model.getCategoryConfig(RowCategory.PREMIUM_ECONOMY);
        int[] economy = model.getCategoryConfig(RowCategory.ECONOMY);

        int totRows = business[0] + premium[0] + economy[0];
        if (!validParams(business, premium, economy)) {
            throw new IllegalArgumentException(""); //TODO: crear nuestras excepciones
        }

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

    public void assignSeat(int rowNumber, char seat, Ticket ticket) { // TODO: concurrencia
        checkValidRow(rowNumber);

        if (state != FlightState.PENDING) {
            throw new IllegalStateException("Plane is not in pending state");
        }

        if (rows[rowNumber].getRowCategory().ordinal() < ticket.getCategory().ordinal()) {
            throw new IllegalArgumentException("Passenger category is not permited");
        }

        for (Row row : rows) {
            if (row.passengerHasSeat(ticket.getPassenger())) {
                throw new IllegalStateException("Passenger already has a seat");
            }
        }

        seatPassenger(rowNumber, seat, ticket);
    }

    public void changeSeat(int newRow, char newSeat, Ticket ticket) { // TODO: concurrencia
        checkValidRow(newRow);
        if (state != FlightState.PENDING) {
            throw new IllegalStateException("Plane is not in pending state");
        }

        for (Row row : rows) {
            if (row.passengerHasSeat(ticket.getPassenger())) {
                if (row.getRowCategory().ordinal() < ticket.getCategory().ordinal())
                    throw new IllegalArgumentException("Passenger category is not permited");

                row.removePassenger(ticket.getPassenger());
                availableSeats[row.getRowCategory().ordinal()]++;
                seatPassenger(newRow, newSeat, ticket);
                return;
            }
        }

        throw new IllegalStateException("Passenger does not have a seat");
    }

    public boolean checkSeat(int row, char seat) {
        checkValidRow(row);
        if (state != FlightState.PENDING) {
            throw new IllegalStateException("Plane is not in pending state");
        }
        return rows[row].isAvailable(seat);
    }

    public int[] getAvailableSeats() {
        return availableSeats;
    }

    public int getAvailableByCategory(RowCategory category) {
        int toReturn = -1;
        //seatLock.lock();

        for (int i = category.ordinal(); i >= 0; i--) {
            if (availableSeats[i] > 0) {
                toReturn = i;
                break;
            }
        }

        //seatLock.unlock();
        return toReturn;
    }

    private void checkValidRow(int row) {
        if (row < 0 || row >= rows.length) {
            throw new IllegalArgumentException("Row " + row + " does not exist");
        }
    }

    public PlaneModel getModel() {
        return model;
    }

    //TODO: Revisar
    private boolean validParams(int[] business, int[] premEconomy, int[] economy) {
        return business[0] >= 0 && business[1] >= 0 && premEconomy[0] >= 0 && premEconomy[1] >= 0 &&
                economy[0] >= 0 && economy[1] >= 0 && (business[0] + premEconomy[0] + economy[0]) > 0;
    }

    public Row[] getRows() {
        return rows;
    }

    private void seatPassenger(int rowNumber, char seat, Ticket ticket) {
        Row row = rows[rowNumber];
            row.assignSeat(seat, ticket.getPassenger()); //tira exception si estás pisando a alguien en un asiento
        
        synchronized (ticket) {
            ticket.setSeat(rowNumber, seat);
        }

        // TODO: sync
        availableSeats[row.getRowCategory().ordinal()]--;
    }

    public void findSeat(Ticket ticket) { // TODO: concurrencia
        for (Row row : rows) {
            if (row.getRowCategory().ordinal() >= ticket.getCategory().ordinal() && row.hasAvailableSeats()) {
                row.findSeat(ticket.getPassenger());
                availableSeats[row.getRowCategory().ordinal()]--;
                return;
            }
        }

        throw new IllegalStateException("No seats available for category " + ticket.getCategory());
    }

    public void removeTicket(Ticket ticket) { // FIXME: eficiente
        String passenger = ticket.getPassenger();
        for (Row row : rows) {
            if (row.passengerHasSeat(passenger)) {
                row.removePassenger(passenger);
                availableSeats[row.getRowCategory().ordinal()]++;
                return;
            }
        }
    }

    public Lock getSeatLock() {
        return seatLock;
    }
}
