package ar.edu.itba.pod.models;

import java.io.Serializable;
import java.util.Map;
/**
 * JFK | AA101 | 7 BUSINESS
 * JFK | AA119 | 3 BUSINESS
 * JFK | AA103 | 18 PREMIUM_ECONOMY
 **/

public class AlternativeFlightResponse implements Serializable {
    private final String flightCode;
    private final String destination;
    private final Map<RowCategory, Integer> availableSeats;


    public AlternativeFlightResponse(String flightCode, String destination, Map<RowCategory, Integer> availableSeats) {
        this.flightCode = flightCode;
        this.destination = destination;
        this.availableSeats = availableSeats;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public String getDestination() {
        return destination;
    }

    public Map<RowCategory, Integer> getAvailableSeats() {
        return availableSeats;
    }
}
