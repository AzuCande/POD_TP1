package ar.edu.itba.pod.models.exceptions.flightExceptions;

public class FlightAlreadyExistsException extends IllegalStateException {

    @Override
    public String getMessage() {
        return "Flight already exists";
    }

}
