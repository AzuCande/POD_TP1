package ar.edu.itba.pod.models;

import java.io.Serializable;

public class CancelledTicket implements Serializable {

    private final String flightCode;
    private final String passenger;

    public CancelledTicket(String flightCode, String passenger) {
        this.flightCode = flightCode;
        this.passenger = passenger;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public String getPassenger() {
        return passenger;
    }
}
