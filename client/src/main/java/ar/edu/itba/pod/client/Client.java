package ar.edu.itba.pod.client;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private static Logger logger = LoggerFactory.getLogger(Client.class);
    private static final ICSVParser CSV_PARSER = new CSVParserBuilder().withSeparator(';').build();
    public static void main(String[] args) {
        logger.info("rmi-project Client Starting ...");
        Client.readPlaneModels("client/src/main/resources/planes.csv");
    }

    public static void readPlaneModels(String fileName) {
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
                    System.out.println(seatCategory + " " + rows + " " + cols );
                }
                // TODO: Call PlaneService
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }
}
