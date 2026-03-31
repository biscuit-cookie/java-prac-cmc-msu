package ru.cmc.msu.genealogy.dao;

import ru.cmc.msu.genealogy.models.Person;

import java.util.List;

public interface PersonDAO extends CommonDAO<Person, Long> {
    // Returns all people whose names contain the given substring, ignoring case.
    List<Person> findByName(String name);

    // Returns one person with the exact given name, ignoring case, or null if absent.
    Person findExactByName(String name);

    // Returns all people ordered alphabetically by name.
    List<Person> getAllOrderedByName();

    // Returns all people ordered by birth date, placing unknown dates at the end.
    List<Person> getAllOrderedByBirthDate();
}
