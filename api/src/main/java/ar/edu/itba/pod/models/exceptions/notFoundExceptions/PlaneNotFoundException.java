package ar.edu.itba.pod.models.exceptions.notFoundExceptions;

public class PlaneNotFoundException extends NotFoundException{
    @Override
    public String getMessage(){
        return "Plane not found";
    }
}
