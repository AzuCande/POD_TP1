package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.models.RowCategory;
import ar.edu.itba.pod.models.exceptions.seatExceptions.InvalidSeatException;
import ar.edu.itba.pod.models.exceptions.seatExceptions.NoAvailableSeatsException;

public class Row {
    private final RowCategory rowCategory;
    private final String[] passengerNames;

    public Row(RowCategory rowCategory, int seats) {
        this.rowCategory = rowCategory;
        this.passengerNames = new String[seats];
    }

    public boolean isAvailable(char seat) {
        return this.passengerNames[seat - 'A'] == null;
    }

    public void assignSeat(char seat, String passengerName) {
        this.passengerNames[seat - 'A'] = passengerName;
    }

    public void removePassenger(String passenger) {
        for (int i = 0; i < passengerNames.length; i++) {
            if (passengerNames[i] != null && passengerNames[i].equals(passenger)) {
                passengerNames[i] = null;
                return;
            }
        }
    }

    public void checkValidSeat(char seat) {
        if (seat < 'A' || seat >= 'A' + passengerNames.length) {
            throw new InvalidSeatException(seat);
        }
    }

    public RowCategory getRowCategory() {
        return rowCategory;
    }

    public String[] getPassengerNames() {
        return passengerNames;
    }
}