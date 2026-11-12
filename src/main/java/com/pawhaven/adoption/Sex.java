package com.pawhaven.adoption;

public enum Sex {
    FEMALE("Female"),
    MALE("Male");

    private final String label;

    Sex(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
