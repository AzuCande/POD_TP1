package ar.edu.itba.pod.models.exceptions.flightExceptions;

public class IllegalFlightException extends IllegalArgumentException{
    @Override
    public String getMessage() {
        return "The plane configuration is invalid";
    }
}
