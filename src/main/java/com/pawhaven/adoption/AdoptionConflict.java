package com.pawhaven.adoption;

/** The selected animal or application has already changed state. */
public class AdoptionConflict extends RuntimeException {
    public AdoptionConflict(String message) { super(message); }
}
