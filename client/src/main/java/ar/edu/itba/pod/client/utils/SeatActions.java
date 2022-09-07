package ar.edu.itba.pod.client.utils;

public enum SeatActions {
    STATUS("status"), ASSIGN("assign"),
    MOVE("move"), ALTERNATIVES("alternatives"),
    CHANGE_TICKET("changeTicket");

    private final String description;

    SeatActions(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
