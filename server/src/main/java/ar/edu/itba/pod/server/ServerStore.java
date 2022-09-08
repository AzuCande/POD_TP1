package ar.edu.itba.pod.server;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.PlaneModel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerStore {
//    private final Set<PlaneModel> planeModels = ConcurrentHashMap.newKeySet(); // TODO chequear que garantiza exactamente. Ver si dejamos la interfaz Set

    private final Map<String, PlaneModel> planeModels = new HashMap<>();
    private final Map<String, Flight> flights = new HashMap<>();

    private final Map<String, Map<String, List<NotificationHandler>>> notifications = new ConcurrentHashMap<>();

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

    public void registerUser(String flightCode, String passenger, NotificationHandler handler) {
        notifications.putIfAbsent(flightCode, new ConcurrentHashMap<>());
        notifications.get(flightCode).putIfAbsent(passenger, new CopyOnWriteArrayList<>()); //TODO chequear la concurrencia
        notifications.get(flightCode).get(passenger).add(handler);
    }

    public Map<String, Map<String, List<NotificationHandler>>> getNotifications() {
        return notifications;
    }
}
