package com.pawhaven.adoption;

public enum AdoptionStatus {
    AVAILABLE("Available"),
    ON_HOLD("On hold"),
    ADOPTED("Adopted");

    private final String label;

    AdoptionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
