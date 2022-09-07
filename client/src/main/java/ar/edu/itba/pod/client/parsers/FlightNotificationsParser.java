package ar.edu.itba.pod.client.parsers;

import java.util.Properties;

public class FlightNotificationsParser {
    private static final String SERVER_ADDRESS = "serverAddress";
    private static final String FLIGHT = "flight";
    private static final String PASSENGER = "passenger";

    private String serverAddress;
    private String flight;
    private String passenger;



    public void parse() {
        Properties props = System.getProperties();

        if((serverAddress = props.getProperty(SERVER_ADDRESS)) == null) {
            System.out.println("Server address not specified");
            System.exit(1);
        }

        if((flight = props.getProperty(FLIGHT)) == null) {
            System.out.println("Flight not specified");
            System.exit(1);
        }

        if((passenger = props.getProperty(PASSENGER)) == null) {
            System.out.println("Passenger not specified");
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
