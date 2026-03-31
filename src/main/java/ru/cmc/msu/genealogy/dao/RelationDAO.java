package ru.cmc.msu.genealogy.dao;

import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.Relation;
import ru.cmc.msu.genealogy.models.RelationType;

import java.util.List;

public interface RelationDAO extends CommonDAO<Relation, Long> {
    // Returns relations where the given person is the target person.
    List<Relation> getRelationsByTargetPerson(Person person);

    // Returns relations where the given person is the related person.
    List<Relation> getRelationsByRelatedPerson(Person person);

    // Returns all relations involving the given person in any role.
    List<Relation> getRelationsByPerson(Person person);

    // Returns related people connected to the given target person by the specified relation type.
    List<Person> getRelatedPeopleByType(Person person, RelationType relationType);

    // Returns target people connected to the given related person by the specified relation type.
    List<Person> getTargetPeopleByType(Person person, RelationType relationType);
}
