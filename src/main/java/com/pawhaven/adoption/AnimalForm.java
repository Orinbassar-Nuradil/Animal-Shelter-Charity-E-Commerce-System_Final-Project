package com.pawhaven.adoption;

import jakarta.validation.constraints.*;

public class AnimalForm {
    @NotNull(message = "Choose a species.")
    private Species species;

    @NotBlank(message = "Enter the animal’s name.")
    @Size(max = 60)
    private String name;

    @NotNull(message = "Enter the age in months.")
    @Min(0) @Max(480)
    private Integer ageMonths;

    @NotNull(message = "Choose a sex.")
    private Sex sex;

    @Size(max = 100)
    private String breed;

    @NotBlank(message = "Write a short introduction.")
    @Size(max = 1500)
    private String description;

    @NotBlank(message = "Add care information.")
    @Size(max = 1000)
    private String careNotes;

    public Species getSpecies() { return species; }
    public void setSpecies(Species species) { this.species = species; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAgeMonths() { return ageMonths; }
    public void setAgeMonths(Integer ageMonths) { this.ageMonths = ageMonths; }

    public Sex getSex() { return sex; }
    public void setSex(Sex sex) { this.sex = sex; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCareNotes() { return careNotes; }
    public void setCareNotes(String careNotes) { this.careNotes = careNotes; }
    AnimalProfile toProfile() {
        String image = species == Species.CAT ? "/images/animals/cat.svg" : "/images/animals/dog.svg";
        return new AnimalProfile(name, ageMonths, sex, breed, description, careNotes, AdoptionStatus.AVAILABLE, image);
    }
}
