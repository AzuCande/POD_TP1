package ar.edu.itba.pod.server;

import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.PlaneModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerStore {
//    private final Set<PlaneModel> planeModels = ConcurrentHashMap.newKeySet(); // TODO chequear que garantiza exactamente. Ver si dejamos la interfaz Set

    private final Map<String, PlaneModel> planeModels = new HashMap<>();
    private final Map<String, Flight> flights = new HashMap<>();

    final Lock flightsLock = new ReentrantLock();


    public Map<String, PlaneModel> getPlaneModels() {
        return planeModels;
    }

    public Map<String, Flight> getFlights() {
        return flights;
    }

    public Lock getFlightsLock() {
        return flightsLock;
    }
}
