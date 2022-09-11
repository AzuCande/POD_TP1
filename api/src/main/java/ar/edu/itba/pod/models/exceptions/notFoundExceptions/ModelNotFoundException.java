package ar.edu.itba.pod.models.exceptions.notFoundExceptions;

public class ModelNotFoundException extends NotFoundException{
    @Override
    public String getMessage() {
        return "Model not found";
    }
}
