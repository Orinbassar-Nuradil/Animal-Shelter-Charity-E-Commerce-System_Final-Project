package com.pawhaven.patterns.factory;

import com.pawhaven.adoption.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AnimalCreatorTest {
    private final AnimalProfile profile = new AnimalProfile("  Olive  ", 16, Sex.FEMALE, "",
            "Loves a sunny window.", "Introduce other pets slowly.", AdoptionStatus.AVAILABLE, "/images/animals/cat.svg");

    @Test
    void catCreatorMakesACatWithItsCompleteProfile() {
        Animal animal = new CatCreator().createProfile(profile);
        assertThat(animal).isInstanceOf(Cat.class);
        assertThat(animal.getSpecies()).isEqualTo(Species.CAT);
        assertThat(animal.getName()).isEqualTo("Olive");
        assertThat(animal.getAgeMonths()).isEqualTo(16);
        assertThat(animal.getAgeLabel()).isEqualTo("1 year, 4 months");
        assertThat(animal.getSex()).isEqualTo(Sex.FEMALE);
        assertThat(animal.getBreed()).isEqualTo("Mixed breed");
        assertThat(animal.getDescription()).isEqualTo(profile.description());
        assertThat(animal.getCareNotes()).isEqualTo(profile.careNotes());
        assertThat(animal.getStatus()).isEqualTo(AdoptionStatus.AVAILABLE);
        assertThat(animal.getImagePath()).isEqualTo(profile.imagePath());
    }

    @Test
    void dogCreatorMakesADogThroughTheSameCreationOperation() {
        AnimalCreator creator = new DogCreator();
        Animal animal = creator.createProfile(profile);
        assertThat(animal).isInstanceOf(Dog.class);
        assertThat(animal.getSpecies()).isEqualTo(Species.DOG);
        assertThat(animal.getName()).isEqualTo("Olive");
        assertThat(animal.getStatus()).isEqualTo(AdoptionStatus.AVAILABLE);
    }

    @Test
    void incompleteProfileIsRejected() {
        assertThatIllegalArgumentException().isThrownBy(() -> new AnimalProfile(" ", 12, Sex.MALE,
                null, "Story", "Notes", AdoptionStatus.AVAILABLE, "/cat.svg"));
    }
}
