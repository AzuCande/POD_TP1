package ar.edu.itba.pod.models;

import java.io.Serializable;
import java.util.List;

public class ResponseCancelledList implements Serializable {
    private final int changed;
    private final List<CancelledTicket> unchangedTickets;

    public ResponseCancelledList(int changed, List<CancelledTicket> unchangedTickets) {
        this.changed = changed;
        this.unchangedTickets = unchangedTickets;
    }

    public int getChanged() {
        return changed;
    }

    public List<CancelledTicket> getUnchangedTickets() {
        return unchangedTickets;
    }
}
