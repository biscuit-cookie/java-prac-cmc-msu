package ru.cmc.msu.genealogy.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "person_place")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonPlace implements CommonEntity<PersonPlaceId> {

    @EmbeddedId
    private PersonPlaceId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("personId")
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("placeId")
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    public PersonPlace(Person person, Place place) {
        this.person = Objects.requireNonNull(person, "person must not be null");
        this.place = Objects.requireNonNull(place, "place must not be null");
        this.id = new PersonPlaceId(place.getId(), person.getId());
    }

    @Override
    public String toString() {
        return "PersonPlace{" +
                "id=" + id +
                ", person=" + person +
                ", place=" + place +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonPlace)) {
            return false;
        }
        PersonPlace that = (PersonPlace) o;
        return Objects.equals(id, that.id)
                && Objects.equals(person, that.person)
                && Objects.equals(place, that.place);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, person, place);
    }
}
