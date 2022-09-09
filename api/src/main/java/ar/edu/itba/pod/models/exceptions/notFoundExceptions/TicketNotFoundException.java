package ar.edu.itba.pod.models.exceptions.notFoundExceptions;

import ar.edu.itba.pod.models.exceptions.notFoundExceptions.NotFoundException;

public class TicketNotFoundException extends NotFoundException {
    @Override
    public String getMessage() {
        return "Ticket was not found";
    }
}
