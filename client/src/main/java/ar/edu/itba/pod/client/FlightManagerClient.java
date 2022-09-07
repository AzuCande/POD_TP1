package ar.edu.itba.pod.client;

import ar.edu.itba.pod.client.parsers.FlightManagerParser;
import ar.edu.itba.pod.client.utils.FlightActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import static ar.edu.itba.pod.client.utils.FlightActions.*;

public class FlightManagerClient {
    private static final Logger logger = LoggerFactory.getLogger(FlightManagerClient.class); // TODO: SymbolError con mvn install

    public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {
        FlightManagerParser parser = new FlightManagerParser();
        parser.parse();

        logger.info("Flight Manager Client Starting ...");

        /*
        FlightManagerService flightManagerService =
                (FlightManagerService) Naming.lookup("//127.0.0.1:1099/flightManagerService");


         */
        switch (parser.getAction().get()) {
            case MODELS:
                // TODO: parse csv
                System.out.println(FlightActions.MODELS.getDescription());
                break;
            case FLIGHTS:
                System.out.println(FlightActions.FLIGHTS);
                // TODO: parse csv
                break;
            case STATUS:
                //System.out.println(flightManagerService.getFlightState(parser.getFlightCode()));
                System.out.println(STATUS);
                break;
            case CONFIRM:
                System.out.println(FlightActions.CONFIRM);
                //flightManagerService.confirmFlight(parser.getFlightCode());
                break;
            case CANCEL:
                //flightManagerService.cancelFlight(parser.getFlightCode());
                System.out.println(FlightActions.CANCEL);
                break;
            case RETICKETING:
                //flightManagerService.changeCancelledFlights();
                System.out.println(FlightActions.RETICKETING);
                break;
        }
    }
}
