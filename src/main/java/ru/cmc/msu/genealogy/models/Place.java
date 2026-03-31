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
@Table(name = "place")
@Getter
@Setter
@NoArgsConstructor
public class Place implements CommonEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    public Place(String name, String description) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
    }

    public Place(Long id, String name, String description) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
    }

    @Override
    public String toString() {
        return "Place{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Place)) {
            return false;
        }
        Place place = (Place) o;
        return Objects.equals(id, place.id)
                && Objects.equals(name, place.name)
                && Objects.equals(description, place.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description);
    }
}
