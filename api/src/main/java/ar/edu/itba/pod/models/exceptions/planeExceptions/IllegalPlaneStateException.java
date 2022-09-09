package ar.edu.itba.pod.models.exceptions.planeExceptions;

public class IllegalPlaneStateException extends IllegalStateException{
    @Override
    public String getMessage() {
        return "Plane is not in pending state";
    }
}
