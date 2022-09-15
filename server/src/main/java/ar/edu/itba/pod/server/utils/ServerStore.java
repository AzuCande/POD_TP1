package ar.edu.itba.pod.server.utils;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.models.*;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.FlightNotFoundException;
import ar.edu.itba.pod.server.models.Flight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

public class ServerStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerStore.class);
    private final Map<String, PlaneModel> planeModels = new HashMap<>();
    private final Map<String, FlightState> flightCodes = new HashMap<>();
    private final Map<String, Flight> pendingFlights = new HashMap<>();
    private final Map<String, Flight> confirmedFlights = new HashMap<>();
    private final Map<String, Flight> cancelledFlights = new HashMap<>();

    /**
     * Map of Flight Code to Map of Passenger to handlers List
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
    public void registerUser(Notification notification, String passenger,
                             List<NotificationHandler> handlers) {

        if (handlers.isEmpty())
            return;

        Map<String, List<NotificationHandler>> flightNotifications = computeFlightNotifications(
                notification.getOldCode());

        List<NotificationHandler> passengerNotifications;
        synchronized (flightNotifications) {
            passengerNotifications = flightNotifications
                    .computeIfAbsent(passenger, k -> new ArrayList<>());
        }

        synchronized (passengerNotifications) {
            passengerNotifications.addAll(handlers);
        }

        submitNotificationTask(() -> handlers.forEach(handler -> {
            try {
                handler.notifyRegister(notification);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public void submitNotificationTask(Runnable task) {
        executor.submit(task);
    }

    public Map<String, Map<String, List<NotificationHandler>>> getNotifications() {
        return notifications;
    }

    public Map<String, List<NotificationHandler>> getFlightNotifications(String flightCode) {
        return lockFlightNotifications(() -> notifications.get(flightCode));
    }

    public Map<String, List<NotificationHandler>> computeFlightNotifications(String flightCode) {
        return lockFlightNotifications(() -> notifications.computeIfAbsent(flightCode, k -> new HashMap<>()));
    }


    public List<NotificationHandler> getHandlers(String flightCode, String passenger) {
        return lockHandlers(flightCode, notifications -> notifications.get(passenger));
    }

    public List<NotificationHandler> popHandlers(String flightCode, String passenger) {
        return lockHandlers(flightCode, notifications -> notifications.remove(passenger));
    }

    public void removeFlightNotifications(String flightCode) {
        notificationsLock.lock();
        notifications.remove(flightCode);
        notificationsLock.unlock();
    }

    private List<NotificationHandler> lockHandlers(
            String flightCode,
            Function<Map<String, List<NotificationHandler>>, List<NotificationHandler>> getter) {

        Map<String, List<NotificationHandler>> flightNotifications = getFlightNotifications(flightCode);

        if (flightNotifications == null)
            return Collections.emptyList();

        List<NotificationHandler> notificationHandlers;

        synchronized (flightNotifications) {
            notificationHandlers = getter.apply(flightNotifications);
        }

        if (notificationHandlers == null)
            return Collections.emptyList();

        return notificationHandlers;
    }

    private Map<String, List<NotificationHandler>> lockFlightNotifications(Supplier<Map<String, List<NotificationHandler>>> fn) {
        Map<String, List<NotificationHandler>> toReturn;
        notificationsLock.lock();

        toReturn = fn.get();

        notificationsLock.unlock();
        return toReturn;
    }

    public void changeTicketsNotification(String passenger, Notification notification) {
        List<NotificationHandler> notificationHandlers = popHandlers(notification.getOldCode(), passenger);

        synchronized (notificationHandlers) {
            notificationHandlers.forEach(handler -> submitNotificationTask(() -> {
                try {
                    handler.notifyChangeTicket(notification);
                } catch (RemoteException e) {
                    LOGGER.error("Error notifying change ticket", e);
                }
            }));
        }

        registerUser(new Notification(notification.getNewCode(),
                notification.getDestination()), passenger, notificationHandlers);
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