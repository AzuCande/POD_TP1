package ar.edu.itba.pod.models.exceptions.planeExceptions;

public class ModelAlreadyExistsException extends RuntimeException{

    private final String model;

    public ModelAlreadyExistsException(String model) {
        super();
        this.model = model;
    }

    @Override
    public String getMessage() {
        return "Model " + model + " already exists";
    }
}
