package com.pawhaven.patterns.factory;

import com.pawhaven.adoption.*;
import org.springframework.stereotype.Component;

@Component
public class DogCreator extends AnimalCreator {
    @Override
    public Species species() { return Species.DOG; }

    @Override
    protected Animal createAnimal(AnimalProfile profile) { return new Dog(profile); }
}
