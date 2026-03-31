package ru.cmc.msu.genealogy.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.Relation;
import ru.cmc.msu.genealogy.models.RelationType;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class RelationDAOTest extends AbstractDAOTest {

    @Autowired
    private RelationDAO relationDAO;

    @Autowired
    private PersonDAO personDAO;

    @Test
    public void testGetAllAndGetById() {
        List<Relation> relations = relationDAO.getAll();
        assertEquals(relations.size(), 13);

        Relation relation = relationDAO.getById(1L);
        assertNotNull(relation);
        assertEquals(relation.getRelationshipType(), RelationType.PARTNER);

        assertNull(relationDAO.getById(999L));
    }

    @Test
    public void testSaveUpdateDeleteAndDeleteById() {
        Person target = personDAO.findExactByName("Алексей Павлович");
        Person related = personDAO.findExactByName("Наталья Игоревна");

        Relation relation = new Relation();
        relation.setTargetPerson(target);
        relation.setRelatedPerson(related);
        relation.setDateOfBeginning(2015);
        relation.setRelationshipType(RelationType.ADOPTED_CHILD);
        Relation saved = relationDAO.save(relation);

        assertNotNull(saved.getId());
        assertEquals(relationDAO.getAll().size(), 14);

        saved.setDateOfEnd(2022);
        relationDAO.update(saved);
        assertEquals(relationDAO.getById(saved.getId()).getDateOfEnd(), Integer.valueOf(2022));

        relationDAO.delete(saved);
        assertNull(relationDAO.getById(saved.getId()));

        Relation second = new Relation();
        second.setTargetPerson(target);
        second.setRelatedPerson(related);
        second.setDateOfBeginning(2016);
        second.setRelationshipType(RelationType.BASTARD_CHILD);
        second = relationDAO.save(second);
        relationDAO.deleteById(second.getId());
        assertNull(relationDAO.getById(second.getId()));
    }

    @Test
    public void testGetRelationsByTargetPerson() {
        Person person = personDAO.findExactByName("Михаил Рюрикович");
        List<Relation> relations = relationDAO.getRelationsByTargetPerson(person);

        assertEquals(relations.size(), 3);
        assertEquals(relations.get(0).getRelationshipType(), RelationType.PARTNER);
        assertEquals(relations.get(0).getRelatedPerson().getName(), "Елена Михайловна");
        assertEquals(relations.get(1).getRelatedPerson().getName(), "Анна Рюриковна");
        assertEquals(relations.get(2).getRelatedPerson().getName(), "Рюрик Старший");
    }

    @Test
    public void testGetRelationsByRelatedPerson() {
        Person person = personDAO.findExactByName("Рюрик Старший");
        List<Relation> relations = relationDAO.getRelationsByRelatedPerson(person);

        assertEquals(relations.size(), 2);
        assertEquals(relations.get(0).getTargetPerson().getName(), "Михаил Рюрикович");
        assertEquals(relations.get(1).getTargetPerson().getName(), "Софья Рюриковна");
    }

    @Test
    public void testGetRelationsByPerson() {
        Person person = personDAO.findExactByName("Михаил Рюрикович");
        List<Relation> relations = relationDAO.getRelationsByPerson(person);

        assertEquals(relations.size(), 5);
        assertEquals(relations.get(0).getRelationshipType(), RelationType.PARTNER);
        assertEquals(relations.get(0).getRelatedPerson().getName(), "Елена Михайловна");
        assertEquals(relations.get(1).getRelatedPerson().getName(), "Анна Рюриковна");
        assertEquals(relations.get(2).getRelatedPerson().getName(), "Рюрик Старший");
        assertEquals(relations.get(3).getTargetPerson().getName(), "Игорь Михайлович");
        assertEquals(relations.get(4).getTargetPerson().getName(), "Мария Михайловна");
    }

    @Test
    public void testGetRelationsByPersonWhenNoRelationsExist() {
        Person person = new Person();
        person.setName("Человек Без Связей");
        person.setGender("Мужской");
        person.setCharacteristics("Тестовая запись.");
        Person savedPerson = personDAO.save(person);

        List<Relation> relations = relationDAO.getRelationsByPerson(savedPerson);

        assertTrue(relations.isEmpty());
    }

    @Test
    public void testGetRelationsByPersonWhenOnlyTargetRelationsExist() {
        Person person = personDAO.findExactByName("Михаил Рюрикович");
        List<Relation> targetRelations = relationDAO.getRelationsByTargetPerson(person);
        List<Relation> allRelations = relationDAO.getRelationsByPerson(person);

        assertNotNull(targetRelations);
        assertEquals(targetRelations.size(), 3);
        assertEquals(allRelations.size(), 5);
    }

    @Test
    public void testGetRelationsByPersonWhenOnlyRelatedRelationsExist() {
        Person person = personDAO.findExactByName("Елена Михайловна");

        List<Relation> targetRelations = relationDAO.getRelationsByTargetPerson(person);
        List<Relation> relatedRelations = relationDAO.getRelationsByRelatedPerson(person);
        List<Relation> allRelations = relationDAO.getRelationsByPerson(person);

        assertTrue(targetRelations.isEmpty());
        assertEquals(relatedRelations.size(), 3);
        assertEquals(allRelations.size(), 3);
    }

    @Test
    public void testGetRelatedPeopleByType() {
        Person person = personDAO.findExactByName("Михаил Рюрикович");
        List<Person> people = relationDAO.getRelatedPeopleByType(person, RelationType.WEDLOCK_CHILD);

        assertEquals(people.size(), 2);
        assertEquals(people.get(0).getName(), "Анна Рюриковна");
        assertEquals(people.get(1).getName(), "Рюрик Старший");

        assertTrue(relationDAO.getRelatedPeopleByType(person, RelationType.ADOPTED_CHILD).isEmpty());
    }

    @Test
    public void testGetTargetPeopleByType() {
        Person person = personDAO.findExactByName("Рюрик Старший");
        List<Person> people = relationDAO.getTargetPeopleByType(person, RelationType.WEDLOCK_CHILD);

        assertEquals(people.size(), 2);
        assertEquals(people.get(0).getName(), "Михаил Рюрикович");
        assertEquals(people.get(1).getName(), "Софья Рюриковна");

        assertTrue(relationDAO.getTargetPeopleByType(person, RelationType.BASTARD_CHILD).isEmpty());
    }
}
