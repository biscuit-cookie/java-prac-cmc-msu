package ru.cmc.msu.genealogy.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonPlaceId implements Serializable {

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonPlaceId)) {
            return false;
        }
        PersonPlaceId that = (PersonPlaceId) o;
        return Objects.equals(placeId, that.placeId) && Objects.equals(personId, that.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeId, personId);
    }
}
