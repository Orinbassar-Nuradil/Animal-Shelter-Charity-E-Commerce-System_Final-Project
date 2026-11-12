package com.pawhaven.adoption;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CAT")
public class Cat extends Animal {
    protected Cat() {}

    public Cat(AnimalProfile profile) { super(profile); }

    @Override
    public Species getSpecies() { return Species.CAT; }
}
