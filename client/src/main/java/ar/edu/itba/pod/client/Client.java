package ar.edu.itba.pod.client;

import ar.edu.itba.pod.interfaces.FlightManagerService;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static final ICSVParser CSV_PARSER = new CSVParserBuilder().withSeparator(';').build();

    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
        logger.info("rmi-project Client Starting ...");

        FlightManagerService flightManagerService = (FlightManagerService) Naming.lookup("//127.0.0.1:1099/flightManagerService");

        Client.readPlaneModels( "client/src/main/resources/planes.csv", flightManagerService);

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
                    System.out.println(seatCategory + " " + rows + " " + cols);
                }

                // TODO: Call PlaneService
                flightManagerService.addPlaneModel(planeModel, map);
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }

    //Boeing 787;AA100;JFK;BUSINESS#John,ECONOMY#Juliet,BUSINESS#Elizabeth
    public static void readFlights(String fileName, FlightManagerService flightManager) {
        try (FileReader fr = new FileReader(fileName); CSVReader reader = new CSVReaderBuilder(fr)
                .withCSVParser(CSV_PARSER).build()) {
            String[] nextLine;
            reader.readNext();
            int lineNumber = 1;

            Map<String, Set<String>> ticketMap = new HashMap<>(); //category -> Set<names>
            ticketMap.put("BUSINESS", new HashSet<>());
            ticketMap.put("PREMIUM_ECONOMY", new HashSet<>());
            ticketMap.put("ECONOMY", new HashSet<>());

            while ((nextLine = reader.readNext()) != null) {
                String planeModel = nextLine[0];
                String flightCode = nextLine[1];
                String destination = nextLine[2];
                String[] passengers = nextLine[3].split(",");
//                if(!flightManager.isPlanemodelAvailable(planeModel)){
//                    logger.error("Plane model: {} does not exist, line {} ignored", planeModel, lineNumber++);
//                    continue;
//                }
//                if(!flightManager.isFlightCodeAvailable(flightCode)){
//                    logger.error("Flight code: {} duplicated, line {} ignored", planeModel, lineNumber++);
//                    continue;
//                }
                for (String passenger : passengers) {
                    String[] parts = passenger.split("#");
                    String seatCategory = parts[0];
                    String name = parts[1];
                    ticketMap.get(seatCategory).add(name);
                }
                flightManager.addFlight(planeModel, flightCode, destination, ticketMap);
                lineNumber++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException | CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }
}
