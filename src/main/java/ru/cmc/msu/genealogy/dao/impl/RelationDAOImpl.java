package ru.cmc.msu.genealogy.dao.impl;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import ru.cmc.msu.genealogy.dao.RelationDAO;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.Relation;
import ru.cmc.msu.genealogy.models.RelationType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Repository
public class RelationDAOImpl extends CommonDAOImpl<Relation, Long> implements RelationDAO {

    public RelationDAOImpl() {
        super(Relation.class);
    }

    @Override
    public List<Relation> getRelationsByTargetPerson(Person person) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "from Relation r where r.targetPerson = :person order by r.relationshipType, r.relatedPerson.name",
                    Relation.class
            ).setParameter("person", person).list();
        }
    }

    @Override
    public List<Relation> getRelationsByRelatedPerson(Person person) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "from Relation r where r.relatedPerson = :person order by r.relationshipType, r.targetPerson.name",
                    Relation.class
            ).setParameter("person", person).list();
        }
    }

    @Override
    public List<Relation> getRelationsByPerson(Person person) {
        LinkedHashSet<Relation> uniqueRelations = new LinkedHashSet<>();
        uniqueRelations.addAll(getRelationsByTargetPerson(person));
        uniqueRelations.addAll(getRelationsByRelatedPerson(person));
        return new ArrayList<>(uniqueRelations);
    }

    @Override
    public List<Person> getRelatedPeopleByType(Person person, RelationType relationType) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "select r.relatedPerson from Relation r " +
                            "where r.targetPerson = :person and r.relationshipType = :relationType " +
                            "order by r.relatedPerson.name",
                    Person.class
            ).setParameter("person", person)
                    .setParameter("relationType", relationType)
                    .list();
        }
    }

    @Override
    public List<Person> getTargetPeopleByType(Person person, RelationType relationType) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "select r.targetPerson from Relation r " +
                            "where r.relatedPerson = :person and r.relationshipType = :relationType " +
                            "order by r.targetPerson.name",
                    Person.class
            ).setParameter("person", person)
                    .setParameter("relationType", relationType)
                    .list();
        }
    }
}
