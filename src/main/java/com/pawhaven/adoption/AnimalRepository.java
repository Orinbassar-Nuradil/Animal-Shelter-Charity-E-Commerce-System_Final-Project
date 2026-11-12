package com.pawhaven.adoption;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<Animal, Long>, JpaSpecificationExecutor<Animal> {
    List<Animal> findTop3ByStatusOrderByIdAsc(AdoptionStatus status);
    long countByStatus(AdoptionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Animal a where a.id = :id")
    Optional<Animal> lockById(@Param("id") Long id);
}
