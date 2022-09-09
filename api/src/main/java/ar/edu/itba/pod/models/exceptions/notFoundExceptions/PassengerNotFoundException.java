package ar.edu.itba.pod.models.exceptions.notFoundExceptions;

public class PassengerNotFoundException extends NotFoundException{
    @Override
    public String getMessage() {
        return "Passenger not found";
    }
}
