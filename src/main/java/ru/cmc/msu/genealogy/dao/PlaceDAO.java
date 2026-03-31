package ru.cmc.msu.genealogy.dao;

import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.Place;

import java.util.List;

public interface PlaceDAO extends CommonDAO<Place, Long> {
    // Returns all places whose names contain the given substring, ignoring case.
    List<Place> findByName(String name);

    // Returns all places associated with the given person.
    List<Place> getPlacesByPerson(Person person);
}
