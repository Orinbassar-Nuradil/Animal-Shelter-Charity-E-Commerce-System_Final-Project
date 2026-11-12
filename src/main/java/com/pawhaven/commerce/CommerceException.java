package com.pawhaven.commerce;

/** An expected commerce rule violation that can be displayed to the visitor. */
public class CommerceException extends RuntimeException {
    public CommerceException(String message) {
        super(message);
    }
}
