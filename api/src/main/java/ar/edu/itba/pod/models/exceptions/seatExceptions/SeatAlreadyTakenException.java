package ar.edu.itba.pod.models.exceptions.seatExceptions;

public class SeatAlreadyTakenException extends RuntimeException {
    private char seat;

    public SeatAlreadyTakenException(char seat) {
        this.seat = seat;
    }

    @Override
    public String getMessage() {
        return "Seat " + seat + " is already taken";
    }
}
