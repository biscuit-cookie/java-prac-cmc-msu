package ru.cmc.msu.genealogy.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.PersonPlace;
import ru.cmc.msu.genealogy.models.PersonPlaceId;
import ru.cmc.msu.genealogy.models.Place;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class PersonPlaceDAOTest extends AbstractDAOTest {

    @Autowired
    private PersonPlaceDAO personPlaceDAO;

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private PlaceDAO placeDAO;

    @Test
    public void testGetAllAndGetById() {
        List<PersonPlace> links = personPlaceDAO.getAll();
        assertEquals(links.size(), 12);

        PersonPlaceId id = new PersonPlaceId(1L, 1L);
        PersonPlace link = personPlaceDAO.getById(id);
        assertNotNull(link);
        assertEquals(link.getPerson().getName(), "Рюрик Старший");
        assertEquals(link.getPlace().getName(), "Тверь");

        assertNull(personPlaceDAO.getById(new PersonPlaceId(99L, 99L)));
    }

    @Test
    public void testSaveUpdateDeleteAndDeleteById() {
        Person person = personDAO.findExactByName("Алексей Павлович");
        Place place = placeDAO.findByName("Москва").get(0);

        PersonPlace link = new PersonPlace(person, place);
        PersonPlace saved = personPlaceDAO.save(link);
        assertNotNull(saved.getId());
        assertEquals(personPlaceDAO.getAll().size(), 13);

        PersonPlace sameLink = personPlaceDAO.getById(new PersonPlaceId(place.getId(), person.getId()));
        assertNotNull(sameLink);
        assertEquals(sameLink.getPlace().getName(), "Москва");

        personPlaceDAO.delete(sameLink);
        assertNull(personPlaceDAO.getById(new PersonPlaceId(place.getId(), person.getId())));

        PersonPlace second = personPlaceDAO.save(new PersonPlace(person, place));
        personPlaceDAO.deleteById(second.getId());
        assertNull(personPlaceDAO.getById(second.getId()));
    }

    @Test
    public void testGetByPerson() {
        Person person = personDAO.findExactByName("Игорь Михайлович");
        List<PersonPlace> links = personPlaceDAO.getByPerson(person);

        assertEquals(links.size(), 2);
        assertEquals(links.get(0).getPlace().getName(), "Казань");
        assertEquals(links.get(1).getPlace().getName(), "Москва");
    }

    @Test
    public void testGetByPlace() {
        Place place = placeDAO.findByName("Москва").get(0);
        List<PersonPlace> links = personPlaceDAO.getByPlace(place);

        assertEquals(links.size(), 5);
        assertEquals(links.get(0).getPerson().getName(), "Елена Михайловна");
        assertEquals(links.get(4).getPerson().getName(), "Наталья Игоревна");
    }

    @Test
    public void testGetByPersonAndGetByPlaceReturnEmptyListsWhenNoLinksExist() {
        Person person = new Person();
        person.setName("Человек Без Локаций");
        person.setGender("Мужской");
        person.setCharacteristics("Тестовая запись.");
        Person savedPerson = personDAO.save(person);

        Place place = new Place();
        place.setName("Место Без Людей");
        place.setDescription("Тестовая запись.");
        Place savedPlace = placeDAO.save(place);

        assertTrue(personPlaceDAO.getByPerson(savedPerson).isEmpty());
        assertTrue(personPlaceDAO.getByPlace(savedPlace).isEmpty());
    }
}
