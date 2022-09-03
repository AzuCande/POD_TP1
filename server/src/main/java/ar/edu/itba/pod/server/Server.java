package ar.edu.itba.pod.server;

import ar.edu.itba.pod.server.service.FlightManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;


public class Server {
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws FileNotFoundException {
        logger.info("rmi-project Server Starting ...");

        FlightManager flightManager = new FlightManager();
        flightManager.addPlaneModel();

    }
}
