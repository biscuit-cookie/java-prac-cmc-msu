package ru.cmc.msu.genealogy.models;

import org.testng.Assert.ThrowingRunnable;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class ModelMethodsTest {

    @Test
    public void personMethodsShouldBeCovered() {
        Person person = new Person();
        assertNull(person.getId());
        assertNull(person.getName());
        assertNull(person.getGender());
        assertNull(person.getDateOfBirth());
        assertNull(person.getDateOfDeath());
        assertNull(person.getCharacteristics());

        person.setId(1L);
        person.setName("Ivan");
        person.setGender("male");
        person.setDateOfBirth(1900);
        person.setDateOfDeath(1980);
        person.setCharacteristics("founder");

        assertEquals(person.getId(), Long.valueOf(1L));
        assertEquals(person.getName(), "Ivan");
        assertEquals(person.getGender(), "male");
        assertEquals(person.getDateOfBirth(), Integer.valueOf(1900));
        assertEquals(person.getDateOfDeath(), Integer.valueOf(1980));
        assertEquals(person.getCharacteristics(), "founder");

        Person required = new Person("Anna", "female");
        assertEquals(required.getName(), "Anna");
        assertEquals(required.getGender(), "female");

        Person full = new Person(2L, "Anna", "female", 1905, 1991, "teacher");
        Person same = new Person(2L, "Anna", "female", 1905, 1991, "teacher");

        assertTrue(full.toString().contains("Anna"));
        assertEquals(full, same);
        assertEquals(full.hashCode(), same.hashCode());
        assertTrue(full.equals(full));
        assertFalse(full.equals(new Object()));
        assertNotEquals(full, null);
        assertNotEquals(full, new Person(3L, "Anna", "female", 1905, 1991, "teacher"));
        assertNotEquals(full, new Person(2L, "Maria", "female", 1905, 1991, "teacher"));
        assertNotEquals(full, new Person(2L, "Anna", "male", 1905, 1991, "teacher"));
        assertNotEquals(full, new Person(2L, "Anna", "female", 1906, 1991, "teacher"));
        assertNotEquals(full, new Person(2L, "Anna", "female", 1905, 1992, "teacher"));
        assertNotEquals(full, new Person(2L, "Anna", "female", 1905, 1991, "writer"));

        expectThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                new Person(null, "female");
            }
        });
        expectThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                new Person("Anna", null);
            }
        });
        expectThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                new Person(2L, null, "female", 1905, 1991, "teacher");
            }
        });
        expectThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                new Person(2L, "Anna", null, 1905, 1991, "teacher");
            }
        });
    }

    @Test
    public void placeMethodsShouldBeCovered() {
        Place place = new Place();
        assertNull(place.getId());
        assertNull(place.getName());
        assertNull(place.getDescription());

        place.setId(10L);
        place.setName("Moscow");
        place.setDescription("capital");

        assertEquals(place.getId(), Long.valueOf(10L));
        assertEquals(place.getName(), "Moscow");
        assertEquals(place.getDescription(), "capital");

        Place required = new Place("Kazan", "city");
        Place full = new Place(11L, "Kazan", "city");
        Place same = new Place(11L, "Kazan", "city");

        assertEquals(required.getName(), "Kazan");
        assertEquals(required.getDescription(), "city");
        assertTrue(full.toString().contains("Kazan"));
        assertEquals(full, same);
        assertEquals(full.hashCode(), same.hashCode());
        assertTrue(full.equals(full));
        assertFalse(full.equals(new Object()));
        assertNotEquals(full, null);
        assertNotEquals(full, new Place(12L, "Kazan", "city"));
        assertNotEquals(full, new Place(11L, "Tver", "city"));
        assertNotEquals(full, new Place(11L, "Kazan", "village"));

        expectThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                new Place(null, "city");
            }
        });
        expectThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                new Place("Kazan", null);
            }
        });
        expectThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                new Place(11L, null, "city");
            }
        });
        expectThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                new Place(11L, "Kazan", null);
            }
        });
    }

    @Test
    public void personPlaceIdMethodsShouldBeCovered() {
        PersonPlaceId id = new PersonPlaceId();
        assertNull(id.getPlaceId());
        assertNull(id.getPersonId());

        id.setPlaceId(5L);
        id.setPersonId(7L);

        assertEquals(id.getPlaceId(), Long.valueOf(5L));
        assertEquals(id.getPersonId(), Long.valueOf(7L));

        PersonPlaceId same = new PersonPlaceId(5L, 7L);
        assertEquals(id, same);
        assertEquals(id.hashCode(), same.hashCode());
        assertTrue(id.equals(id));
        assertFalse(id.equals(new Object()));
        assertNotEquals(id, null);
        assertNotEquals(id, new PersonPlaceId(6L, 7L));
        assertNotEquals(id, new PersonPlaceId(5L, 8L));
    }

    @Test
    public void personPlaceMethodsShouldBeCovered() {
        Person person = new Person(4L, "Pavel", "male", 1980, null, "desc");
        Place place = new Place(3L, "Tver", "desc");

        PersonPlace personPlace = new PersonPlace();
        assertNull(personPlace.getId());
        assertNull(personPlace.getPerson());
        assertNull(personPlace.getPlace());

        PersonPlaceId id = new PersonPlaceId(3L, 4L);
        personPlace.setId(id);
        personPlace.setPerson(person);
        personPlace.setPlace(place);

        assertSame(personPlace.getId(), id);
        assertSame(personPlace.getPerson(), person);
        assertSame(personPlace.getPlace(), place);

        PersonPlace required = new PersonPlace(person, place);
        PersonPlace full = new PersonPlace(new PersonPlaceId(3L, 4L), person, place);
        PersonPlace same = new PersonPlace(new PersonPlaceId(3L, 4L), person, place);

        assertEquals(required.getId(), new PersonPlaceId(3L, 4L));
        assertTrue(full.toString().contains("PersonPlace"));
        assertEquals(full, same);
        assertEquals(full.hashCode(), same.hashCode());
        assertTrue(full.equals(full));
        assertFalse(full.equals(new Object()));
        assertNotEquals(full, null);
        assertNotEquals(full, new PersonPlace(new PersonPlaceId(9L, 4L), person, place));
        assertNotEquals(full, new PersonPlace(new PersonPlaceId(3L, 4L),
                new Person(4L, "Other", "male", 1980, null, "desc"), place));
        assertNotEquals(full, new PersonPlace(new PersonPlaceId(3L, 4L), person, new Place(3L, "Other", "desc")));

        expectThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                new PersonPlace(null, place);
            }
        });
        expectThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                new PersonPlace(person, null);
            }
        });
    }

    @Test
    public void relationMethodsShouldBeCovered() {
        Person target = new Person(1L, "Rurik", "male", 1880, 1948, "desc");
        Person related = new Person(2L, "Anna", "female", 1885, 1961, "desc");

        Relation relation = new Relation();
        assertNull(relation.getId());
        assertNull(relation.getTargetPerson());
        assertNull(relation.getRelatedPerson());
        assertNull(relation.getDateOfBeginning());
        assertNull(relation.getDateOfEnd());
        assertNull(relation.getRelationshipType());

        relation.setId(5L);
        relation.setTargetPerson(target);
        relation.setRelatedPerson(related);
        relation.setDateOfBeginning(1904);
        relation.setDateOfEnd(1948);
        relation.setRelationshipType(RelationType.PARTNER);

        assertEquals(relation.getId(), Long.valueOf(5L));
        assertSame(relation.getTargetPerson(), target);
        assertSame(relation.getRelatedPerson(), related);
        assertEquals(relation.getDateOfBeginning(), Integer.valueOf(1904));
        assertEquals(relation.getDateOfEnd(), Integer.valueOf(1948));
        assertEquals(relation.getRelationshipType(), RelationType.PARTNER);

        Relation required = new Relation(target, related, RelationType.WEDLOCK_CHILD);
        Relation full = new Relation(5L, target, related, 1904, 1948, RelationType.PARTNER);
        Relation same = new Relation(5L, target, related, 1904, 1948, RelationType.PARTNER);

        assertEquals(required.getTargetPerson(), target);
        assertEquals(required.getRelatedPerson(), related);
        assertEquals(required.getRelationshipType(), RelationType.WEDLOCK_CHILD);
        assertTrue(full.toString().contains("PARTNER"));
        assertEquals(full, same);
        assertEquals(full.hashCode(), same.hashCode());
        assertTrue(full.equals(full));
        assertFalse(full.equals(new Object()));
        assertNotEquals(full, null);
        assertNotEquals(full, new Relation(6L, target, related, 1904, 1948, RelationType.PARTNER));
        assertNotEquals(full, new Relation(5L, new Person(3L, "Other", "male", 1881, null, "x"),
                related, 1904, 1948, RelationType.PARTNER));
        assertNotEquals(full, new Relation(5L, target,
                new Person(4L, "Other", "female", 1886, null, "x"), 1904, 1948, RelationType.PARTNER));
        assertNotEquals(full, new Relation(5L, target, related, 1905, 1948, RelationType.PARTNER));
        assertNotEquals(full, new Relation(5L, target, related, 1904, 1949, RelationType.PARTNER));
        assertNotEquals(full, new Relation(5L, target, related, 1904, 1948, RelationType.ADOPTED_CHILD));

        expectThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                new Relation(null, related, RelationType.PARTNER);
            }
        });
        expectThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                new Relation(target, null, RelationType.PARTNER);
            }
        });
        expectThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                new Relation(target, related, null);
            }
        });
        expectThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                new Relation(5L, null, related, 1904, 1948, RelationType.PARTNER);
            }
        });
        expectThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                new Relation(5L, target, null, 1904, 1948, RelationType.PARTNER);
            }
        });
        expectThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                new Relation(5L, target, related, 1904, 1948, null);
            }
        });
    }
}
