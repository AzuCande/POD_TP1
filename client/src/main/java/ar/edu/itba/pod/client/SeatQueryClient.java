package ar.edu.itba.pod.client;

import ar.edu.itba.pod.client.parsers.SeatQueryParser;
import ar.edu.itba.pod.interfaces.SeatQueryService;
import ar.edu.itba.pod.models.ResponseRow;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class SeatQueryClient {
    private static final Logger logger = LoggerFactory.getLogger(SeatQueryClient.class);

    public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {
        SeatQueryParser parser = new SeatQueryParser();
        parser.parse();

        logger.info("Flight Notifications Client Starting ...");

        SeatQueryService service = (SeatQueryService) Naming.lookup("//" + parser.getServerAddress() + "/seatQueryService");

        ArrayList<ResponseRow> rows = null;

        if (parser.getRow().isPresent() && parser.getCategory().isPresent()) {
            System.out.println("Invalid params");
            System.exit(1);

        } else if (parser.getRow().isPresent()) {
            rows = new ArrayList<>();
            rows.add(parser.getRow().get(), service.query(parser.getFlight(), parser.getRow().get()));

        } else if (parser.getCategory().isPresent()) {
            rows = service.query(parser.getFlight(), parser.getCategory().get());

        } else {
            rows = service.query(parser.getFlight());
        }
        writeToCSV(rows, parser.getOutPath());
    }

    public static void writeToCSV(ArrayList<ResponseRow> rows, String path) {
        File file = new File(path);
        try {

            FileWriter outputFile = new FileWriter(file);

            CSVWriter writer = new CSVWriter(outputFile);

            String[] header = {"Seats", "Category"};
            writer.writeNext(header);

            int index = 0;
            for (ResponseRow row : rows) {

                StringBuilder stringBuilder = new StringBuilder("|");
                for (int i = 0; i < row.getPassengerInitials().length; i++) {
                    stringBuilder.append(index).append(" ").append((char) (i + 'A')).append(" ").append(row.getPassengerInitials()[i]).append("|");
                }
                String[] seats = new String[2];
                seats[0] = stringBuilder.toString();
                seats[1] = row.getRowCategory().toString();
                System.out.println(seats[0] + " " + seats[1]);
                index++;
                writer.writeNext(seats);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
