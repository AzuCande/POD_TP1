package ar.edu.itba.pod.server.model;

public class Flight {
    private final String code;
    private FlightState state = FlightState.PENDING;
    private final Plane plane;
    private final String destination;

    public Flight(String code, Plane plane, String destination) {
        this.code = code;
        this.plane = plane;
        this.destination = destination;
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
