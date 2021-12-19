package com.company.yavlash.entity;

import com.company.yavlash.util.BusIdGenerator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class Bus implements Runnable {
    private static final Logger logger = LogManager.getLogger();
    private final long busId;
    private final List<Long> busStopNumbers;
    private final int MAX_CAPACITY;
    private int currentPeopleAmount;
    private State state;

    public enum State {
        WAITING, RUNNING, COMPLETED
    }

    public Bus(int maxCapacity, int currentPeopleAmount, List<Long> busStopNumbers) {
        MAX_CAPACITY = maxCapacity;
        this.currentPeopleAmount = currentPeopleAmount;
        this.busStopNumbers = busStopNumbers;
        busId = BusIdGenerator.generateId();
    }

    public long getBusId() {
        return busId;
    }

    public int getCurrentPeopleAmount() {
        return currentPeopleAmount;
    }

    public void setCurrentPeopleAmount(int currentPeopleAmount) {
        this.currentPeopleAmount = currentPeopleAmount;
    }

    public int getMaxCapacity() {
        return MAX_CAPACITY;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public void run() {
        logger.log(Level.INFO,"Bus {} started its route", busId);

        Route route = Route.getInstance();
        for (long busStopNumber : busStopNumbers) {
            BusStop busStop = new BusStop();
            try {
                busStop = route.obtainBusStop(busStopNumber, this);
                logger.log(Level.INFO,"Bus {} arrived to a bus stop {} with {} people",
                        busId, busStop.getBusStopId(), currentPeopleAmount);
                busStop.processBus(this);
            } finally {
                route.releaseBusStop(busStop);
                logger.log(Level.INFO, "Bus {} drove away from a bus stop {} with {} people",
                        busId, busStop.getBusStopId(), currentPeopleAmount);
            }
        }
        logger.log(Level.INFO,"Bus {} completed it's route", busId);
        state = State.COMPLETED;
    }
}