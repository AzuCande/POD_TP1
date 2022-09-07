package ar.edu.itba.pod.client;

import ar.edu.itba.pod.callbacks.NotificationHandler;
import ar.edu.itba.pod.client.parsers.FlightManagerParser;
import ar.edu.itba.pod.client.parsers.FlightNotificationsParser;
import ar.edu.itba.pod.interfaces.FlightManagerService;
import ar.edu.itba.pod.interfaces.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class FlightNotificationsClient {

    private static final Logger logger = LoggerFactory.getLogger(FlightNotificationsClient.class);

    public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {
        FlightNotificationsParser parser = new FlightNotificationsParser();
        parser.parse();

        logger.info("Flight Notifications Client Starting ...");

        NotificationService notificationService =
                (NotificationService) Naming.lookup("//127.0.0.1:1099/" + NotificationService.class.getName());

        notificationService.registerPassenger(parser.getFlight(), parser.getPassenger(),
                () -> System.out.println("Notified!"));

        // TODO: implement notifications callback
    }
}
