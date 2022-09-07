package ar.edu.itba.pod.server;

import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.PlaneModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ServerStore {
//    private final Set<PlaneModel> planeModels = ConcurrentHashMap.newKeySet(); // TODO chequear que garantiza exactamente. Ver si dejamos la interfaz Set

    private final Map<String, PlaneModel> planeModels = new HashMap<>();
    private final Map<String, Flight> flights = new HashMap<>();
    private final Set<String> airports = ConcurrentHashMap.newKeySet(); //TODO : deprecated?


    public Map<String, PlaneModel> getPlaneModels() {
        return planeModels;
    }

    public Map<String, Flight> getFlights() {
        return flights;
    }

    public Set<String> getAirports() {
        return airports;
    }
}
