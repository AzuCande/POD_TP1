package ar.edu.itba.pod.models;

import java.io.Serializable;
import java.util.ArrayList;

public class ResponseCancelledList implements Serializable {
    private final int changed;
    private final ArrayList<CancelledTicket> unchangedTickets;

    public ResponseCancelledList(int changed, ArrayList<CancelledTicket> unchangedTickets) {
        this.changed = changed;
        this.unchangedTickets = unchangedTickets;
    }

    public int getChanged() {
        return changed;
    }

    public ArrayList<CancelledTicket> getUnchangedTickets() {
        return unchangedTickets;
    }
}
