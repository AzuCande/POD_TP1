package ar.edu.itba.pod.client.parsers;

import ar.edu.itba.pod.models.RowCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Properties;

public class SeatQueryParser {
    private final Logger LOGGER = LoggerFactory.getLogger(SeatQueryParser.class);
    private static final String SERVER_ADDRESS = "serverAddress";
    private static final String FLIGHT = "flight";
    private static final String CATEGORY = "category";
    private static final String ROW = "row";
    private static final String OUT_PATH = "outPath";

    private String serverAddress;
    private String flight;
    private Optional<RowCategory> category;
    private Optional<Integer> row;
    private String outPath;

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

        
        category = Optional.ofNullable(props.getProperty(CATEGORY)).map(RowCategory::valueOf);
        row = Optional.ofNullable(props.getProperty(ROW)).map(Integer::parseInt);
        outPath = props.getProperty(OUT_PATH);
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getFlight() {
        return flight;
    }

    public Optional<RowCategory> getCategory() {
        return category;
    }

    public Optional<Integer> getRow() {
        return row;
    }

    public String getOutPath() {
        return outPath;
    }
}
