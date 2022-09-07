package ar.edu.itba.pod.models;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

}
