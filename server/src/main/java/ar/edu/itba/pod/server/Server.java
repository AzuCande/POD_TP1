package ar.edu.itba.pod.server;

import ar.edu.itba.pod.interfaces.FlightManagerService;
import ar.edu.itba.pod.server.service.FlightManager;

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

        final FlightManagerService flightManager = new FlightManager();

        final Registry registry = LocateRegistry.getRegistry();

        final Remote remote = UnicastRemoteObject.exportObject(flightManager, 0);

        registry.rebind("flightManagerService", remote);


    }
}
