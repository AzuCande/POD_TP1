package ar.edu.itba.pod.server.models;

public class Seat {
    private final String letter;
    private String passengerName;

    public Seat(String letter) {
        this.letter = letter;
    }

    public void assign(String name){
        this.passengerName = name;
    }

    public boolean isAvailable(){
        return this.passengerName == null;
    }

    public void remove(){
        this.passengerName = null;
    }
}
