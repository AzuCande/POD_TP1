package ar.edu.itba.pod.client.parsers;

import ar.edu.itba.pod.client.utils.FlightActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Properties;

public class FlightManagerParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlightManagerParser.class);
    private static final String SERVER_ADDRESS = "serverAddress";
    private static final String ACTION = "action";
    private static final String PATH = "inPath";
    private static final String FLIGHT_CODE = "flight";

    private String serverAddress;
    private Optional<FlightActions> action;
    private String path;
    private String flightCode;

    public void parse() {
        Properties props = System.getProperties();

        if((serverAddress = props.getProperty(SERVER_ADDRESS)) == null) {
            LOGGER.error("Server address not specified");
            System.exit(1);

        }

        try {
            action = Optional.ofNullable(props.getProperty(ACTION)).map(FlightActions::getAction);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid action", e);
            System.exit(1);
        }

        if(!action.isPresent()) {
            LOGGER.error("Action not specified");
            System.exit(1);
        }

        path = props.getProperty(PATH);
        flightCode = props.getProperty(FLIGHT_CODE);

        if(FlightActions.MODELS.equals(action.orElse(null))
                || FlightActions.FLIGHTS.equals(action.orElse(null))) {
            if(path == null) {
                LOGGER.error("Path not specified");
                System.exit(1);
            }
        }

        if(FlightActions.STATUS.equals(action.orElse(null)) ||
                FlightActions.CONFIRM.equals(action.orElse(null)) ||
                FlightActions.CANCEL.equals(action.orElse(null))){
            if(flightCode == null) {
                LOGGER.error("Flight code not specified");
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
