package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.model.Flight;
import ar.edu.itba.pod.model.FlightState;
import ar.edu.itba.pod.model.PlaneModel;
import ar.edu.itba.pod.interfaces.FlightManagerService;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FlightManager implements FlightManagerService {
    Set<PlaneModel> planeModels = ConcurrentHashMap.newKeySet(); // TODO chequear que garantiza exactamente. Ver si dejamos la interfaz Set
    Set<Flight> flights = ConcurrentHashMap.newKeySet();
    Set<String> airports = ConcurrentHashMap.newKeySet(); //TODO : deprecated?


    @Override
    public void addPlaneModel(String model, Map<String, int[]> seatCategories) {
        planeModels.add(new PlaneModel(model, seatCategories));
    }

    @Override
    public void addFlight(String planeModel, String flightCode, String destination, Map<String, Set<String>> ticketMap) throws RemoteException {

        flights.add(new Flight(null, flightCode, destination, ticketMap)); //TODO: gettear el planemodel

    }

    @Override
    public Set<PlaneModel> getPlaneModels(){
        return this.planeModels;
    }

    @Override
    public Set<Flight> getFlights(){
        return this.flights;
    }

    @Override
    public boolean isPlanemodelAvailable(String PlaneModel) throws RemoteException { //TODO: check if string is in set
        return false;
    }

    @Override
    public boolean isFlightcodeAvailable(String flightCode) throws RemoteException { //TODO: check if string is in set
        return false;
    }

    @Override
    public FlightState getFlightState(String flight) throws RemoteException {
        return null;
    }

    @Override
    public void setFlightState(FlightState newState) throws RemoteException {

    }

    @Override
    public void listAlternativeFlights() throws RemoteException {

    }
}
