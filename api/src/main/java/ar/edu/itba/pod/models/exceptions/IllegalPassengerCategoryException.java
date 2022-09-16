package ar.edu.itba.pod.models.exceptions;

public class IllegalPassengerCategoryException extends IllegalArgumentException {

    @Override
    public String getMessage() {
        return "The passenger category is invalid";
    }
}
