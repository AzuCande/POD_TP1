package ar.edu.itba.pod.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public static List<FlightResponse> compactFlights(List<Flight> flights){
        List<FlightResponse> toRet = new ArrayList<>();
        for(Flight flight : flights){
            Map<RowCategory, Integer> categories = new HashMap<>();
            categories.put(RowCategory.ECONOMY, flight.getAvailableSeats(RowCategory.ECONOMY));
            categories.put(RowCategory.PREMIUM_ECONOMY, flight.getAvailableSeats(RowCategory.PREMIUM_ECONOMY));
            categories.put(RowCategory.BUSINESS, flight.getAvailableSeats(RowCategory.BUSINESS));
            toRet.add(new FlightResponse(flight.getCode(), flight.getDestination(), categories));
        }
        return toRet;
    }



}
