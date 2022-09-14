package ar.edu.itba.pod.client.parsers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class FlightNotificationsParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightNotificationsParser.class);
    private static final String SERVER_ADDRESS = "serverAddress";
    private static final String FLIGHT = "flight";
    private static final String PASSENGER = "passenger";

    private String serverAddress;
    private String flight;
    private String passenger;



    public void parse() {
        Properties props = System.getProperties();

        if((serverAddress = props.getProperty(SERVER_ADDRESS)) == null) {
            LOGGER.error("Server address not specified");
            System.exit(1);
        }

        if((flight = props.getProperty(FLIGHT)) == null) {
            LOGGER.error("Flight not specified");
            System.exit(1);
        }

        if((passenger = props.getProperty(PASSENGER)) == null) {
            LOGGER.error("Passenger not specified");
            System.exit(1);
        }

    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getFlight() {
        return flight;
    }

    public String getPassenger() {
        return passenger;
    }
}
