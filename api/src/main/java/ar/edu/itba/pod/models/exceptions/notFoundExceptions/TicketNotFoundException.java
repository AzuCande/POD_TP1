package ar.edu.itba.pod.models.exceptions.notFoundExceptions;

public class TicketNotFoundException extends NotFoundException {
    @Override
    public String getMessage() {
        return "Ticket was not found";
    }
}
