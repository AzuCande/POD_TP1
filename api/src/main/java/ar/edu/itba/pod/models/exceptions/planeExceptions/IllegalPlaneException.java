package ar.edu.itba.pod.models.exceptions.planeExceptions;

public class IllegalPlaneException extends IllegalArgumentException{
    @Override
    public String getMessage() {
        return "The plane configuration is invalid";
    }
}
