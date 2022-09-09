package ar.edu.itba.pod.client;

import ar.edu.itba.pod.client.parsers.FlightNotificationsParser;
import ar.edu.itba.pod.client.parsers.SeatQueryParser;
import ar.edu.itba.pod.interfaces.NotificationService;
import ar.edu.itba.pod.interfaces.SeatManagerService;
import ar.edu.itba.pod.interfaces.SeatQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class SeatQueryClient {
    private static final Logger logger = LoggerFactory.getLogger(SeatQueryClient.class);

    public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {
        SeatQueryParser parser = new SeatQueryParser();
        parser.parse();

        logger.info("Flight Notifications Client Starting ...");

        SeatQueryService service =
                (SeatQueryService) Naming.lookup("//127.0.0.1:1099/" + "seatQueryService");

        service.query(parser.getFlight(), "nada");
        // TODO: parametros y escribir respuesta a CSV
    }
}
