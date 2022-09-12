package ar.edu.itba.pod.models.exceptions.seatExceptions;

public class SeatAlreadyTakenException extends RuntimeException {
    private int row;
    private char seat;

    public SeatAlreadyTakenException(int row, char seat) {
        super();
        this.row = row;
        this.seat = seat;
    }

    @Override
    public String getMessage() {
        return "Seat " + row + " " + seat + " is already taken";
    }
}
