package ar.edu.itba.pod.models.exceptions.planeExceptions;

public class IllegalPlaneStateException extends IllegalStateException {
    // TODO: refactor extends
    @Override
    public String getMessage() {
        return "Plane is not in pending state";
    }
}
