package ar.edu.itba.pod.client;

import ar.edu.itba.pod.client.parsers.FlightManagerParser;
import ar.edu.itba.pod.interfaces.FlightManagerService;
import ar.edu.itba.pod.models.ResponseCancelledList;
import ar.edu.itba.pod.models.RowCategory;
import ar.edu.itba.pod.models.Ticket;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FlightManagerClient {
    private static final Logger logger = LoggerFactory.getLogger(FlightManagerClient.class);
    private static final ICSVParser CSV_PARSER = new CSVParserBuilder().withSeparator(';').build();

    public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {
        FlightManagerParser parser = new FlightManagerParser();
        parser.parse();
        logger.info("Flight Manager Client Starting ...");

        FlightManagerService flightManagerService =
                (FlightManagerService) Naming.lookup("//" + parser.getServerAddress() + "/flightManagerService");

        try {
            switch (parser.getAction().get()) {
                case MODELS:
                    logger.info("Uploading plane models...");
                    FlightManagerClient.readPlaneModels(parser.getPath(), flightManagerService);
                    break;
                case FLIGHTS:
                    logger.info("Uploading flights...");
                    FlightManagerClient.readFlights(parser.getPath(), flightManagerService);
                    break;
                case STATUS:
                    logger.info("Checking flight " + parser.getFlightCode() + " status...");
                    System.out.println(flightManagerService.getFlightState(parser.getFlightCode()));
                    break;
                case CONFIRM:
                    logger.info("Confirming flight " + parser.getFlightCode() + " ...");
                    flightManagerService.confirmFlight(parser.getFlightCode());
                    System.out.println("Flight confirmed successfully");
                    break;
                case CANCEL:
                    logger.info("Canceling flight " + parser.getFlightCode() + " ...");
                    flightManagerService.cancelFlight(parser.getFlightCode());
                    System.out.println("Flight cancelled successfully");
                    break;
                case RETICKETING:
                    logger.info("Reticketing cancelled flights...");
                    printReticketing(flightManagerService.changeCancelledFlights());
                    System.out.println("Reticketing successful");
                    break;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }

    public static void readPlaneModels(String fileName, FlightManagerService flightManagerService) {
        try (FileReader fr = new FileReader(fileName); CSVReader reader = new CSVReaderBuilder(fr)
                .withCSVParser(CSV_PARSER).build()) {
            String[] nextLine;
            reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                String planeModel = nextLine[0];
                System.out.println(planeModel);
                Map<String, int[]> map = new HashMap<>();

                String[] categories = nextLine[1].split(",");
                for (String category : categories) {
                    String[] parts = category.split("#");
                    String seatCategory = parts[0];
                    int rows = Integer.parseInt(parts[1]);
                    int cols = Integer.parseInt(parts[2]);
                    map.put(seatCategory, new int[]{rows, cols});
                }
                try {
                    flightManagerService.addPlaneModel(planeModel, map);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("Ignoring model...");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }


    public static void readFlights(String fileName, FlightManagerService flightManager) {
        try (FileReader fr = new FileReader(fileName); CSVReader reader = new CSVReaderBuilder(fr)
                .withCSVParser(CSV_PARSER).build()) {
            String[] nextLine;
            reader.readNext();

            List<Ticket> tickets = new ArrayList<>();

            while ((nextLine = reader.readNext()) != null) {
                String planeModel = nextLine[0];
                String flightCode = nextLine[1];
                String destination = nextLine[2];
                String[] passengers = nextLine[3].split(",");

                for (String passenger : passengers) {
                    String[] parts = passenger.split("#");
                    RowCategory seatCategory = RowCategory.valueOf(parts[0]);
                    String name = parts[1];
                    tickets.add(new Ticket(seatCategory, name, destination));
                }
                try {
                    flightManager.addFlight(planeModel, flightCode, destination, tickets);
                } catch (Exception e) {
                    System.out.println("No model for flight " + flightCode);
                }
                tickets.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void printReticketing(ResponseCancelledList list) {
        System.out.printf("%d tickets were changed\n", list.getChanged());
        list.getUnchangedTickets().forEach(t -> {
            System.out.printf("Cannot find alternative flight for %s on Flight %s\n", t.getPassenger(), t.getFlightCode());
        });

    }
}
