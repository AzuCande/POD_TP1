package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.server.model.FlightState;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;

public class FlightManager {
    Set<String> planeModels = ConcurrentHashMap.newKeySet(); // TODO chequear que garantiza exactamente
    Set<String> flightCodes = ConcurrentHashMap.newKeySet();
    Set<String> airports = ConcurrentHashMap.newKeySet();

    public void addPlaneModel() throws FileNotFoundException {
        //consume CSV with plane models
        String fileName = "D:\\itba\\pod\\POD_TP1\\server\\src\\main\\resources\\csv\\planeModels.csv"; //TODO: hacer relative path
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            List<String[]> r = reader.readAll();
            r.forEach(x -> System.out.println(Arrays.toString(x)));
            r.forEach(x -> planeModels.add(Arrays.toString(x)));
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public void addFlight(){
        //consume CSV with flights
    }

    public FlightState getFlightState(String flight) {
        return null;
    }

    public void setFlightState(FlightState newState){

    }

    public void listAlternativeFlights() {

    }
}
