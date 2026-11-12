package com.pawhaven.adoption;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<AdoptionApplication, Long> {
    long countByStatus(ApplicationStatus status);
    boolean existsByAnimalIdAndEmailIgnoreCaseAndStatus(Long animalId, String email, ApplicationStatus status);

    @EntityGraph(attributePaths = "animal")
    List<AdoptionApplication> findAllByOrderByIdDesc();

    List<AdoptionApplication> findByAnimalIdAndStatus(Long animalId, ApplicationStatus status);

    @Query("select a.animal.id from AdoptionApplication a where a.id = :id")
    Optional<Long> findAnimalId(@Param("id") Long id);
}
