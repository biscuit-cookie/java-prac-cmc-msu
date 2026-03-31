package ru.cmc.msu.genealogy.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "relation")
@Getter
@Setter
@NoArgsConstructor
public class Relation implements CommonEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relation_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_person", nullable = false)
    private Person targetPerson;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "related_person", nullable = false)
    private Person relatedPerson;

    @Column(name = "date_of_beginning")
    private Integer dateOfBeginning;

    @Column(name = "date_of_end")
    private Integer dateOfEnd;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "relationship_type", nullable = false)
    private RelationType relationshipType;

    public Relation(Person targetPerson, Person relatedPerson, RelationType relationshipType) {
        this.targetPerson = Objects.requireNonNull(targetPerson, "targetPerson must not be null");
        this.relatedPerson = Objects.requireNonNull(relatedPerson, "relatedPerson must not be null");
        this.relationshipType = Objects.requireNonNull(relationshipType, "relationshipType must not be null");
    }

    public Relation(Long id, Person targetPerson, Person relatedPerson, Integer dateOfBeginning, Integer dateOfEnd,
                    RelationType relationshipType) {
        this.id = id;
        this.targetPerson = Objects.requireNonNull(targetPerson, "targetPerson must not be null");
        this.relatedPerson = Objects.requireNonNull(relatedPerson, "relatedPerson must not be null");
        this.dateOfBeginning = dateOfBeginning;
        this.dateOfEnd = dateOfEnd;
        this.relationshipType = Objects.requireNonNull(relationshipType, "relationshipType must not be null");
    }

    @Override
    public String toString() {
        return "Relation{" +
                "id=" + id +
                ", targetPerson=" + targetPerson +
                ", relatedPerson=" + relatedPerson +
                ", dateOfBeginning=" + dateOfBeginning +
                ", dateOfEnd=" + dateOfEnd +
                ", relationshipType=" + relationshipType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Relation)) {
            return false;
        }
        Relation relation = (Relation) o;
        return Objects.equals(id, relation.id)
                && Objects.equals(targetPerson, relation.targetPerson)
                && Objects.equals(relatedPerson, relation.relatedPerson)
                && Objects.equals(dateOfBeginning, relation.dateOfBeginning)
                && Objects.equals(dateOfEnd, relation.dateOfEnd)
                && relationshipType == relation.relationshipType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, targetPerson, relatedPerson, dateOfBeginning, dateOfEnd, relationshipType);
    }
}
