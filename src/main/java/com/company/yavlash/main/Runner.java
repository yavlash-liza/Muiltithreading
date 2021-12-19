package com.company.yavlash.main;

import com.company.yavlash.entity.Bus;
import com.company.yavlash.exception.RouteException;
import com.company.yavlash.parser.BusParser;
import com.company.yavlash.reader.BusReader;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Runner {
    public static void main(String[] args) throws RouteException {
        BusReader busReader = new BusReader();
        BusParser busParser = new BusParser();
        List<String> readBusData = busReader.readBusData("data.txt");
        List<Bus> buses = busParser.parseBuses(readBusData);

        ExecutorService executorService = Executors.newFixedThreadPool(readBusData.size());
        buses.forEach(executorService::execute);
        executorService.shutdown();
    }
}