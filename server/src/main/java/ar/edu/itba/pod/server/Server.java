package ar.edu.itba.pod.server;

import ar.edu.itba.pod.interfaces.FlightManagerService;
import ar.edu.itba.pod.interfaces.SeatManagerService;
import ar.edu.itba.pod.server.service.FlightManagerServiceImpl;

import ar.edu.itba.pod.server.service.SeatManagerServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws RemoteException {
        logger.info("rmi-project Server Starting ...");

        ServerStore store = new ServerStore();

        final FlightManagerService flightManagerService = new FlightManagerServiceImpl();

        final SeatManagerService seatManagerService = new SeatManagerServiceImpl(store);

        final Registry registry = LocateRegistry.getRegistry();

        final Remote remoteFlightManagerService = UnicastRemoteObject.exportObject(flightManagerService, 0);
        final Remote remoteSeatManagerService = UnicastRemoteObject.exportObject(seatManagerService, 0);

        registry.rebind("flightManagerService", remoteFlightManagerService);
        registry.rebind("seatManagerService", remoteSeatManagerService);

    }
}
