package com.pawhaven.patterns.factory;

import com.pawhaven.adoption.*;
import org.springframework.stereotype.Component;

@Component
public class CatCreator extends AnimalCreator {
    @Override
    public Species species() { return Species.CAT; }

    @Override
    protected Animal createAnimal(AnimalProfile profile) { return new Cat(profile); }
}
