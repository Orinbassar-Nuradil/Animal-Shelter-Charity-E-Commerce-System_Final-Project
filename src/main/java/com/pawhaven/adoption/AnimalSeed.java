package com.pawhaven.adoption;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.pawhaven.adoption.Species.*;
import static com.pawhaven.adoption.Sex.*;
import static com.pawhaven.adoption.AdoptionStatus.*;

@Component
public class AnimalSeed implements ApplicationRunner {
    private final AnimalRepository animals;
    private final AdoptionService adoption;

    public AnimalSeed(AnimalRepository animals, AdoptionService adoption) {
        this.animals = animals;
        this.adoption = adoption;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (animals.count() != 0) return;
        add(CAT, "Maple", 24, FEMALE, "Ginger domestic shorthair", AVAILABLE, "maple.jpg",
                "Maple knows exactly which patch of afternoon sun lasts the longest. Once she has had time to get to know you, she likes to sit close enough that one paw rests on your knee.",
                "Offer quiet corners and a gentle introduction to her new home. She enjoys a short brushing session a few times a week.");
        add(DOG, "Milo", 36, MALE, "Golden retriever mix", AVAILABLE, "milo.jpg",
                "Milo brings his favourite rope to the gate when his walking partner arrives. He is happiest taking the long way home, with plenty of time to sniff the leaves.",
                "He enjoys two steady walks each day. A patient companion can help him practise greeting new people with all four paws on the ground.");
        add(CAT, "Luna", 12, FEMALE, "Silver tabby", AVAILABLE, "luna.jpg",
                "Luna watches the birds from her window perch and tells them all about her morning. A feather wand soon brings out her playful side.",
                "Give her a secure window perch, climbing space and daily play. Let her choose when to approach for cuddles.");
        add(DOG, "Otis", 60, MALE, "Hound mix", AVAILABLE, "otis.svg",
                "Otis takes his sniffing duties very seriously. Back at the shelter, he stretches out beside the volunteers while they finish their paperwork.",
                "A comfortable harness and relaxed scent walks suit him. Keep food puzzles simple while he learns a new routine.");
        add(CAT, "Cleo", 48, FEMALE, "Tortoiseshell", ON_HOLD, "cleo.svg",
                "Cleo is a small cat with a wonderfully loud purr. She greets familiar faces by curling her tail around their ankles.",
                "She is taking a quiet break from visits. The team is monitoring her comfort before reopening applications.");
        add(DOG, "Pip", 8, FEMALE, "Terrier mix", AVAILABLE, "pip.svg",
                "Pip is learning that a cardboard box can be a treasure chest full of toys. She checks in often with the volunteer on the other end of her lead.",
                "A consistent routine, short play breaks and reward-based training will help her settle.");
        add(CAT, "Basil", 84, MALE, "Black domestic shorthair", AVAILABLE, "basil.svg",
                "Basil has perfected the art of keeping a reader company. He settles beside an open book and occasionally offers a slow blink.",
                "Keep his bed within easy reach and give him a low-sided litter tray. He prefers a calm household.");
        add(DOG, "Nala", 30, FEMALE, "Shepherd mix", AVAILABLE, "nala.svg",
                "Nala likes solving treat puzzles and returning to her mat for a rest. She has begun to recognise the sound of her favourite volunteer’s bicycle.",
                "She appreciates predictable walks and a place to retreat when visitors arrive.");
        add(CAT, "Peaches", 6, FEMALE, "Cream tabby", AVAILABLE, "peaches.svg",
                "Peaches pounces on felt mice, then carefully lines them up by her blanket. Her curiosity makes every ordinary afternoon a small adventure.",
                "Kitten-proof loose cords and offer several short play sessions every day.");
        add(DOG, "Bruno", 72, MALE, "Labrador mix", ON_HOLD, "bruno.svg",
                "Bruno enjoys a slow stroll followed by a sunny spot by the kitchen door. He is a familiar, much-loved face around the shelter.",
                "He is on a temporary rest plan. The shelter will reassess his availability after his next care review.");
        add(CAT, "Miso", 18, MALE, "Bicolor shorthair", ADOPTED, "miso.svg",
                "Miso won over his new family by climbing into their empty tote bag during a visit. He now has a sunny windowsill of his own.",
                "His family received his feeding and settling-in notes when he went home.");
        add(DOG, "Ruby", 48, FEMALE, "Spaniel mix", ADOPTED, "ruby.svg",
                "Ruby loved to carry a soft toy on every walk. Her adopters now keep a basket of favourites beside their front door.",
                "Her family keeps up her gentle grooming routine and regular walks.");
    }

    private void add(Species species, String name, int age, Sex sex, String breed, AdoptionStatus status,
                     String image, String description, String care) {
        adoption.create(species, new AnimalProfile(name, age, sex, breed, description, care, status, "/images/animals/" + image));
    }
}
