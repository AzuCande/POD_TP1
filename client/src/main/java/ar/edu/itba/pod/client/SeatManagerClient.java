package ar.edu.itba.pod.client;


import ar.edu.itba.pod.client.parsers.SeatManagerParser;
import ar.edu.itba.pod.interfaces.SeatManagerService;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class SeatManagerClient {
    private static final Logger logger = LoggerFactory.getLogger(SeatManagerClient.class);

    public static void main(String[] args) throws MalformedURLException, NotBoundException, RemoteException {
        SeatManagerParser parser = new SeatManagerParser();
        parser.parse();

        // logger.info("Flight Notifications Client Starting ...");

        SeatManagerService service =
                (SeatManagerService) Naming.lookup("//127.0.0.1:1099/" + SeatManagerService.class.getName());

        switch (parser.getAction().get()) {
            case STATUS:
                service.isAvailable(parser.getFlightCode(), parser.getRow().orElseThrow(RuntimeException::new),
                        parser.getColumn().orElseThrow(RuntimeException::new));
                break;
            case ASSIGN:
                service.assign(parser.getFlightCode(), parser.getPassenger().orElseThrow(RuntimeException::new),
                        parser.getRow().orElseThrow(RuntimeException::new),
                        parser.getColumn().orElseThrow(RuntimeException::new));
                break;
            case ALTERNATIVES:
                service.listAlternativeFlights(parser.getFlightCode(),
                        parser.getPassenger().orElseThrow(RuntimeException::new));
                break;
            case MOVE:
                service.changeSeat(parser.getFlightCode(),
                        parser.getPassenger().orElseThrow(RuntimeException::new),
                        parser.getRow().orElseThrow(RuntimeException::new),
                        parser.getColumn().orElseThrow(RuntimeException::new));
                break;
            case CHANGE_TICKET:
                service.changeTicket(parser.getPassenger().orElseThrow(RuntimeException::new),
                        parser.getOriginalFlightCode().orElseThrow(RuntimeException::new),
                        parser.getFlightCode());
                break;
            default:
                throw new IllegalArgumentException("Invalid operation");
        }
        // service.query(parser.getFlight(), "nada");
        // TODO: parametros y escribir respuesta a CSV
    }
}
