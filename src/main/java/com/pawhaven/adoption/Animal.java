package com.pawhaven.adoption;

import jakarta.persistence.*;

@Entity
@Table(name = "animals")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "species", length = 8)
public abstract class Animal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String name;
    @Column(nullable = false)
    private int ageMonths;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Sex sex;
    @Column(nullable = false, length = 100)
    private String breed;
    @Column(nullable = false, length = 1500)
    private String description;
    @Column(nullable = false, length = 1000)
    private String careNotes;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AdoptionStatus status;
    @Column(nullable = false)
    private String imagePath;

    protected Animal() {}

    protected Animal(AnimalProfile profile) {
        name = profile.name();
        ageMonths = profile.ageMonths();
        sex = profile.sex();
        breed = profile.breed();
        description = profile.description();
        careNotes = profile.careNotes();
        status = profile.status();
        imagePath = profile.imagePath();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getAgeMonths() { return ageMonths; }
    public Sex getSex() { return sex; }
    public String getBreed() { return breed; }
    public String getDescription() { return description; }
    public String getCareNotes() { return careNotes; }
    public AdoptionStatus getStatus() { return status; }
    public String getImagePath() { return imagePath; }
    public abstract Species getSpecies();

    public String getAgeLabel() {
        if (ageMonths < 12) return ageMonths + (ageMonths == 1 ? " month" : " months");
        int years = ageMonths / 12;
        int months = ageMonths % 12;
        return years + (years == 1 ? " year" : " years")
                + (months == 0 ? "" : ", " + months + (months == 1 ? " month" : " months"));
    }

    void changeStatus(AdoptionStatus newStatus) { status = newStatus; }
}
