package com.pawhaven.adoption;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class AdoptionDecisionTest {
    @Autowired AdoptionService service;
    @Autowired ApplicationRepository applications;
    @Autowired AnimalRepository animals;

    @Test
    void competingApprovalsProduceOnePlacementAndNeitherDecisionCanBeRepeated() throws Exception {
        Animal animal = newAnimal();
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            AdoptionApplication one = service.apply(animal.getId(), form("one@example.test"));
            AdoptionApplication two = service.apply(animal.getId(), form("two@example.test"));
            CountDownLatch start = new CountDownLatch(1);
            Future<Boolean> first = workers.submit(() -> approve(one.getId(), start));
            Future<Boolean> second = workers.submit(() -> approve(two.getId(), start));
            start.countDown();
            assertThat(first.get(15, SECONDS)).isNotEqualTo(second.get(15, SECONDS));
            assertThat(applications.findByAnimalIdAndStatus(animal.getId(), ApplicationStatus.APPROVED)).hasSize(1);
            assertThat(applications.findByAnimalIdAndStatus(animal.getId(), ApplicationStatus.REJECTED)).hasSize(1);
            assertThat(service.animal(animal.getId()).getStatus()).isEqualTo(AdoptionStatus.ADOPTED);
            assertThatThrownBy(() -> service.decide(one.getId(), ApplicationStatus.REJECTED)).isInstanceOf(AdoptionConflict.class);
            assertThatThrownBy(() -> service.changeAvailability(animal.getId(), AdoptionStatus.AVAILABLE))
                    .isInstanceOf(AdoptionConflict.class);
        } finally {
            workers.shutdownNow();
            discard(animal);
        }
    }

    @Test
    void duplicateSubmissionsAreBlockedAndHoldingAnAnimalPausesNewApplicationsAndApproval() {
        Animal animal = newAnimal();
        try {
            AdoptionApplication pending = service.apply(animal.getId(), form("Guest@example.test"));
            assertThatThrownBy(() -> service.apply(animal.getId(), form("guest@example.test"))).isInstanceOf(AdoptionConflict.class);
            service.changeAvailability(animal.getId(), AdoptionStatus.ON_HOLD);
            assertThatThrownBy(() -> service.apply(animal.getId(), form("second@example.test"))).isInstanceOf(AdoptionConflict.class);
            assertThatThrownBy(() -> service.decide(pending.getId(), ApplicationStatus.APPROVED)).isInstanceOf(AdoptionConflict.class);
            assertThat(applications.findById(pending.getId()).orElseThrow().getStatus()).isEqualTo(ApplicationStatus.PENDING);
            service.changeAvailability(animal.getId(), AdoptionStatus.AVAILABLE);
            service.decide(pending.getId(), ApplicationStatus.REJECTED);
            assertThat(service.animal(animal.getId()).getStatus()).isEqualTo(AdoptionStatus.AVAILABLE);
        } finally {
            discard(animal);
        }
    }

    private boolean approve(long applicationId, CountDownLatch start) throws InterruptedException {
        start.await();
        try {
            service.decide(applicationId, ApplicationStatus.APPROVED);
            return true;
        } catch (AdoptionConflict alreadyPlaced) {
            return false;
        }
    }

    private Animal newAnimal() {
        return service.create(Species.CAT, new AnimalProfile("Fern", 17, Sex.FEMALE, "Tabby", "Likes company.",
                "Needs a calm routine.", AdoptionStatus.AVAILABLE, "/images/animals/cat.svg"));
    }

    private static AdoptionForm form(String email) {
        AdoptionForm form = new AdoptionForm();
        form.setApplicantName("Demo Guest");
        form.setEmail(email);
        form.setPhone("+7 700 555 0100");
        form.setHousing("Apartment with a secure balcony.");
        form.setReason("We can provide a quiet home.");
        return form;
    }

    private void discard(Animal animal) {
        for (ApplicationStatus status : ApplicationStatus.values()) {
            applications.deleteAll(applications.findByAnimalIdAndStatus(animal.getId(), status));
        }
        animals.deleteById(animal.getId());
    }
}
