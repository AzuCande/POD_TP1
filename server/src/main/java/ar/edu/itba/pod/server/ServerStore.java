package ar.edu.itba.pod.server;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.models.*;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.FlightNotFoundException;
import ar.edu.itba.pod.server.models.Flight;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerStore {
    private final Map<String, PlaneModel> planeModels = new HashMap<>();
    private final Map<String, FlightState> flightCodes = new HashMap<>();
    private final Map<String, Flight> pendingFlights = new HashMap<>();
    private final Map<String, Flight> confirmedFlights = new HashMap<>();
    private final Map<String, Flight> cancelledFlights = new HashMap<>();

    /**
     * Mapa de Flight Code a Mapa de Passenger a Lista de Handlers
     */
    private final Map<String, Map<String, List<NotificationHandler>>> notifications = new HashMap<>();

    private final Lock notificationsLock = new ReentrantLock();

    private final ExecutorService executor = Executors.newCachedThreadPool();


    public Map<String, PlaneModel> getPlaneModels() {
        return planeModels;
    }


    /**
     * Registers a user to be notified.
     * It does not use concurrent collections because they don't guarantee
     * to read the latest value
     */
    public void registerUser(Notification notification, String passenger, NotificationHandler handler) {
        Map<String, List<NotificationHandler>> flightNotifications;
        notificationsLock.lock();
        flightNotifications = notifications.computeIfAbsent(notification.getOldCode(),
                k -> new HashMap<>());
        notificationsLock.unlock();

        List<NotificationHandler> passengerNotifications;
        synchronized (flightNotifications) { // TODO: preguntar lo de sync sobre local variable
            passengerNotifications = flightNotifications
                    .computeIfAbsent(passenger, k -> new ArrayList<>());
        }
        synchronized (passengerNotifications) {
            passengerNotifications.add(handler);
        }

        executor.submit(() -> {
            try {
                handler.notifyRegister(notification);//, category, row, col);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
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

    public Map<String, Flight> getFlightsByState(FlightState state) {
        switch (state) {
            case PENDING:
                return pendingFlights;
            case CONFIRMED:
                return confirmedFlights;
            case CANCELED:
                return cancelledFlights;
        }
        return Collections.emptyMap();
    }

    public Map<String, Flight> getPendingFlights() {
        return pendingFlights;
    }

    public Map<String, Flight> getConfirmedFlights() {
        return confirmedFlights;
    }

    public Map<String, Flight> getCancelledFlights() {
        return cancelledFlights;
    }

    public Flight getFlight(String flightCode) {
        synchronized (flightCodes) {
            FlightState state = Optional.ofNullable(getFlightCodes().get(flightCode))
                    .orElseThrow(FlightNotFoundException::new);

            synchronized (getFlightsByState(state)) {
                return Optional.ofNullable(getFlightsByState(state).get(flightCode))
                        .orElseThrow(FlightNotFoundException::new);
            }
        }
    }

    public Map<String, FlightState> getFlightCodes() {
        return flightCodes;
    }
}