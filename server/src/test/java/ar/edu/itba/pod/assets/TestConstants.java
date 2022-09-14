package ar.edu.itba.pod.assets;

import ar.edu.itba.pod.models.PlaneModel;
import ar.edu.itba.pod.models.RowCategory;
import ar.edu.itba.pod.models.Ticket;
import ar.edu.itba.pod.server.models.Flight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestConstants {

    public static final String PLANE_MODEL_STR_1 = "Boeing 747";
    public static final String PLANE_MODEL_STR_2 = "Airbus A123";
    public static final String PLANE_MODEL_STR_3 = "Boeing 787";

    public static final String FLIGHT_CODE_1 = "AA100";
    public static final String FLIGHT_CODE_2 = "AA200";
    public static final String FLIGHT_CODE_3 = "AA300";
    public static final String DESTINATION_1 = "JFK";
    public static final String DESTINATION_2 = "ICN";

    public static Map<String, int[]> SEAT_CATEGORIES = new HashMap<String, int[]>() {{
        put("BUSINESS", new int[] {2, 3});
        put("PREMIUM_ECONOMY", new int[] {3, 3});
        put("ECONOMY", new int[] {20, 10});
    }};

    public static Map<String, int[]> EMPTY_SEAT_CATEGORIES = new HashMap<String, int[]>() {{
        put("BUSINESS", new int[] {1, 1});
        put("PREMIUM_ECONOMY", new int[] {1, 1});
        put("ECONOMY", new int[] {1, 1});
    }};

    public static final PlaneModel PLANE_MODEL_1 = new PlaneModel(PLANE_MODEL_STR_1, SEAT_CATEGORIES);
    public static final PlaneModel PLANE_MODEL_2 = new PlaneModel(PLANE_MODEL_STR_2, SEAT_CATEGORIES);
    public static final PlaneModel PLANE_MODEL_3 = new PlaneModel(PLANE_MODEL_STR_3, SEAT_CATEGORIES);

    public static final String PASSENGER_1 = "Matias Lombardi";
    public static final String PASSENGER_2 = "Azul Kim";
    public static final String PASSENGER_3 = "Patrick Dey";
    public static final String PASSENGER_4 = "Franco Meola";
    public static final String PASSENGER_5 = "Santos Rosati";
    public static final String PASSENGER_6 = "Uriel Mihura";
    public static final String PASSENGER_7 = "Marcelo Turrin";

    public static final Ticket TICKET_1 = new Ticket(RowCategory.BUSINESS, PASSENGER_1, DESTINATION_1);
    public static final Ticket TICKET_2 = new Ticket(RowCategory.PREMIUM_ECONOMY, PASSENGER_2, DESTINATION_1);
    public static final Ticket TICKET_3 = new Ticket(RowCategory.ECONOMY, PASSENGER_3, DESTINATION_1);
    public static final Ticket TICKET_4 = new Ticket(RowCategory.ECONOMY, PASSENGER_4, DESTINATION_1);
    public static final Ticket TICKET_5 = new Ticket(RowCategory.BUSINESS, PASSENGER_5, DESTINATION_2);
    public static final Ticket TICKET_6 = new Ticket(RowCategory.ECONOMY, PASSENGER_6, DESTINATION_2);
    public static final Ticket TICKET_7 = new Ticket(RowCategory.BUSINESS, PASSENGER_1, DESTINATION_2);
    public static final Ticket TICKET_8 = new Ticket(RowCategory.PREMIUM_ECONOMY, PASSENGER_3, DESTINATION_2);
    public static final Ticket TICKET_9 = new Ticket(RowCategory.ECONOMY, PASSENGER_5, DESTINATION_2);
    public static final Ticket TICKET_10 = new Ticket(RowCategory.BUSINESS, PASSENGER_7, DESTINATION_2);

    public static final List<Ticket> TICKETS_1 = new ArrayList<Ticket>() {{
        add(TICKET_1);
        add(TICKET_2);
        add(TICKET_3);
        add(TICKET_4);
    }};
    public static final List<Ticket> TICKETS_2 = new ArrayList<Ticket>() {{
        add(TICKET_5);
        add(TICKET_6);
    }};

    public static final List<Ticket> TICKETS_3 = new ArrayList<Ticket>() {{
        add(TICKET_1);
        add(TICKET_2);
    }};

    public static final List<Ticket> TICKETS_4 = new ArrayList<Ticket>() {{
        add(TICKET_3);
        add(TICKET_4);
    }};

    public static final List<Ticket> TICKETS_5 = new ArrayList<Ticket>() {{
        add(TICKET_7);
        add(TICKET_8);
        add(TICKET_9);
    }};

    public static final List<Ticket> TICKETS_6 = new ArrayList<Ticket>() {{
        add(TICKET_10);
    }};

    public static final int ROW_0 = 0;
    public static final int ROW_1 = 1;
    public static final int ROW_2 = 2;
    public static final int ROW_3 = 3;
    public static final int ROW_5 = 5;
    public static final int ROW_6 = 6;
    public static final char SEAT_1 = 'A';
    public static final char SEAT_2 = 'B';
    public static final int ROWS_NUM_1 = 25;

    public static final Flight FLIGHT_1 = new Flight(PLANE_MODEL_1, FLIGHT_CODE_1, DESTINATION_1, TICKETS_1);
}
