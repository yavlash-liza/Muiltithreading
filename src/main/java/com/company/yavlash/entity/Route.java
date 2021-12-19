package com.company.yavlash.entity;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Route {
    private static final Logger logger = LogManager.getLogger();

    private static Route instance;
    private static final AtomicBoolean stateInstance = new AtomicBoolean(false);
    private static final ReentrantLock lockInstance = new ReentrantLock(true);

    private static final String RESOURCE_FILE_NAME = "data/routeData.properties";
    private static final String BUS_STOP_AMOUNT_PROPERTY = "bus_stop_amount";
    private static final String BUS_STOP_CAPACITY_PROPERTY = "bus_stop_capacity";
    private static final int DEFAULT_BUS_STOP_AMOUNT = 5;
    private static final int DEFAULT_BUS_STOP_CAPACITY = 2;
    private static final Lock lock = new ReentrantLock();
    private final Map<Long, Condition> conditions = new HashMap<>();
    private final Map<Long, Semaphore> semaphores = new HashMap<>();
    private final List<BusStop> availableBusStops = new ArrayList<>();
    private final List<BusStop> occupiedBusStops = new ArrayList<>();
    private int busStopAmount;

    public static Route getInstance() {
        if (!stateInstance.get()) {
            lockInstance.lock();
            try {
                if (instance == null) {
                    instance = new Route();
                    stateInstance.set(true);
                }
            } finally {
                lockInstance.unlock();
            }
        }
        return instance;
    }

    private Route() {
        int busStopCapacity = retrieveBusStopProperties();
        for (int i = 0; i < busStopAmount; i++) {
            BusStop busStop = new BusStop(busStopCapacity);
            availableBusStops.add(busStop);
            conditions.put(busStop.getBusStopId(), lock.newCondition());
            semaphores.put(busStop.getBusStopId(), new Semaphore(busStop.getMaxBusCapacity(), true));
        }
    }

    public BusStop obtainBusStop(long busStopNumber, Bus bus) {
        try {
            lockInstance.lock();
            bus.setState(Bus.State.WAITING);
            try {
                while (bus.getState().equals(Bus.State.WAITING)) {
                    if (semaphores.get(busStopNumber).tryAcquire()) {
                        bus.setState(Bus.State.RUNNING);
                    } else {
                        logger.log(Level.INFO,"Bus stop {} is currently occupied", busStopNumber);
                        conditions.get(busStopNumber).await();
                    }
                }
            } catch (InterruptedException exception) {
                logger.log(Level.ERROR,"Error was found while processing a bus route: " + exception);
                Thread.currentThread().interrupt();
            }
            BusStop busStop = availableBusStops.stream()
                    .filter(stream -> stream.getBusStopId() == busStopNumber)
                    .findFirst()
                    .orElse(new BusStop());
            if (semaphores.get(busStopNumber).availablePermits() == 0) {
                availableBusStops.remove(busStop);
                occupiedBusStops.add(busStop);
            }
            return busStop;
        } finally {
            lockInstance.unlock();
        }
    }

    public void releaseBusStop(BusStop busStop) {
        try {
            lockInstance.lock();
            semaphores.get(busStop.getBusStopId()).release();
            if (!availableBusStops.contains(busStop)) {
                occupiedBusStops.remove(busStop);
                availableBusStops.add(busStop);
                conditions.get(busStop.getBusStopId()).signalAll();
                logger.log(Level.INFO,"Bus stop {} is available now", busStop.getBusStopId());
            }
        } finally {
            lockInstance.unlock();
        }
    }

    public int getPeopleOffBus(Bus bus) {
        int decreasedPeopleAmount = new Random().nextInt(bus.getCurrentPeopleAmount() + 1);
        bus.setCurrentPeopleAmount(bus.getCurrentPeopleAmount() - decreasedPeopleAmount);
        logger.log(Level.INFO,"{} people got off the bus {}", decreasedPeopleAmount, bus.getBusId());
        return decreasedPeopleAmount;
    }

    public int getPeopleOnBus(Bus bus, BusStop busStop) {
        int availableSeatsAmount = bus.getMaxCapacity() - bus.getCurrentPeopleAmount();
        int maxPossiblePeopleAmount = Math.min(busStop.getCurrentPeopleAmount(), availableSeatsAmount);
        int increasedPeopleAmount = new Random().nextInt(maxPossiblePeopleAmount + 1);
        bus.setCurrentPeopleAmount(bus.getCurrentPeopleAmount() + increasedPeopleAmount);
        logger.log(Level.INFO,"{} people got on the bus {}", increasedPeopleAmount, bus.getBusId());
        return increasedPeopleAmount;
    }

    public int retrieveBusStopProperties() {
        int busStopCapacity;
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream(RESOURCE_FILE_NAME));
            busStopAmount = Integer.parseInt(properties.getProperty(BUS_STOP_AMOUNT_PROPERTY,
                    String.valueOf(DEFAULT_BUS_STOP_AMOUNT)));
            busStopCapacity = Integer.parseInt(properties.getProperty(BUS_STOP_CAPACITY_PROPERTY,
                    String.valueOf(DEFAULT_BUS_STOP_CAPACITY)));
        } catch (IOException exception) {
            logger.log(Level.ERROR,"Error was occurred while reading file \"{}\"", RESOURCE_FILE_NAME);
            logger.log(Level.WARN,"Route will be initialised with default values of bus stop amount({}) and bus stop capacity({})",
                    DEFAULT_BUS_STOP_AMOUNT, DEFAULT_BUS_STOP_CAPACITY);
            busStopAmount = DEFAULT_BUS_STOP_AMOUNT;
            busStopCapacity = DEFAULT_BUS_STOP_CAPACITY;
        }
        return busStopCapacity;
    }
}