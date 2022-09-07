package ar.edu.itba.pod.client.parsers;

import ar.edu.itba.pod.client.utils.FlightActions;

import java.util.Optional;
import java.util.Properties;

public class FlightManagerParser {
    private static final String SERVER_ADDRESS = "serverAddress";
    private static final String ACTION = "action";
    private static final String PATH = "inPath";
    private static final String FLIGHT_CODE = "flights";

    private String serverAddress;
    private Optional<FlightActions> action;
    private String path;
    private String flightCode;

    public void parse() {
        Properties props = System.getProperties();

        if((serverAddress = props.getProperty(SERVER_ADDRESS)) == null) {
            System.out.println("Server address not specified");
            System.exit(1);
        }

        try {
            action = Optional.ofNullable(props.getProperty(ACTION)).map(FlightActions::getAction);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid action");
            System.exit(1);
        }

        if(!action.isPresent()) {
            System.out.println("Action not specified");
            System.exit(1);
        }

        path = props.getProperty(PATH);
        flightCode = props.getProperty(FLIGHT_CODE);

        if(FlightActions.MODELS.equals(action.orElse(null))
                || FlightActions.FLIGHTS.equals(action.orElse(null))) {
            if(path == null) {
                System.out.println("Path not specified");
                System.exit(1);
            }
        }

        if(FlightActions.STATUS.equals(action.orElse(null)) ||
                FlightActions.CONFIRM.equals(action.orElse(null)) ||
                FlightActions.CANCEL.equals(action.orElse(null))){
            if(flightCode == null) {
                System.out.println("Flight code not specified");
                System.exit(1);
            }
        }
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public Optional<FlightActions> getAction() {
        return action;
    }

    public String getPath() {
        return path;
    }

    public String getFlightCode() {
        return flightCode;
    }
}
