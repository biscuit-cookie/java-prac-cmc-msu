package ru.cmc.msu.genealogy.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import ru.cmc.msu.genealogy.models.Person;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class PersonDAOTest extends AbstractDAOTest {

    @Autowired
    private PersonDAO personDAO;

    @Test
    public void testGetAllAndGetById() {
        List<Person> people = personDAO.getAll();
        assertEquals(people.size(), 10);

        Person person = personDAO.getById(1L);
        assertNotNull(person);
        assertEquals(person.getName(), "Рюрик Старший");

        assertNull(personDAO.getById(999L));
    }

    @Test
    public void testSaveAndDeleteById() {
        Person person = new Person();
        person.setName("Виктор Алексеевич");
        person.setGender("Мужской");
        person.setDateOfBirth(2001);
        person.setCharacteristics("Студент и исследователь семейной истории.");
        Person saved = personDAO.save(person);

        assertNotNull(saved.getId());
        Person fromDb = personDAO.getById(saved.getId());
        assertNotNull(fromDb);
        assertEquals(fromDb.getName(), "Виктор Алексеевич");
        assertEquals(personDAO.getAll().size(), 11);

        personDAO.deleteById(saved.getId());
        assertNull(personDAO.getById(saved.getId()));
        assertEquals(personDAO.getAll().size(), 10);
    }

    @Test
    public void testSaveCollection() {
        List<Person> newPeople = Arrays.asList(
                buildPerson("Ольга Павловна", "Женский", 2000, null, "Новая запись 1."),
                buildPerson("Даниил Павлович", "Мужской", 2003, null, "Новая запись 2.")
        );

        personDAO.saveCollection(newPeople);

        assertEquals(personDAO.getAll().size(), 12);
        assertEquals(personDAO.findExactByName("Ольга Павловна").getGender(), "Женский");
        assertEquals(personDAO.findExactByName("Даниил Павлович").getDateOfBirth(), Integer.valueOf(2003));
    }

    @Test
    public void testUpdate() {
        Person person = personDAO.findExactByName("Игорь Михайлович");
        assertNotNull(person);

        person.setCharacteristics("Сын Михаила, радиофизик и преподаватель.");
        person.setDateOfDeath(2020);
        personDAO.update(person);

        Person updated = personDAO.getById(person.getId());
        assertEquals(updated.getCharacteristics(), "Сын Михаила, радиофизик и преподаватель.");
        assertEquals(updated.getDateOfDeath(), Integer.valueOf(2020));
    }

    @Test
    public void testDelete() {
        Person person = personDAO.findExactByName("Наталья Игоревна");
        assertNotNull(person);

        personDAO.delete(person);

        assertNull(personDAO.getById(person.getId()));
        assertNull(personDAO.findExactByName("Наталья Игоревна"));
    }

    @Test
    public void testFindByName() {
        List<Person> people = personDAO.findByName("игор");
        assertEquals(people.size(), 3);
        assertEquals(people.get(0).getName(), "Игорь Михайлович");
        assertEquals(people.get(1).getName(), "Наталья Игоревна");
        assertEquals(people.get(2).getName(), "Павел Игоревич");

        assertTrue(personDAO.findByName("несуществующий").isEmpty());
    }

    @Test
    public void testFindExactByName() {
        Person person = personDAO.findExactByName("алексей павлович");
        assertNotNull(person);
        assertEquals(person.getName(), "Алексей Павлович");

        assertNull(personDAO.findExactByName("Неизвестный Человек"));
    }

    @Test
    public void testGetAllOrderedByName() {
        List<Person> people = personDAO.getAllOrderedByName();
        assertEquals(people.size(), 10);
        assertEquals(people.get(0).getName(), "Алексей Павлович");
        assertEquals(people.get(people.size() - 1).getName(), "Софья Рюриковна");
    }

    @Test
    public void testGetAllOrderedByBirthDate() {
        List<Person> people = personDAO.getAllOrderedByBirthDate();
        assertEquals(people.size(), 10);
        assertEquals(people.get(0).getName(), "Рюрик Старший");
        assertEquals(people.get(1).getName(), "Анна Рюриковна");
        assertEquals(people.get(people.size() - 1).getName(), "Алексей Павлович");
    }

    @Test
    public void testGetAllOrderedByBirthDatePlacesPeopleWithoutBirthDateLast() {
        Person personWithoutBirthDate = buildPerson(
                "Человек Без Даты Рождения",
                "Мужской",
                null,
                null,
                "Тестовая запись без года рождения."
        );
        personDAO.save(personWithoutBirthDate);

        List<Person> people = personDAO.getAllOrderedByBirthDate();

        assertEquals(people.size(), 11);
        assertEquals(people.get(people.size() - 1).getName(), "Человек Без Даты Рождения");
        assertNull(people.get(people.size() - 1).getDateOfBirth());
    }

    private Person buildPerson(String name, String gender, Integer birthDate, Integer deathDate, String characteristics) {
        Person person = new Person();
        person.setName(name);
        person.setGender(gender);
        person.setDateOfBirth(birthDate);
        person.setDateOfDeath(deathDate);
        person.setCharacteristics(characteristics);
        return person;
    }
}
