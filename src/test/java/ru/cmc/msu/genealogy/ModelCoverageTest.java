package ru.cmc.msu.genealogy;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.testng.annotations.Test;
import ru.cmc.msu.genealogy.models.CommonEntity;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.PersonPlace;
import ru.cmc.msu.genealogy.models.PersonPlaceId;
import ru.cmc.msu.genealogy.models.Place;
import ru.cmc.msu.genealogy.models.Relation;
import ru.cmc.msu.genealogy.models.RelationType;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class ModelCoverageTest {

    @Test
    public void testPersonConstructorsAccessorsAndCommonEntityContract() {
        Person empty = new Person();
        assertNull(empty.getId());
        assertNull(empty.getName());

        empty.setId(10L);
        empty.setName("Иван");
        empty.setGender("Мужской");
        empty.setDateOfBirth(1990);
        empty.setDateOfDeath(2050);
        empty.setCharacteristics("Описание");

        CommonEntity<Long> entity = empty;
        assertEquals(entity.getId(), Long.valueOf(10L));
        assertEquals(empty.getName(), "Иван");
        assertEquals(empty.getGender(), "Мужской");
        assertEquals(empty.getDateOfBirth(), Integer.valueOf(1990));
        assertEquals(empty.getDateOfDeath(), Integer.valueOf(2050));
        assertEquals(empty.getCharacteristics(), "Описание");

        Person required = new Person("Мария", "Женский");
        assertEquals(required.getName(), "Мария");
        assertEquals(required.getGender(), "Женский");

        Person full = new Person(11L, "Мария", "Женский", 1992, null, "Характеристика");
        assertEquals(full.getId(), Long.valueOf(11L));
        assertTrue(full.toString().contains("Мария"));

        expectThrows(NullPointerException.class, () -> new Person(null, "Женский"));
        expectThrows(NullPointerException.class, () -> new Person("Мария", null));
    }

    @Test
    public void testPersonEqualsAndHashCode() {
        Person base = new Person(1L, "А", "М", 1900, 1980, "desc");
        Person same = new Person(1L, "А", "М", 1900, 1980, "desc");
        Person diffId = new Person(2L, "А", "М", 1900, 1980, "desc");
        Person diffName = new Person(1L, "Б", "М", 1900, 1980, "desc");
        Person diffGender = new Person(1L, "А", "Ж", 1900, 1980, "desc");
        Person diffBirth = new Person(1L, "А", "М", 1901, 1980, "desc");
        Person diffDeath = new Person(1L, "А", "М", 1900, 1981, "desc");
        Person diffCharacteristics = new Person(1L, "А", "М", 1900, 1980, "other");

        assertEquals(base, base);
        assertTrue(base.equals(base));
        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());
        assertNotEquals(base, diffId);
        assertNotEquals(base, diffName);
        assertNotEquals(base, diffGender);
        assertNotEquals(base, diffBirth);
        assertNotEquals(base, diffDeath);
        assertNotEquals(base, diffCharacteristics);
        assertNotEquals(base, null);
        assertNotEquals(base, "person");
        assertFalse(base.equals(new Object()));
    }

    @Test
    public void testPlaceConstructorsAccessorsEqualsAndHashCode() {
        Place empty = new Place();
        empty.setId(1L);
        empty.setName("Москва");
        empty.setDescription("Описание");
        assertEquals(empty.getId(), Long.valueOf(1L));
        assertEquals(empty.getName(), "Москва");
        assertEquals(empty.getDescription(), "Описание");

        Place required = new Place("Казань", "Город");
        Place full = new Place(2L, "Казань", "Город");
        Place same = new Place(2L, "Казань", "Город");
        Place diffId = new Place(3L, "Казань", "Город");
        Place diffName = new Place(2L, "Тверь", "Город");
        Place diffDescription = new Place(2L, "Казань", "Другой город");

        assertEquals(required.getName(), "Казань");
        assertTrue(full.equals(full));
        assertEquals(full, same);
        assertEquals(full.hashCode(), same.hashCode());
        assertNotEquals(full, diffId);
        assertNotEquals(full, diffName);
        assertNotEquals(full, diffDescription);
        assertNotEquals(full, null);
        assertNotEquals(full, "place");
        assertFalse(full.equals(new Object()));
        assertTrue(full.toString().contains("Казань"));

        expectThrows(NullPointerException.class, () -> new Place(null, "x"));
        expectThrows(NullPointerException.class, () -> new Place("x", null));
    }

    @Test
    public void testPersonPlaceIdEqualsHashCodeAndAccessors() {
        PersonPlaceId empty = new PersonPlaceId();
        empty.setPlaceId(5L);
        empty.setPersonId(7L);
        assertEquals(empty.getPlaceId(), Long.valueOf(5L));
        assertEquals(empty.getPersonId(), Long.valueOf(7L));

        PersonPlaceId same = new PersonPlaceId(5L, 7L);
        PersonPlaceId diffPlace = new PersonPlaceId(6L, 7L);
        PersonPlaceId diffPerson = new PersonPlaceId(5L, 8L);

        assertEquals(empty, same);
        assertTrue(empty.equals(empty));
        assertEquals(empty.hashCode(), same.hashCode());
        assertNotEquals(empty, diffPlace);
        assertNotEquals(empty, diffPerson);
        assertNotEquals(empty, null);
        assertNotEquals(empty, "id");
        assertFalse(empty.equals(new Object()));
    }

    @Test
    public void testPersonPlaceConstructorsAccessorsEqualsAndHashCode() {
        Person person = new Person(4L, "Павел", "Мужской", 1980, null, "desc");
        Place place = new Place(3L, "Тверь", "desc");

        PersonPlace empty = new PersonPlace();
        empty.setId(new PersonPlaceId(3L, 4L));
        empty.setPerson(person);
        empty.setPlace(place);
        assertEquals(empty.getId(), new PersonPlaceId(3L, 4L));
        assertSame(empty.getPerson(), person);
        assertSame(empty.getPlace(), place);

        PersonPlace constructed = new PersonPlace(person, place);
        assertEquals(constructed.getId(), new PersonPlaceId(3L, 4L));

        PersonPlace full = new PersonPlace(new PersonPlaceId(3L, 4L), person, place);
        PersonPlace same = new PersonPlace(new PersonPlaceId(3L, 4L), person, place);
        PersonPlace diffId = new PersonPlace(new PersonPlaceId(8L, 4L), person, place);
        PersonPlace diffPerson = new PersonPlace(new PersonPlaceId(3L, 4L),
                new Person(4L, "Другой", "Мужской", 1970, null, "other"), place);
        PersonPlace diffPlace = new PersonPlace(new PersonPlaceId(3L, 4L), person, new Place(3L, "Казань", "other"));

        assertTrue(full.equals(full));
        assertEquals(full, same);
        assertEquals(full.hashCode(), same.hashCode());
        assertNotEquals(full, diffId);
        assertNotEquals(full, diffPerson);
        assertNotEquals(full, diffPlace);
        assertNotEquals(full, null);
        assertNotEquals(full, "personPlace");
        assertFalse(full.equals(new Object()));
        assertTrue(full.toString().contains("PersonPlace"));

        expectThrows(NullPointerException.class, () -> new PersonPlace(null, place));
        expectThrows(NullPointerException.class, () -> new PersonPlace(person, null));
    }

    @Test
    public void testRelationConstructorsAccessorsEqualsAndHashCode() {
        Person target = new Person(1L, "Рюрик", "Мужской", 1880, 1948, "desc");
        Person related = new Person(2L, "Анна", "Женский", 1885, 1961, "desc");

        Relation empty = new Relation();
        empty.setId(5L);
        empty.setTargetPerson(target);
        empty.setRelatedPerson(related);
        empty.setDateOfBeginning(1904);
        empty.setDateOfEnd(1948);
        empty.setRelationshipType(RelationType.PARTNER);

        assertEquals(empty.getId(), Long.valueOf(5L));
        assertSame(empty.getTargetPerson(), target);
        assertSame(empty.getRelatedPerson(), related);
        assertEquals(empty.getDateOfBeginning(), Integer.valueOf(1904));
        assertEquals(empty.getDateOfEnd(), Integer.valueOf(1948));
        assertEquals(empty.getRelationshipType(), RelationType.PARTNER);

        Relation required = new Relation(target, related, RelationType.WEDLOCK_CHILD);
        Relation full = new Relation(5L, target, related, 1904, 1948, RelationType.PARTNER);
        Relation same = new Relation(5L, target, related, 1904, 1948, RelationType.PARTNER);
        Relation diffId = new Relation(6L, target, related, 1904, 1948, RelationType.PARTNER);
        Relation diffTarget = new Relation(5L, new Person(3L, "Другой", "Мужской", 1881, null, "other"), related, 1904, 1948, RelationType.PARTNER);
        Relation diffRelated = new Relation(5L, target, new Person(4L, "Иная", "Женский", 1886, null, "other"), 1904, 1948, RelationType.PARTNER);
        Relation diffBeginning = new Relation(5L, target, related, 1905, 1948, RelationType.PARTNER);
        Relation diffEnd = new Relation(5L, target, related, 1904, 1949, RelationType.PARTNER);
        Relation diffType = new Relation(5L, target, related, 1904, 1948, RelationType.ADOPTED_CHILD);

        assertEquals(required.getRelationshipType(), RelationType.WEDLOCK_CHILD);
        assertTrue(full.equals(full));
        assertEquals(full, same);
        assertEquals(full.hashCode(), same.hashCode());
        assertNotEquals(full, diffId);
        assertNotEquals(full, diffTarget);
        assertNotEquals(full, diffRelated);
        assertNotEquals(full, diffBeginning);
        assertNotEquals(full, diffEnd);
        assertNotEquals(full, diffType);
        assertNotEquals(full, null);
        assertNotEquals(full, "relation");
        assertFalse(full.equals(new Object()));
        assertTrue(full.toString().contains("PARTNER"));

        expectThrows(NullPointerException.class, () -> new Relation(null, related, RelationType.PARTNER));
        expectThrows(NullPointerException.class, () -> new Relation(target, null, RelationType.PARTNER));
        expectThrows(NullPointerException.class, () -> new Relation(target, related, null));
    }

    @Test
    public void testRelationTypeEnum() {
        assertEquals(RelationType.values().length, 4);
        assertEquals(RelationType.valueOf("PARTNER"), RelationType.PARTNER);
        assertEquals(RelationType.WEDLOCK_CHILD.ordinal(), 1);
        assertEquals(RelationType.ADOPTED_CHILD.name(), "ADOPTED_CHILD");
        assertFalse(RelationType.BASTARD_CHILD == RelationType.PARTNER);
    }

    @Test
    public void testApplicationMain() {
        try (MockedStatic<SpringApplication> springApplication = Mockito.mockStatic(SpringApplication.class)) {
            GenealogyApplication.main(new String[]{"arg1"});
            springApplication.verify(() -> SpringApplication.run(GenealogyApplication.class, new String[]{"arg1"}));
        }

        assertNotNull(new GenealogyApplication());
    }
}
