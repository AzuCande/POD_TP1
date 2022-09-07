package ar.edu.itba.pod.client.utils;

public enum FlightActions {
    MODELS("models"), FLIGHTS("flights"),
    STATUS("status"), CONFIRM("confirm"),
    CANCEL("cancel"), RETICKETING("reticketing");

    private final String description;

    FlightActions(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
