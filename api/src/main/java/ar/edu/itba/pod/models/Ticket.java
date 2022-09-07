package ar.edu.itba.pod.models;

import ar.edu.itba.pod.models.RowCategory;

import java.util.Objects;

public class Ticket {
    private final RowCategory category;
    private final String passenger;
    private final String destination;

    public Ticket(RowCategory category, String passenger, String destination) {
        this.category = category;
        this.passenger = passenger;
        this.destination = destination;
    }

    public String getPassenger() {
        return passenger;
    }

    public RowCategory getCategory() {
        return category;
    }

    public String getDestination(){
        return destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return passenger.equals(ticket.passenger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(passenger);
    }
}
