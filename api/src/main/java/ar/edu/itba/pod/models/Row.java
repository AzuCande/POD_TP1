package ar.edu.itba.pod.models;

public class Row {
    private final RowCategory rowCategory;
    private final String[] seatOwners; // TODO modelo de esto en base a la concurrencia

    public Row(RowCategory rowCategory, int seats) {
        this.rowCategory = rowCategory;
        this.seatOwners = new String[seats];
    }

    public boolean isAvailable(char seat) {
        checkValidSeat(seat);
        return this.seatOwners[seat - 'A'] == null;
    }

    public boolean passengerHasSeat(String passengerName) {
        for (String seatOwner : seatOwners) {
            if (seatOwner != null && seatOwner.equals(passengerName)) {
                return true;
            }
        }

        return false;
    }

    public void assignSeat(char seat, String passenger) {//TODO: usar ticket?
        checkSeatAvailable(seat);

        this.seatOwners[seat - 'A'] = passenger;
    }

    public void removePassenger(String passenger) {
        for (int i = 0; i < seatOwners.length; i++) {
            if (seatOwners[i] != null && seatOwners[i].equals(passenger)) {
                seatOwners[i] = null;
                return;
            }
        }
    }

    private void checkValidSeat(char seat) {//TODO: usar ticket?
        if (seat < 'A' || seat >= 'A' + seatOwners.length) {
            throw new IllegalArgumentException("Seat " + seat + " does not exist");
        }
    }
    
    private void checkSeatAvailable(char seat) {//TODO: usar ticket?
        checkValidSeat(seat);
        if (!isAvailable(seat)) {
            throw new IllegalStateException("Seat already taken");
        }
    }

    public RowCategory getRowCategory() {
        return rowCategory;
    }
}