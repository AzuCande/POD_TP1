package ar.edu.itba.pod.models;

public enum RowCategory {
    ECONOMY(0),
    PREMIUM_ECONOMY(1),
    BUSINESS(2);

    private final int value;

    RowCategory(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}