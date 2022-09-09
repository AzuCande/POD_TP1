package ar.edu.itba.pod.models.exceptions.seatExceptions;

public class NoAvailableSeatsException extends RuntimeException {

    @Override
    public String getMessage() {
        return "There are no available seats";
    }
}
