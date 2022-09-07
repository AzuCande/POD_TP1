package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.models.*;
import ar.edu.itba.pod.interfaces.FlightManagerService;
import ar.edu.itba.pod.server.ServerStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class FlightManagerServiceImpl implements FlightManagerService {

    private static final Logger logger = LoggerFactory.getLogger(FlightManagerServiceImpl.class);
    private final ServerStore store;

    final Lock modelsLock = new ReentrantLock();


    public FlightManagerServiceImpl(ServerStore store) {
        this.store = store;
    }

    @Override
    public void addPlaneModel(String model, Map<String, int[]> seatCategories) {
        modelsLock.lock();
        boolean isOccupied = store.getPlaneModels().containsKey(model);
        if (!isOccupied)
            store.getPlaneModels().put(model, null);
        modelsLock.unlock();
        // TODO: ver si con un concurrent map y compute if absent queda mejor
        if (isOccupied)
            throw new RuntimeException();

        store.getPlaneModels().put(model, new PlaneModel(model, seatCategories));
        logger.info("Added flight");

    }

    @Override
    public void addFlight(String planeModel, String flightCode, String destination, Map<RowCategory, Set<String>> ticketMap) throws RemoteException {
        store.getFlightsLock().lock();
        if (store.getPlaneModels().getOrDefault(planeModel, null) != null) {
            store.getFlightsLock().unlock(); // TODO: podriamos hacer un try catch con un finally con try lock
            throw new RuntimeException();
        }

        boolean isOccupied = store.getFlights().containsKey(flightCode);

        if (!isOccupied)
            store.getFlights().put(flightCode, null);
        modelsLock.unlock();
        if (isOccupied)
            throw new RuntimeException();
        PlaneModel model = store.getPlaneModels().get(planeModel);
        store.getFlights().put(flightCode, new Flight(new Plane(model), flightCode, destination, ticketMap));
    }

    @Override
    public Collection<PlaneModel> getPlaneModels() {
        // TODO: que onda la lectura?
        return store.getPlaneModels().values();
    }

    @Override
    public Collection<Flight> getFlights() {
        // TODO: que onda la lectura?
        return store.getFlights().values();
    }

    @Override
    public boolean hasPlaneModel(String PlaneModel) throws RemoteException { //TODO: check if string is in set
//        modelsLock.lock();
        boolean hasPlane = store.getPlaneModels().containsKey(PlaneModel);
//        modelsLock.unlock();
        return hasPlane;

    }

    @Override
    public boolean hasFlightCode(String flightCode) throws RemoteException { //TODO: check if string is in set
        store.getFlightsLock().lock();
        boolean hasFlightCode = store.getFlights().containsKey(flightCode);
        store.getFlightsLock().unlock();
        return hasFlightCode;
    }

    @Override
    public FlightState getFlightState(String flightCode) throws RemoteException {
        Optional<FlightState> flightState = Optional.empty();
        store.getFlightsLock().lock();
        Flight flight = store.getFlights().getOrDefault(flightCode, null);
        if (flight != null)
            flightState = Optional.of(flight.getState());
        store.getFlightsLock().unlock();
        return flightState.orElseThrow(RuntimeException::new); // TODO nuestra excepcion
    }

    @Override
    public void confirmFlight(String flightCode) throws RemoteException {
        changeFlightState(flightCode, FlightState.CONFIRMED);
    }

    @Override
    public void cancelFlight(String flightCode) throws RemoteException {
        changeFlightState(flightCode, FlightState.CANCELED);
    }

    private void changeFlightState(String flightCode, FlightState state) {
        store.getFlightsLock().lock();
        Flight flight = store.getFlights().getOrDefault(flightCode, null);
        if (flight != null)
            flight.setState(state);
        store.getFlightsLock().unlock();
    }

    @Override
    public void changeCancelledFlights() throws RemoteException {
        // TODO: concurrencia
        Map<FlightState, List<Flight>> partition = store.getFlights().values().stream()
                .filter(flight -> !flight.getState().equals(FlightState.CONFIRMED))
                .collect(Collectors.groupingBy(Flight::getState));
        List<Flight> cancelledFlights = partition.get(FlightState.CANCELED);
        List<Flight> pendingFlights = partition.get(FlightState.PENDING);

        for (Flight cancelled : cancelledFlights) {

        }
    }
}
