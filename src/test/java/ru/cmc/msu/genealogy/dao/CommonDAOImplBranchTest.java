package ru.cmc.msu.genealogy.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import ru.cmc.msu.genealogy.models.Person;

import java.util.Collections;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class CommonDAOImplBranchTest extends AbstractDAOTest {

    @Autowired
    private PersonDAO personDAO;

    @Test
    public void testDeleteByIdForMissingEntityDoesNothing() {
        assertEquals(personDAO.getAll().size(), 10);
        personDAO.deleteById(999L);
        assertEquals(personDAO.getAll().size(), 10);
        assertNull(personDAO.getById(999L));
    }

    @Test
    public void testDeleteUsesMergeBranchForDetachedEntity() {
        Person person = personDAO.findExactByName("Алексей Павлович");
        personDAO.delete(person);
        assertNull(personDAO.findExactByName("Алексей Павлович"));
    }

    @Test
    public void testSaveCollectionWithEmptyCollectionDoesNothing() {
        assertEquals(personDAO.getAll().size(), 10);
        personDAO.saveCollection(Collections.emptyList());
        assertEquals(personDAO.getAll().size(), 10);
    }

    @Test
    public void testDeleteByIdForExistingEntityRemovesIt() {
        Person person = personDAO.findExactByName("Наталья Игоревна");
        assertNotNull(person);

        personDAO.deleteById(person.getId());

        assertNull(personDAO.getById(person.getId()));
        assertEquals(personDAO.getAll().size(), 9);
    }
}
