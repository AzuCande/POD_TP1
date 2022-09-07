package ar.edu.itba.pod.server.models;

import ar.edu.itba.pod.models.RowCategory;

public class Ticket {
    private final String id;
    private final RowCategory category;
    private final String passenger;

    public Ticket(String id, RowCategory category, String passenger) {
        this.id = id;
        this.category = category;
        this.passenger = passenger;
    }

    public String getId() {
        return id;
    }

    public String getPassenger() {
        return passenger;
    }

    public RowCategory getCategory() {
        return category;
    }
}
