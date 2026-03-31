package ru.cmc.msu.genealogy.dao;

import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.PersonPlace;
import ru.cmc.msu.genealogy.models.PersonPlaceId;
import ru.cmc.msu.genealogy.models.Place;

import java.util.List;

public interface PersonPlaceDAO extends CommonDAO<PersonPlace, PersonPlaceId> {
    // Returns all person-place links for the given person.
    List<PersonPlace> getByPerson(Person person);

    // Returns all person-place links for the given place.
    List<PersonPlace> getByPlace(Place place);
}
