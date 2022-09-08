package ar.edu.itba.pod.models;

import java.io.Serializable;
import java.util.Objects;

public class Ticket implements Serializable {
    private final RowCategory category;
    private final String passenger;
    private final String destination;
    private Integer row;
    private Character col;

    public Ticket(RowCategory category, String passenger, String destination) {
        this.category = category;
        this.passenger = passenger;
        this.destination = destination;
    }

    public void setSeat(Integer row, Character col) {
        this.row = row;
        this.col = col;
    }

    public Integer getRow() {
        return row;
    }

    public Character getCol() {
        return col;
    }

    public String getPassenger() {
        return passenger;
    }

    public RowCategory getCategory() {
        return category;
    }

    public String getDestination(){
        return destination;
    }
}
