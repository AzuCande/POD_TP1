package ar.edu.itba.pod.models;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Plane {
    private final PlaneModel model;
    private final Row[] rows;
    private final FlightState state = FlightState.PENDING; // TODO: deprecated

    private final Lock seatLock = new ReentrantLock();

    public Plane(PlaneModel model) {
        this.model = model;

        int[] business = model.getCategoryConfig(RowCategory.BUSINESS);
        int[] premium = model.getCategoryConfig(RowCategory.PREMIUM_ECONOMY);
        int[] economy = model.getCategoryConfig(RowCategory.ECONOMY);

        int totRows = business[0] + premium[0] + economy[0];
        if (validParams(business[0], business[1], premium[0], premium[1], economy[0], economy[1])) {
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

    }

    public void assignSeat(int rowNumber, char seat, String passengerName) { //TODO: usar ticket y no passenger
        checkValidRow(rowNumber);

        if (state != FlightState.PENDING) {
            throw new IllegalStateException("Plane is not in pending state");
        }
        //TODO : check if passenger category is permited

        //TODO: revisar
        this.seatLock.lock();
        for (Row row : rows) {
            if (row.passengerHasSeat(passengerName)) {
                throw new IllegalStateException("Passenger already has a seat");
            }
        }
        rows[rowNumber].assignSeat(seat, passengerName);
        this.seatLock.unlock();
    }

    public void changeSeat(int newRow, char newSeat, String passengerName) {//TODO: usar ticket y no passenger

        checkValidRow(newRow);
        if (state != FlightState.PENDING) {
            throw new IllegalStateException("Plane is not in pending state");
        }

        RowCategory prevRowCategory = null;
        for (Row row : rows) {
            if (row.passengerHasSeat(passengerName)) {
                row.removePassenger(passengerName);
                prevRowCategory = row.getRowCategory();
                break;
            }
        }

        if (prevRowCategory == null) {
            throw new IllegalStateException("Passenger does not have a seat");
        }

        // TODO: revisar, ticket tiene categoria
        if (prevRowCategory != rows[newRow].getRowCategory()) {
            throw new IllegalStateException("Passenger cannot change seat category");
        }

        //lock
        rows[newRow].assignSeat(newSeat, passengerName);
        //unlock
    }

    public boolean checkSeat(int row, char seat) {
        checkValidRow(row);
        if (state != FlightState.PENDING) {
            throw new IllegalStateException("Plane is not in pending state");
        }
        return rows[row].isAvailable(seat);
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
    private boolean validParams(int businessRows, int businessCols, int premEconomyRows, int premEconomyCols, int economyRows, int economyCols) {
        return (businessRows + premEconomyRows + economyRows > 0) && (businessRows > 0 && businessCols > 0 || premEconomyRows > 0 && premEconomyCols > 0 || economyRows > 0 && economyCols > 0);
    }

    public Row[] getRows() {
        return rows;
    }
}
