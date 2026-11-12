package com.pawhaven.adoption;

import java.util.Objects;

/** Profile information shared by the concrete animal creators. */
public record AnimalProfile(String name, int ageMonths, Sex sex, String breed,
                            String description, String careNotes, AdoptionStatus status, String imagePath) {
    public AnimalProfile {
        name = required(name, 60, "Name");
        if (ageMonths < 0 || ageMonths > 480) {
            throw new IllegalArgumentException("Age must be between 0 and 480 months.");
        }
        Objects.requireNonNull(sex, "Sex is required.");
        breed = breed == null || breed.isBlank() ? "Mixed breed" : required(breed, 100, "Breed");
        description = required(description, 1500, "Description");
        careNotes = required(careNotes, 1000, "Care notes");
        Objects.requireNonNull(status, "Status is required.");
        imagePath = required(imagePath, 255, "Image");
    }

    private static String required(String value, int limit, String label) {
        if (value == null || value.isBlank() || value.strip().length() > limit) {
            throw new IllegalArgumentException(label + " is missing or too long.");
        }
        return value.strip();
    }
}
