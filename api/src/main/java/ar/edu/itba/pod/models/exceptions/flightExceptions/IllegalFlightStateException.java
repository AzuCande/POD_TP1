package ar.edu.itba.pod.models.exceptions.flightExceptions;

public class IllegalFlightStateException extends IllegalStateException {
    @Override
    public String getMessage() {
        return "Plane is not in pending state";
    }
}
