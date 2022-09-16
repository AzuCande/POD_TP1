package ar.edu.itba.pod.models.exceptions;

public class IllegalRowException extends IllegalArgumentException {

    private final int row;

    public IllegalRowException(int row) {
        super();
        this.row = row;
    }

    @Override
    public String getMessage() {
        return "Row " + row + " does not exist";
    }
}
