package ar.edu.itba.pod.models;

import java.util.Map;

public class FlightResponse {

    private String flightCode;
    private String destination;
    private Map<RowCategory, Integer> availableSeats;


    FlightResponse(String flightCode, String destination, Map<RowCategory, Integer> availableSeats){
        this.flightCode = flightCode;
        this.destination = destination;
        this.availableSeats = availableSeats;
    }

    //      JFK | AA101 | 7 BUSINESS
    //      JFK | AA119 | 3 BUSINESS
    //      JFK | AA103 | 18 PREMIUM_ECONOMY



}
