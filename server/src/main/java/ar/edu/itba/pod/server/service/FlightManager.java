package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.server.model.FlightState;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FlightManager {
    Set<String> planeModels = ConcurrentHashMap.newKeySet(); // TODO chequear que garantiza exactamente
    Set<String> flightCodes = ConcurrentHashMap.newKeySet();
    Set<String> airports = ConcurrentHashMap.newKeySet();

    public void addPlaneModel(){
        //consume CSV with plane models
    }

    public void addFlight(){
        //consume CSV with flights
    }

    public FlightState getFlightState(String flight) {
        return null;
    }

    public void setFlightState(FlightState newState){

    }

    public void listAlternativeFlights() {

    }
}
