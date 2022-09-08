package ar.edu.itba.pod.server;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.models.Flight;
import ar.edu.itba.pod.models.PlaneModel;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerStore {
    private final Map<String, PlaneModel> planeModels = new HashMap<>();
    private final Map<String, Flight> flights = new HashMap<>();

    /**
     * Mapa de Flight Code a Mapa de Passenger a Lista de Handlers
     */
    private final Map<String, Map<String, List<NotificationHandler>>> notifications = new HashMap<>();

    private final Lock flightsLock = new ReentrantLock();

    private final Lock notificationsLock = new ReentrantLock();

    private final ExecutorService executor = Executors.newCachedThreadPool();


    public Map<String, PlaneModel> getPlaneModels() {
        return planeModels;
    }

    public Map<String, Flight> getFlights() {
        return flights;
    }

    public Lock getFlightsLock() {
        return flightsLock;
    }

    /**
     * Registers a user to be notified.
     * It does not use concurrent collections because they don't guarantee
     * to read the latest value
     */
    public void registerUser(String flightCode, String passenger, NotificationHandler handler) {
        notificationsLock.lock();

        notifications.computeIfAbsent(flightCode, k -> new HashMap<>())
                        .computeIfAbsent(passenger, k -> new ArrayList<>()).add(handler);

        notificationsLock.unlock();

        executor.submit(() -> {
            try {
                // TODO: buscar ticket con asientos
                handler.notifyRegister(flightCode, flights.get(flightCode).getDestination(),
                        null, -1, -1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        // TODO: https://www.codejava.net/java-core/concurrency/java-concurrent-collection-copyonwritearraylist-examples#:~:text=The%20CopyOnWriteArrayList%20class%20uses%20a%20mechanism%20called%20copy-on-write,iterator%2C%20listIterator%2C%20etc%29%20work%20on%20a%20different%20copy.
        // verificar si esa implementación es thread safe
    }

    public void submitNotificationTask(Runnable task) {
        executor.submit(task);
    }

    public Map<String, Map<String, List<NotificationHandler>>> getNotifications() {
        return notifications;
    }

    public Lock getNotificationsLock() {
        return notificationsLock;
    }
}
