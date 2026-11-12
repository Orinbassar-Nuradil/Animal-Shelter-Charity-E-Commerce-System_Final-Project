package com.pawhaven.adoption;

import com.pawhaven.patterns.factory.AnimalCreator;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Validated
@Transactional(readOnly = true)
public class AdoptionService {
    private final AnimalRepository animals;
    private final ApplicationRepository applications;
    private final Map<Species, AnimalCreator> creators = new EnumMap<>(Species.class);

    public AdoptionService(AnimalRepository animals, ApplicationRepository applications, List<AnimalCreator> creators) {
        this.animals = animals;
        this.applications = applications;
        for (AnimalCreator creator : creators) {
            if (this.creators.put(creator.species(), creator) != null) {
                throw new IllegalStateException("Multiple creators for " + creator.species());
            }
        }
    }

    public List<Animal> catalog(Species species, AdoptionStatus status) {
        return animals.findAll((root, query, cb) -> {
            List<Predicate> filters = new ArrayList<>();
            if (species != null) {
                filters.add(cb.equal(root.type(), species == Species.CAT ? Cat.class : Dog.class));
            }
            if (status != null) filters.add(cb.equal(root.get("status"), status));
            return cb.and(filters.toArray(Predicate[]::new));
        }, Sort.by("id"));
    }

    public Animal animal(long id) {
        return animals.findById(id).orElseThrow(() -> missing("Animal"));
    }

    @Transactional
    public Animal addAnimal(@Valid AnimalForm form) {
        return create(form.getSpecies(), form.toProfile());
    }

    @Transactional
    public Animal create(Species species, AnimalProfile profile) {
        AnimalCreator creator = Objects.requireNonNull(creators.get(species), "Unknown species.");
        return animals.save(creator.createProfile(profile));
    }

    @Transactional
    public void changeAvailability(long animalId, AdoptionStatus status) {
        Animal animal = lockAnimal(animalId);
        if (animal.getStatus() == AdoptionStatus.ADOPTED || status == AdoptionStatus.ADOPTED) {
            throw new AdoptionConflict("An adopted placement is recorded through an approved application.");
        }
        animal.changeStatus(Objects.requireNonNull(status));
    }

    @Transactional
    public AdoptionApplication apply(long animalId, @Valid AdoptionForm form) {
        Animal animal = lockAnimal(animalId);
        if (animal.getStatus() != AdoptionStatus.AVAILABLE) {
            throw new AdoptionConflict("This animal is currently unavailable for applications.");
        }
        if (applications.existsByAnimalIdAndEmailIgnoreCaseAndStatus(animalId, form.getEmail().strip(), ApplicationStatus.PENDING)) {
            throw new AdoptionConflict("An application from this email is already pending for " + animal.getName() + ".");
        }
        return applications.save(new AdoptionApplication(animal, form));
    }

    public List<AdoptionApplication> applications() {
        return applications.findAllByOrderByIdDesc();
    }

    @Transactional
    public void decide(long applicationId, ApplicationStatus decision) {
        // Every adoption write locks the same animal before reading mutable application state.
        Long animalId = applications.findAnimalId(applicationId).orElseThrow(() -> missing("Application"));
        Animal animal = lockAnimal(animalId);
        AdoptionApplication application = applications.findById(applicationId).orElseThrow(() -> missing("Application"));
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new AdoptionConflict("This application has already been reviewed.");
        }
        if (decision == ApplicationStatus.REJECTED) {
            application.decide(decision);
            return;
        }
        if (decision != ApplicationStatus.APPROVED || animal.getStatus() != AdoptionStatus.AVAILABLE) {
            throw new AdoptionConflict("Approval requires a pending application and an available animal.");
        }
        // Resolve competing requests in the same transaction as this placement.
        applications.findByAnimalIdAndStatus(animalId, ApplicationStatus.PENDING)
                .forEach(pending -> pending.decide(pending.getId().equals(applicationId)
                        ? ApplicationStatus.APPROVED : ApplicationStatus.REJECTED));
        animal.changeStatus(AdoptionStatus.ADOPTED);
    }

    private Animal lockAnimal(long id) {
        return animals.lockById(id).orElseThrow(() -> missing("Animal"));
    }

    private ResponseStatusException missing(String type) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, type + " was not found.");
    }
}
