package ar.edu.itba.pod.models.exceptions;

public class PassengerAlreadySeatedException extends IllegalStateException {

    @Override
    public String getMessage() {
        return "Passenger already has a seat assigned";
    }
}
