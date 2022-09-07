package ar.edu.itba.pod.models;

import java.util.Map;

public class SeatResponse {

    private String flightCode;
    private String destination;
    private Map<RowCategory, Integer> availableSeats;

    //    |01A*|01B*|01C*| BUSINESS
    //    |02A*|02BJ|02C*| BUSINESS
    //    |03A*|03B*|03C*| PREMIUM_ECONOMY
}
