package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.server.model.FlightState;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import ar.edu.itba.pod.server.model.Plane;

import java.io.FileReader;

public class FlightManager {
    Set<String> planeModels = ConcurrentHashMap.newKeySet(); // TODO chequear que garantiza exactamente
    Set<String> flightCodes = ConcurrentHashMap.newKeySet();
    Set<String> airports = ConcurrentHashMap.newKeySet();

    public void addPlaneModel() throws FileNotFoundException {
        //consume CSV with plane models
        String fileName = "D:\\itba\\pod\\POD_TP1\\server\\src\\main\\resources\\csv\\planeModels.csv"; //TODO: hacer relative path
        int lineNumber = 0;
        try(BufferedReader reader = new BufferedReader(new FileReader(fileName));){

            String line = reader.readLine(); //ignore first line
            reading_file:
            while((line = reader.readLine()) != null) {
                lineNumber++;
                String[] model_seats = line.split(";|,|#");
                int[] categoryIndexes = {1,4,7};
                int[] categorySizes = {0,0,0,0,0,0}; //filas-columnas bussines, premium, economy
                for(int category : categoryIndexes) {
                    if(model_seats.length > category) {
                        switch (model_seats[category]) {
                            case "BUSINESS":
                                categorySizes[0] = Integer.parseInt(model_seats[category+1]);
                                categorySizes[1] = Integer.parseInt(model_seats[category+2]);
                                break;
                            case "PREMIUM_ECONOMY":
                                categorySizes[2] = Integer.parseInt(model_seats[category+1]);
                                categorySizes[3] = Integer.parseInt(model_seats[category+2]);
                                break;
                            case "ECONOMY":
                                categorySizes[4] = Integer.parseInt(model_seats[category+1]);
                                categorySizes[5] = Integer.parseInt(model_seats[category+2]);
                                break;
                            default:
                                System.out.println("error 1 parsing csv in line " + lineNumber);
                                continue reading_file;
                        }
                    }
                }
                System.out.println(model_seats[0] + "\nbusiness: " + categorySizes[0] + " " + categorySizes[1] + "\npremium economy: " + categorySizes[2] + " " + categorySizes[3] + "\neconomy: " + categorySizes[4] + " " + categorySizes[5] +  "\n----");
//                Plane newPlane = new Plane(model_seats[0], categorySizes[0], categorySizes[1], categorySizes[2], categorySizes[3], categorySizes[4], categorySizes[5]);
//                planeModels.add(newPlane);
            }
        } catch (IOException e) {
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
