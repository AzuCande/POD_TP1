package ar.edu.itba.pod.server.utils;

import ar.edu.itba.pod.models.RowCategory;
import ar.edu.itba.pod.models.Ticket;
import ar.edu.itba.pod.server.models.Flight;
import java.util.Comparator;

public class FlightComparator implements Comparator<Flight> {
    private final Ticket ticket;

    public FlightComparator(Ticket ticket) {
        this.ticket = ticket;
    }

    @Override
    public int compare(Flight flight1, Flight flight2) {
        int cat1 = flight1.getAvailableCategory(ticket.getCategory());
        int cat2 = flight2.getAvailableCategory(ticket.getCategory());
        if (cat1 != cat2)
            return cat2 - cat1;

        RowCategory category = RowCategory.values()[cat1];
        return flight2.getAvailableByCategory(category) - flight1.getAvailableByCategory(category);
    }
}