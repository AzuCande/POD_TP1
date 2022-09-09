package ar.edu.itba.pod.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlightResponse implements Serializable {
    private String flightCode;
    private String destination;
    private Map<RowCategory, Integer> availableSeats;


    public FlightResponse(String flightCode, String destination, Map<RowCategory, Integer> availableSeats){
        this.flightCode = flightCode;
        this.destination = destination;
        this.availableSeats = availableSeats;
    }

    //      JFK | AA101 | 7 BUSINESS
    //      JFK | AA119 | 3 BUSINESS
    //      JFK | AA103 | 18 PREMIUM_ECONOMY

    /*
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

     */



//        StringBuilder stringBuilder = new StringBuilder();
//        for (Flight f : alternativeFlights) {
//            stringBuilder.append(f.getDestination()).append(" | ");
//            stringBuilder.append(f.getCode()).append(" | ");
//            for (int category = ticket.getCategory().ordinal();
//                 category >= 0; category--) {
//                RowCategory cat = RowCategory.values()[category];
//                stringBuilder.append(f.getAvailableSeats(cat)).append(" ").append(cat).append('\n');
//            }
//
//        }

}
