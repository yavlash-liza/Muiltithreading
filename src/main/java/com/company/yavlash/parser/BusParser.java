package com.company.yavlash.parser;

import com.company.yavlash.entity.Bus;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class BusParser {
    private static final String DELIMITER_REGEX = "/";
    private static final String BUS_STOP_DELIMITER_REGEX = "-";

    public List<Bus> parseBuses(List<String> busesData) {
        return busesData.stream()
                .map(d -> Stream.of(d.split(DELIMITER_REGEX))
                        .map(String::toString)
                        .toList())
                .map(b -> new Bus(Integer.parseInt(b.get(0)), Integer.parseInt(b.get(1)),
                        Arrays.stream(b.get(2).split(BUS_STOP_DELIMITER_REGEX))
                                .map(Long::parseLong)
                                .toList()))
                .toList();
    }
}