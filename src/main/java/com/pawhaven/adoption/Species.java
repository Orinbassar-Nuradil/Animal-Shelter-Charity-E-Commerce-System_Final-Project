package com.pawhaven.adoption;

public enum Species {
    CAT("Cat"),
    DOG("Dog");

    private final String label;

    Species(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
