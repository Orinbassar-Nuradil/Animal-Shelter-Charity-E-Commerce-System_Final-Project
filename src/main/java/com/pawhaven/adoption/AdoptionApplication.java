package com.pawhaven.adoption;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Locale;

@Entity
@Table(name = "adoption_applications")
public class AdoptionApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Animal animal;
    @Column(nullable = false, length = 80)
    private String applicantName;
    @Column(nullable = false, length = 254)
    private String email;
    @Column(nullable = false, length = 32)
    private String phone;
    @Column(nullable = false, length = 1000)
    private String housing;
    @Column(nullable = false, length = 1000)
    private String reason;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ApplicationStatus status;
    @Column(nullable = false)
    private Instant submittedAt;

    protected AdoptionApplication() {}

    AdoptionApplication(Animal animal, AdoptionForm form) {
        this.animal = animal;
        applicantName = form.getApplicantName().strip();
        email = form.getEmail().strip().toLowerCase(Locale.ROOT);
        phone = form.getPhone().strip();
        housing = form.getHousing().strip();
        reason = form.getReason().strip();
        status = ApplicationStatus.PENDING;
        submittedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Animal getAnimal() { return animal; }
    public String getApplicantName() { return applicantName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getHousing() { return housing; }
    public String getReason() { return reason; }
    public ApplicationStatus getStatus() { return status; }
    public Instant getSubmittedAt() { return submittedAt; }
    void decide(ApplicationStatus decision) { status = decision; }
}
