package ru.cmc.msu.genealogy.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "person")
@Getter
@Setter
@NoArgsConstructor
public class Person implements CommonEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id", nullable = false)
    private Long id;

    @Column(name = "person_name", nullable = false)
    private String name;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "date_of_birth")
    private Integer dateOfBirth;

    @Column(name = "date_of_death")
    private Integer dateOfDeath;

    @Column(name = "characteristics")
    private String characteristics;

    public Person(String name, String gender) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.gender = Objects.requireNonNull(gender, "gender must not be null");
    }

    public Person(Long id, String name, String gender, Integer dateOfBirth, Integer dateOfDeath, String characteristics) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.gender = Objects.requireNonNull(gender, "gender must not be null");
        this.dateOfBirth = dateOfBirth;
        this.dateOfDeath = dateOfDeath;
        this.characteristics = characteristics;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", dateOfDeath=" + dateOfDeath +
                ", characteristics='" + characteristics + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Person)) {
            return false;
        }
        Person person = (Person) o;
        return Objects.equals(id, person.id)
                && Objects.equals(name, person.name)
                && Objects.equals(gender, person.gender)
                && Objects.equals(dateOfBirth, person.dateOfBirth)
                && Objects.equals(dateOfDeath, person.dateOfDeath)
                && Objects.equals(characteristics, person.characteristics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, gender, dateOfBirth, dateOfDeath, characteristics);
    }
}
