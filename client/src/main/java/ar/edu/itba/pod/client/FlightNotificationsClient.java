package ar.edu.itba.pod.client;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.client.parsers.FlightNotificationsParser;
import ar.edu.itba.pod.client.utils.NotificationHandlerImpl;
import ar.edu.itba.pod.interfaces.NotificationService;
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

    private static final Logger logger = LoggerFactory.getLogger(FlightNotificationsClient.class);

    public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {
        FlightNotificationsParser parser = new FlightNotificationsParser();
        parser.parse();

        logger.info("Flight Notifications Client Starting ...");

        final NotificationHandler notificationHandler = new NotificationHandlerImpl();

        final Registry registry = LocateRegistry.getRegistry();

        final Remote remote = UnicastRemoteObject.exportObject(notificationHandler, 0);

        registry.rebind("notificationHandler", remote);

        NotificationService notificationService =
                (NotificationService) Naming.lookup("//127.0.0.1:1099/" + "notificationService");

        notificationService.registerPassenger(parser.getFlight(), parser.getPassenger(), notificationHandler);
    }
}
