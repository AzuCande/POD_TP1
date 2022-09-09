package ar.edu.itba.pod.models.exceptions.seatExceptions;

public class InvalidSeatException extends RuntimeException {
    private final char seat;

    public InvalidSeatException(char seat) {
        super();
        this.seat = seat;
    }

    @Override
    public String getMessage() {
        return "Seat" + seat + " does not exists";
    }
}
