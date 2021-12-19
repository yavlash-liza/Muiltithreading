package com.company.yavlash.exception;

public class RouteException extends Exception{
    public RouteException() {
    }

    public RouteException(String message) {
        super(message);
    }

    public RouteException(String message, Throwable cause) {
        super(message, cause);
    }

    public RouteException(Throwable cause) {
        super(cause);
    }
}