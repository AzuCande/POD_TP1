package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.models.RowCategory;
import ar.edu.itba.pod.models.exceptions.seatExceptions.InvalidSeatException;
import ar.edu.itba.pod.models.exceptions.seatExceptions.NoAvailableSeatsException;
import ar.edu.itba.pod.models.exceptions.seatExceptions.SeatAlreadyTakenException;


public class Row { // TODO se usa en una interfaz remota?
    private final RowCategory rowCategory;
    private final String[] passengerNames; // TODO modelo de esto en base a la concurrencia

    public Row(RowCategory rowCategory, int seats) {
        this.rowCategory = rowCategory;
        this.passengerNames = new String[seats];
    }

    public boolean hasAvailableSeats() {
        for (String passengerName : passengerNames) {
            if (passengerName == null) {
                return true;
            }
        }

        return false;
    }

    public boolean isAvailable(char seat) {
        checkValidSeat(seat);
        return this.passengerNames[seat - 'A'] == null;
    }

    public void findSeat(String passenger) {
        for (int i = 0; i < passengerNames.length; i++) {
            if (passengerNames[i] == null) {
                passengerNames[i] = passenger;
                return;
            }
        }

        throw new NoAvailableSeatsException();
    }

    public boolean passengerHasSeat(String passengerName) {
        for (String passenger : passengerNames) {
            if (passenger != null && passenger.equals(passengerName)) {
                return true;
            }
        }

        return false;
    }

    public void assignSeat(char seat, String passengerName) {
        checkSeatAvailable(seat);

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
    
    public void checkSeatAvailable(char seat) {
        checkValidSeat(seat);
        if (!isAvailable(seat)) {
            throw new SeatAlreadyTakenException(seat);
        }
    }

    public RowCategory getRowCategory() {
        return rowCategory;
    }

    public String[] getPassengerNames() {
        return passengerNames;
    }
}