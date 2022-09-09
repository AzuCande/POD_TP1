package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.models.FlightState;
import ar.edu.itba.pod.models.RowCategory;
import ar.edu.itba.pod.models.Ticket;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.TicketNotFoundException;

import java.util.List;

public class Flight {
    private final String code;
    private FlightState state = FlightState.PENDING;
    private final Plane plane;
    private final String destination;
    private final List<Ticket> tickets;

    public Flight(Plane plane, String code, String destination, List<Ticket> tickets) {
        this.plane = plane;
        this.code = code;
        this.destination = destination;
        this.tickets = tickets;
    }

    public Plane getPlane() {
        return plane;
    }

    public String getDestination() {
        return destination;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public String getCode() {
        return code;
    }

    public FlightState getState() {
        return state;
    }

    public void setState(FlightState state) {
        this.state = state;
    }


    public void findSeat(Ticket ticket) {
        plane.findSeat(ticket);
        tickets.add(ticket);
    }

    public void removeTicket(Ticket ticket) {
        plane.removeTicket(ticket);
        tickets.remove(ticket);
    }

    public void changeSeat(int freeRow, char freeSeat, String passenger) {
        Ticket ticket = tickets.stream().filter(t -> t.getPassenger().equals(passenger)).findFirst()
                .orElseThrow(TicketNotFoundException::new);

        plane.changeSeat(freeRow, freeSeat, ticket);

    }

    public int getAvailableSeats(RowCategory category) {
        int[] availableSeats = getPlane().getAvailableSeats();
        for (int i = category.ordinal(); i >= 0; i--) {
            if (availableSeats[i] > 0)
                return i;
        }
        return -1;
    }
}
