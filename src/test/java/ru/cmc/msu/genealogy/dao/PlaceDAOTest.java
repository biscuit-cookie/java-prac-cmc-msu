package ru.cmc.msu.genealogy.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.Place;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class PlaceDAOTest extends AbstractDAOTest {

    @Autowired
    private PlaceDAO placeDAO;

    @Autowired
    private PersonDAO personDAO;

    @Test
    public void testGetAllAndGetById() {
        List<Place> places = placeDAO.getAll();
        assertEquals(places.size(), 5);

        Place place = placeDAO.getById(1L);
        assertNotNull(place);
        assertEquals(place.getName(), "Тверь");

        assertNull(placeDAO.getById(999L));
    }

    @Test
    public void testSaveUpdateDeleteAndDeleteById() {
        Place place = new Place();
        place.setName("Ярославль");
        place.setDescription("Город, связанный с дополнительной ветвью семьи.");
        Place saved = placeDAO.save(place);
        assertNotNull(saved.getId());
        assertEquals(placeDAO.getAll().size(), 6);

        saved.setDescription("Город, связанный с новой ветвью семьи.");
        placeDAO.update(saved);
        assertEquals(placeDAO.getById(saved.getId()).getDescription(), "Город, связанный с новой ветвью семьи.");

        placeDAO.delete(saved);
        assertNull(placeDAO.getById(saved.getId()));

        Place second = new Place();
        second.setName("Псков");
        second.setDescription("Временное место проживания.");
        second = placeDAO.save(second);
        assertNotNull(second.getId());
        placeDAO.deleteById(second.getId());
        assertNull(placeDAO.getById(second.getId()));
    }

    @Test
    public void testFindByName() {
        List<Place> places = placeDAO.findByName("моск");
        assertEquals(places.size(), 1);
        assertEquals(places.get(0).getName(), "Москва");

        assertTrue(placeDAO.findByName("несуществующее место").isEmpty());
    }

    @Test
    public void testGetPlacesByPerson() {
        Person person = personDAO.findExactByName("Игорь Михайлович");
        assertNotNull(person);

        List<Place> places = placeDAO.getPlacesByPerson(person);
        assertEquals(places.size(), 2);
        assertEquals(places.get(0).getName(), "Казань");
        assertEquals(places.get(1).getName(), "Москва");

        Person newPerson = new Person();
        newPerson.setName("Временный Человек");
        newPerson.setGender("Мужской");
        newPerson.setCharacteristics("Не связан с местами.");
        Person savedPerson = personDAO.save(newPerson);
        assertTrue(placeDAO.getPlacesByPerson(savedPerson).isEmpty());
    }
}
