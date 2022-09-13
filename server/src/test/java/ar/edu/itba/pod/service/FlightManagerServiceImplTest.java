package ar.edu.itba.pod.service;

import ar.edu.itba.pod.assets.TestConstants;
import ar.edu.itba.pod.models.FlightState;
import ar.edu.itba.pod.models.exceptions.flightExceptions.IllegalFlightStateException;
import ar.edu.itba.pod.models.exceptions.flightExceptions.ModelAlreadyExistsException;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.FlightNotFoundException;
import ar.edu.itba.pod.models.exceptions.notFoundExceptions.ModelNotFoundException;
import ar.edu.itba.pod.server.utils.ServerStore;
import ar.edu.itba.pod.server.models.Flight;
import ar.edu.itba.pod.server.service.FlightManagerServiceImpl;
import org.junit.Test;

import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlightManagerServiceImplTest {
    private final ServerStore store = new ServerStore();

    private final FlightManagerServiceImpl flightManagerService = new FlightManagerServiceImpl(store);

    @Test
    public void addPlaneModelSuccessfully() {
        flightManagerService.addPlaneModel(TestConstants.PLANE_MODEL_STR_1, TestConstants.SEAT_CATEGORIES);

        assertEquals(TestConstants.PLANE_MODEL_1, store.getPlaneModels().get(TestConstants.PLANE_MODEL_STR_1));
    }

    @Test(expected = ModelAlreadyExistsException.class)
    public void addPlaneModelAlreadyExists() {
        flightManagerService.addPlaneModel(TestConstants.PLANE_MODEL_STR_1, TestConstants.SEAT_CATEGORIES);
        flightManagerService.addPlaneModel(TestConstants.PLANE_MODEL_STR_1, TestConstants.SEAT_CATEGORIES);
    }

    @Test
    public void testAddFlightSuccessfully() throws RemoteException {
        flightManagerService.addPlaneModel(TestConstants.PLANE_MODEL_STR_1, TestConstants.SEAT_CATEGORIES);
        flightManagerService.addFlight(TestConstants.PLANE_MODEL_STR_1, TestConstants.FLIGHT_CODE_1, TestConstants.DESTINATION_1, TestConstants.TICKETS_1);

        assertEquals(1, store.getPendingFlights().size());

        Flight actualFlight = store.getPendingFlights().get(TestConstants.FLIGHT_CODE_1);

        assertEquals(TestConstants.FLIGHT_CODE_1, actualFlight.getCode());
        assertEquals(TestConstants.DESTINATION_1, actualFlight.getDestination());
        assertEquals(FlightState.PENDING, actualFlight.getState());

        assertEquals(TestConstants.TICKET_1, actualFlight.getTickets().get(TestConstants.PASSENGER_1));
        assertEquals(TestConstants.TICKET_2, actualFlight.getTickets().get(TestConstants.PASSENGER_2));
        assertEquals(TestConstants.TICKET_3, actualFlight.getTickets().get(TestConstants.PASSENGER_3));

        assertEquals(TestConstants.ROWS_NUM_1, actualFlight.getRows().length);
    }

    @Test(expected = ModelNotFoundException.class)
    public void testAddFlightWithNonExistentPlaneModel() throws RemoteException {
        flightManagerService.addFlight(TestConstants.PLANE_MODEL_STR_1, TestConstants.FLIGHT_CODE_1, TestConstants.DESTINATION_1, TestConstants.TICKETS_1);
    }

    @Test
    public void testGetFlightStateOfExistingFlight() throws RemoteException {
        flightManagerService.addPlaneModel(TestConstants.PLANE_MODEL_STR_1, TestConstants.SEAT_CATEGORIES);
        flightManagerService.addFlight(TestConstants.PLANE_MODEL_STR_1, TestConstants.FLIGHT_CODE_1, TestConstants.DESTINATION_1, TestConstants.TICKETS_1);

        FlightState actualFlightState = flightManagerService.getFlightState(TestConstants.FLIGHT_CODE_1);

        assertEquals(FlightState.PENDING, actualFlightState);
    }

    @Test(expected = FlightNotFoundException.class)
    public void testGetFlightStateOfNonExistentFlight() throws RemoteException {
        flightManagerService.addPlaneModel(TestConstants.PLANE_MODEL_STR_1, TestConstants.SEAT_CATEGORIES);
        flightManagerService.addFlight(TestConstants.PLANE_MODEL_STR_1, TestConstants.FLIGHT_CODE_1, TestConstants.DESTINATION_1, TestConstants.TICKETS_1);

        flightManagerService.getFlightState(TestConstants.FLIGHT_CODE_2);
    }

    @Test
    public void testConfirmFlightSuccessfully() throws RemoteException {
        flightManagerService.addPlaneModel(TestConstants.PLANE_MODEL_STR_1, TestConstants.SEAT_CATEGORIES);
        flightManagerService.addFlight(TestConstants.PLANE_MODEL_STR_1, TestConstants.FLIGHT_CODE_1, TestConstants.DESTINATION_1, TestConstants.TICKETS_1);
        flightManagerService.confirmFlight(TestConstants.FLIGHT_CODE_1);

        assertEquals(1, store.getConfirmedFlights().size());

        Flight actualFlight = store.getConfirmedFlights().get(TestConstants.FLIGHT_CODE_1);
        assertEquals(TestConstants.FLIGHT_CODE_1, actualFlight.getCode());
        assertEquals(TestConstants.DESTINATION_1, actualFlight.getDestination());
        assertEquals(FlightState.CONFIRMED, actualFlight.getState());

        assertEquals(TestConstants.TICKET_1, actualFlight.getTickets().get(TestConstants.PASSENGER_1));
        assertEquals(TestConstants.TICKET_2, actualFlight.getTickets().get(TestConstants.PASSENGER_2));
        assertEquals(TestConstants.TICKET_3, actualFlight.getTickets().get(TestConstants.PASSENGER_3));

        assertEquals(TestConstants.ROWS_NUM_1, actualFlight.getRows().length);
    }

    @Test(expected = IllegalFlightStateException.class)
    public void testConfirmFlightWithNonExistentFlight() throws RemoteException {
        flightManagerService.confirmFlight(TestConstants.FLIGHT_CODE_1);
    }

    @Test
    public void testCancelFlightSuccessfully() throws RemoteException {
        flightManagerService.addPlaneModel(TestConstants.PLANE_MODEL_STR_1, TestConstants.SEAT_CATEGORIES);
        flightManagerService.addFlight(TestConstants.PLANE_MODEL_STR_1, TestConstants.FLIGHT_CODE_1, TestConstants.DESTINATION_1, TestConstants.TICKETS_1);
        flightManagerService.cancelFlight(TestConstants.FLIGHT_CODE_1);

        assertEquals(1, store.getCancelledFlights().size());

        Flight actualFlight = store.getCancelledFlights().get(TestConstants.FLIGHT_CODE_1);
        assertEquals(TestConstants.FLIGHT_CODE_1, actualFlight.getCode());
        assertEquals(TestConstants.DESTINATION_1, actualFlight.getDestination());
        assertEquals(FlightState.CANCELED, actualFlight.getState());

        assertEquals(TestConstants.TICKET_1, actualFlight.getTickets().get(TestConstants.PASSENGER_1));
        assertEquals(TestConstants.TICKET_2, actualFlight.getTickets().get(TestConstants.PASSENGER_2));
        assertEquals(TestConstants.TICKET_3, actualFlight.getTickets().get(TestConstants.PASSENGER_3));

        assertEquals(TestConstants.ROWS_NUM_1, actualFlight.getRows().length);
    }

    @Test(expected = IllegalFlightStateException.class)
    public void testCancelFlightWithNonExistentFlight() throws RemoteException {
        flightManagerService.cancelFlight(TestConstants.FLIGHT_CODE_1);
    }

    @Test
    public void testChangeCancelledFlightsSuccessfully() throws RemoteException {
        flightManagerService.addPlaneModel(TestConstants.PLANE_MODEL_STR_1, TestConstants.SEAT_CATEGORIES);
        flightManagerService.addPlaneModel(TestConstants.PLANE_MODEL_STR_2, TestConstants.SEAT_CATEGORIES);

        flightManagerService.addFlight(TestConstants.PLANE_MODEL_STR_1, TestConstants.FLIGHT_CODE_1, TestConstants.DESTINATION_1, TestConstants.TICKETS_1);
        flightManagerService.addFlight(TestConstants.PLANE_MODEL_STR_2, TestConstants.FLIGHT_CODE_2, TestConstants.DESTINATION_1, TestConstants.TICKETS_2);

        flightManagerService.cancelFlight(TestConstants.FLIGHT_CODE_1);
        flightManagerService.changeCancelledFlights();

        assertEquals(1, store.getPendingFlights().size());
        assertEquals(1, store.getCancelledFlights().size());

        Flight pendingFlight = store.getPendingFlights().get(TestConstants.FLIGHT_CODE_2);
        Flight cancelledFlight = store.getCancelledFlights().get(TestConstants.FLIGHT_CODE_1);

        assertEquals(FlightState.PENDING, pendingFlight.getState());
        assertEquals(FlightState.CANCELED, cancelledFlight.getState());

        assertEquals(TestConstants.FLIGHT_CODE_1, cancelledFlight.getCode());
        assertEquals(TestConstants.FLIGHT_CODE_2, pendingFlight.getCode());

        assertEquals(6, pendingFlight.getTickets().values().size());
    }

    // TODO: Revisar si va RuntimeException
//    @Test(expected = RuntimeException.class)
//    public void testChangeCancelledFlightsWithNoCancelledFlights() throws RemoteException {
//        flightManagerService.changeCancelledFlights();
//    }
}