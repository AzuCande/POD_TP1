package ar.edu.itba.pod.server.service;

import ar.edu.itba.pod.callbacks.NotificationHandler;
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
    public void addFlight(String planeModel, String flightCode, String destination, List<Ticket> tickets) throws RemoteException {
        store.getFlightsLock().lock();

        boolean isOccupied;

        try {
            if (store.getPlaneModels().getOrDefault(planeModel, null) == null) {
                // TODO: podriamos hacer un try catch con un finally con try lock
                throw new RuntimeException();
            }

            isOccupied = store.getFlights().containsKey(flightCode);

            if (isOccupied)
                throw new RuntimeException();

            store.getFlights().put(flightCode, null);
        } finally {
            store.getFlightsLock().unlock();
        }

        PlaneModel model = store.getPlaneModels().get(planeModel);
        store.getFlights().put(flightCode, new Flight(new Plane(model), flightCode, destination, tickets));
    }

    @Override
    public Collection<PlaneModel> getPlaneModels() {
        // TODO: que onda la lectura?
        return new ArrayList<>(store.getPlaneModels().values());
    }

    @Override
    public Collection<Flight> getFlights() {
        // TODO: que onda la lectura?
        return new ArrayList<>(store.getFlights().values());
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
        Flight flight = Optional.ofNullable(store.getFlights().get(flightCode))
                .filter(f -> f.getState().equals(FlightState.PENDING)).orElseThrow(IllegalArgumentException::new); // TODO: custom exception

        flight.setState(state);
        store.getFlightsLock().unlock();

        // TODO: lockear las notificaciones
        store.getNotifications().getOrDefault(flightCode, new HashMap<>()).forEach((passenger, handlers) -> {
            try {
                Ticket ticket = flight.getTickets().stream()
                        .filter(t -> t.getPassenger().equals(passenger)).findFirst().orElseThrow(IllegalArgumentException::new);

//                Row row = Arrays.stream(flight.getPlane().getRows()).filter(r -> r.passengerHasSeat(passenger)).findFirst().orElseThrow(IllegalArgumentException::new);
//
//                row.
                for (NotificationHandler handler : handlers) {
                    store.submitNotificationTask(() -> {
                        try {
                            switch (flight.getState()) {
                                case CONFIRMED:
                                    handler.notifyConfirmFlight(flightCode, flight.getDestination(),
                                            state, ticket.getCategory(), 1, 'A');
                                    break;
                                case CANCELED:
                                    handler.notifyCancelFlight(flightCode, flight.getDestination(),
                                            state, ticket.getCategory(), 1, 'A');
                                    break;
                            }
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void changeCancelledFlights() throws RemoteException {
        // TODO: concurrencia
        Map<FlightState, List<Flight>> partition = store.getFlights().values().stream()
                .filter(flight -> !flight.getState().equals(FlightState.CONFIRMED))
                .collect(Collectors.groupingBy(Flight::getState));

        List<Flight> cancelledFlights = partition.get(FlightState.CANCELED)
                .stream().sorted(Comparator.comparing(Flight::getCode)).collect(Collectors.toList());

        List<Flight> pendingFlights = partition.get(FlightState.PENDING)
                .stream().sorted(Comparator.comparing(Flight::getCode)).collect(Collectors.toList());

        List<Ticket> toRemove = new ArrayList<>();

        for (Flight cancelled : cancelledFlights) {
            List<Ticket> tickets = cancelled.getTickets()
                    .stream().sorted(Comparator.comparing(Ticket::getPassenger)).collect(Collectors.toList());
            for (Ticket ticket : tickets) {
                String destination = ticket.getDestination();
                RowCategory category = ticket.getCategory();

                List<Flight> alternativeFlights = pendingFlights
                        .stream()
                        .filter(flight -> Objects.equals(flight.getDestination(), destination) &&
                                hasAvailableSeats(flight, category))
                        .sorted(Comparator.comparing(flight -> getAvailableSeats(flight, category)))
                        .collect(Collectors.toList());

                if (alternativeFlights.isEmpty()) {
                    logger.info("No alternative flights found for passenger " + ticket.getPassenger());
                    continue;
                }

                Flight newFlight = alternativeFlights.get(0);

                // Transfer ticket from cancelled to newFlight
                //cancelled.removeTicket(ticket); // TODO: esto es legal mientras itero? no.
                // TODO: estas llamando a un metodo, no veo por qué no sería legal
                toRemove.add(ticket);
                newFlight.findSeat(ticket);

                // TODO chequeos de que se pudo hacer
                store.getNotifications().getOrDefault(cancelled.getCode(), new HashMap<>())
                        .getOrDefault(ticket.getPassenger(), new ArrayList<>())
                        .forEach(handler -> store.submitNotificationTask(() -> {
                            try {
                                // TODO
                                handler.notifyChangeTicket(cancelled.getCode(),
                                        cancelled.getDestination(), newFlight.getCode());
                            } catch (RemoteException e) {
                                throw new RuntimeException(e);
                            }

                        }));

            }
            for (Ticket remove : toRemove) {
                cancelled.removeTicket(remove);
            }
            toRemove.clear();
        }
    }

    private int getAvailableSeats(Flight flight, RowCategory category) {
        int[] availableSeats = flight.getPlane().getAvailableSeats(); // TODO: lockear los asientos
        for (int i = category.ordinal(); i >= 0; i--) {
            if (availableSeats[i] > 0)
                return i;
        }

        return -1;
    }

    private boolean hasAvailableSeats(Flight flight, RowCategory category) {
        return getAvailableSeats(flight, category) != -1;
    }
}
