package ar.edu.itba.pod.client;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.client.parsers.FlightNotificationsParser;
import ar.edu.itba.pod.client.utils.NotificationHandlerImpl;
import ar.edu.itba.pod.interfaces.NotificationService;
import ar.edu.itba.pod.models.exceptions.PassengerNotSeatedException;
import ar.edu.itba.pod.models.exceptions.flightExceptions.IllegalFlightStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class FlightNotificationsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlightNotificationsClient.class);

    public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {
        FlightNotificationsParser parser = new FlightNotificationsParser();
        parser.parse();

        LOGGER.info("Flight Notifications Client Starting ...");

        final NotificationHandler notificationHandler = new NotificationHandlerImpl();

        final Registry registry = LocateRegistry.getRegistry();

        final Remote remote = UnicastRemoteObject.exportObject(notificationHandler, 0);

        registry.rebind("notificationHandler", remote);

        NotificationService notificationService = (NotificationService) Naming.lookup("//" +
                parser.getServerAddress() + "/notificationService");

        try {
            notificationService.registerPassenger(parser.getFlight(), parser.getPassenger(), notificationHandler);
        } catch (RemoteException | IllegalFlightStateException | PassengerNotSeatedException e) {
            LOGGER.error("Error registering passenger", e);
            UnicastRemoteObject.unexportObject(notificationHandler, true);
        }
    }
}
