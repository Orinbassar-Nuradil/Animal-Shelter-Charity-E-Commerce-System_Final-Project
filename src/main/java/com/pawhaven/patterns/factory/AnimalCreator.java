package com.pawhaven.patterns.factory;

import com.pawhaven.adoption.*;

/** The shared creation operation delegates concrete animal construction to its Factory Method. */
public abstract class AnimalCreator {
    public abstract Species species();

    public final Animal createProfile(AnimalProfile profile) {
        return createAnimal(profile);
    }

    protected abstract Animal createAnimal(AnimalProfile profile);
}
