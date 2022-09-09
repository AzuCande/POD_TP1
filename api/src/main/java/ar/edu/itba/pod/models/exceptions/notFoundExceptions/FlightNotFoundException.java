package ar.edu.itba.pod.models.exceptions.notFoundExceptions;

public class FlightNotFoundException extends NotFoundException {
    @Override
    public String getMessage() {
        return "Flight not found";
    }
}
