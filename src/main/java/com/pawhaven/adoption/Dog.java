package com.pawhaven.adoption;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("DOG")
public class Dog extends Animal {
    protected Dog() {}

    public Dog(AnimalProfile profile) { super(profile); }

    @Override
    public Species getSpecies() { return Species.DOG; }
}
