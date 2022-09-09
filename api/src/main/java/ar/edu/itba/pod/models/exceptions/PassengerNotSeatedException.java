package ar.edu.itba.pod.models.exceptions;

public class PassengerNotSeatedException extends IllegalStateException{
    @Override
    public String getMessage() {
        return "Passenger does not have a seat assigned";
    }
}
