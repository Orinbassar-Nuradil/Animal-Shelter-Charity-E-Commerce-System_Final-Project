package com.pawhaven.patterns.factory;

import com.pawhaven.adoption.Animal;
import com.pawhaven.adoption.AnimalProfile;
import com.pawhaven.adoption.Species;


public abstract class AnimalCreator {


    public abstract Species species();
    public final Animal createProfile(AnimalProfile profile) {
        return createAnimal(profile);
    }
    protected abstract Animal createAnimal(AnimalProfile profile);
}
