package ar.edu.itba.pod.service;

import ar.edu.itba.pod.assets.TestConstants;
import ar.edu.itba.pod.models.ResponseRow;
import ar.edu.itba.pod.models.RowCategory;
import ar.edu.itba.pod.server.utils.ServerStore;
import ar.edu.itba.pod.server.service.FlightManagerServiceImpl;
import ar.edu.itba.pod.server.service.SeatManagerServiceImpl;
import ar.edu.itba.pod.server.service.SeatQueryServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.rmi.RemoteException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SeatQueryServiceImplTest {
    private final ServerStore store = new ServerStore();

    private final FlightManagerServiceImpl flightManagerService = new FlightManagerServiceImpl(store);
    private final SeatManagerServiceImpl seatManagerService = new SeatManagerServiceImpl(store);
    private final SeatQueryServiceImpl seatQueryService = new SeatQueryServiceImpl(store);

    @BeforeAll
    public void setUp() throws RemoteException {
        flightManagerService.addPlaneModel(TestConstants.PLANE_MODEL_STR_1, TestConstants.SEAT_CATEGORIES);

        flightManagerService.addFlight(TestConstants.PLANE_MODEL_STR_1, TestConstants.FLIGHT_CODE_1, TestConstants.DESTINATION_1, TestConstants.TICKETS_1);
        flightManagerService.addFlight(TestConstants.PLANE_MODEL_STR_1, TestConstants.FLIGHT_CODE_2, TestConstants.DESTINATION_2, TestConstants.TICKETS_2);

        seatManagerService.assign(TestConstants.FLIGHT_CODE_1, TestConstants.PASSENGER_1, TestConstants.ROW_1, TestConstants.SEAT_1);
        seatManagerService.assign(TestConstants.FLIGHT_CODE_1, TestConstants.PASSENGER_2, TestConstants.ROW_2, TestConstants.SEAT_2);
        seatManagerService.assign(TestConstants.FLIGHT_CODE_1, TestConstants.PASSENGER_3, TestConstants.ROW_5, TestConstants.SEAT_1);
        seatManagerService.assign(TestConstants.FLIGHT_CODE_1, TestConstants.PASSENGER_4, TestConstants.ROW_6, TestConstants.SEAT_2);
    }

    @Test
    public void testGetAllSeatsMap() throws RemoteException {
        List<ResponseRow> seatMap = seatQueryService.query(TestConstants.FLIGHT_CODE_1);

        assertEquals(2, seatMap.stream().filter(row -> row.getRowCategory().equals(RowCategory.BUSINESS)).count());
        assertEquals(3, seatMap.stream().filter(row -> row.getRowCategory().equals(RowCategory.PREMIUM_ECONOMY)).count());
        assertEquals(20, seatMap.stream().filter(row -> row.getRowCategory().equals(RowCategory.ECONOMY)).count());

        assertEquals(4, seatMap.stream().map(ResponseRow::getPassengerInitials).map(String::copyValueOf).filter(p -> p.matches(".*[A-Z]+.*")).count());

        assertEquals('M', seatMap.get(1).getPassengerInitials()[0]);
        assertEquals('A', seatMap.get(2).getPassengerInitials()[1]);
        assertEquals('P', seatMap.get(5).getPassengerInitials()[0]);
        assertEquals('F', seatMap.get(6).getPassengerInitials()[1]);
    }

    @Test
    public void testGetAllSeatsMapByRowCategory() throws RemoteException {
        List<ResponseRow> seatMapByCategory = seatQueryService.query(TestConstants.FLIGHT_CODE_1, RowCategory.BUSINESS);
        System.out.println(seatMapByCategory.stream().map(ResponseRow::getPassengerInitials).map(String::copyValueOf).collect(Collectors.toList()));
        assertEquals('M', seatMapByCategory.get(1).getPassengerInitials()[0]);
    }

    @Test
    public void testGetSeatsMapByRow() throws RemoteException {
        ResponseRow seatMapByRow = seatQueryService.query(TestConstants.FLIGHT_CODE_1, TestConstants.ROW_1);

        assertEquals('M', seatMapByRow.getPassengerInitials()[0]);
    }
}
