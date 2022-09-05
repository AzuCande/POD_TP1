package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.server.model.FlightState;
import ar.edu.itba.pod.server.model.PlaneModel;
import ar.edu.itba.pod.server.interfaces.FlightManagerService;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FlightManager implements FlightManagerService {
    Set<PlaneModel> planeModels = ConcurrentHashMap.newKeySet(); // TODO chequear que garantiza exactamente
    Set<String> flightCodes = ConcurrentHashMap.newKeySet();
    Set<String> airports = ConcurrentHashMap.newKeySet();

    public void addPlaneModel(String model, Map<String, int[]> seatCategories) {
        planeModels.add(new PlaneModel(model, seatCategories));
    }

    @Override
    public void addFlight() throws RemoteException {
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
