package com.company.yavlash.util;

public class BusIdGenerator {
    private static Long id;
    static {
        id = 0L;
    }

    private BusIdGenerator() {
    }

    public static Long generateId() {
        return ++id;
    }
}
