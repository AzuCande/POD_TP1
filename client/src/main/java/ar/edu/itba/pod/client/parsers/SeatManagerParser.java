package ar.edu.itba.pod.client.parsers;

import ar.edu.itba.pod.client.utils.SeatActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

public class SeatManagerParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeatManagerParser.class);
    private static final String SERVER_ADDRESS = "serverAddress";
    private static final String ACTION = "action";
    private static final String FLIGHT_CODE = "flight";
    private static final String PASSENGER = "passenger";
    private static final String ROW = "row";
    private static final String COLUMN = "col";
    private static final String ORIGINAL_FLIGHT = "originalFlight";

    private String serverAddress;
    private String flightCode;
    private Optional<SeatActions> action;
    private Optional<String> passenger;
    private Optional<Integer> row;
    private Optional<Character> column;
    private Optional<String> originalFlightCode;

    public void parse() {
        Properties props = System.getProperties();

        if ((serverAddress = props.getProperty(SERVER_ADDRESS)) == null) {
            LOGGER.error("Server address not specified");
            System.exit(1);
        }

        try {
            action = Optional.ofNullable(props.getProperty(ACTION)).map(p ->
                    Arrays.stream(SeatActions.values()).filter(a -> a.getDescription().equals(p))
                            .findFirst().orElseThrow(IllegalArgumentException::new));
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid action", e);
            System.exit(1);
        }

        if (!action.isPresent()) {
            LOGGER.error("Action not specified");
            System.exit(1);
        }

        if ((flightCode = props.getProperty(FLIGHT_CODE)) == null) {
            LOGGER.error("Flight code not specified");
            System.exit(1);
        }

        passenger = Optional.ofNullable(props.getProperty(PASSENGER));

        Optional<String> col = Optional.of(props.getProperty(COLUMN));
        col.ifPresent(s -> column = Optional.of(s.charAt(0)));

        if (SeatActions.STATUS.equals(action.orElse(null)) || SeatActions.ASSIGN.equals(action.orElse(null)) ||
                SeatActions.MOVE.equals(action.orElse(null))) {
            try {
                row = Optional.ofNullable(props.getProperty(ROW)).map(Integer::parseInt);
            } catch (NumberFormatException e) {
                LOGGER.error("Invalid row", e);
                System.exit(1);
            }
            if (!column.isPresent()) {
                LOGGER.error("Column not specified");
                System.exit(1);
            }
        }

        if (SeatActions.ASSIGN.equals(action.orElse(null)) || SeatActions.MOVE.equals(action.orElse(null))
                || SeatActions.ALTERNATIVES.equals(action.orElse(null)) || SeatActions.CHANGE_TICKET.equals(action.orElse(null))) {
            if (!passenger.isPresent()) {
                LOGGER.error("Passenger not specified");
            }
        }
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public Optional<SeatActions> getAction() {
        return action;
    }

    public Optional<String> getPassenger() {
        return passenger;
    }

    public Optional<Integer> getRow() {
        return row;
    }

    public Optional<Character> getColumn() {
        return column;
    }

    public Optional<String> getOriginalFlightCode() {
        return originalFlightCode;
    }
}
