package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.interfaces.NotificationService;
import ar.edu.itba.pod.models.Notification;
import ar.edu.itba.pod.models.exceptions.PassengerNotSeatedException;
import ar.edu.itba.pod.models.exceptions.flightExceptions.IllegalFlightStateException;
import ar.edu.itba.pod.server.models.Flight;
import ar.edu.itba.pod.models.FlightState;
import ar.edu.itba.pod.server.utils.ServerStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class NotificationServiceImpl implements NotificationService {

    private final ServerStore store;
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationServiceImpl.class);

    public NotificationServiceImpl(ServerStore store) {
        this.store = store;
    }

    @Override
    public void registerPassenger(String flightCode, String passenger, NotificationHandler handler)
            throws RemoteException {
        Flight flight;
        synchronized (store.getFlightCodes()) {
            FlightState state = Optional.ofNullable(store.getFlightCodes().get(flightCode))
                    .filter(s -> !FlightState.CONFIRMED.equals(s))
                    .orElseThrow(IllegalFlightStateException::new);
            Map<String, Flight> flights = store.getFlightsByState(state);
            synchronized (flights) {
                flight = flights.get(flightCode);
                flight.getSeatsLock().lock();
            }
        }

        try {
            Optional.ofNullable(flight.getTickets().get(passenger))
                    .orElseThrow(PassengerNotSeatedException::new);
        } finally {
            flight.getSeatsLock().unlock();
        }
        LOGGER.info("Registered passenger " + passenger + " to receive notifications");
        store.registerUser(new Notification(flightCode, flight.getDestination()), passenger,
                Collections.singletonList(handler));
    }
}