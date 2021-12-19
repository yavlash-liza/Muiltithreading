package com.company.yavlash.util;

public class BusStopIdGenerator {
    private static Long id;
    static {
        id = 0L;
    }

    private BusStopIdGenerator() {
    }

    public static Long generateId() {
        return ++id;
    }
}
