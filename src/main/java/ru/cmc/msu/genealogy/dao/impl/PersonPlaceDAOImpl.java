package ru.cmc.msu.genealogy.dao.impl;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import ru.cmc.msu.genealogy.dao.PersonPlaceDAO;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.PersonPlace;
import ru.cmc.msu.genealogy.models.PersonPlaceId;
import ru.cmc.msu.genealogy.models.Place;

import java.util.List;

@Repository
public class PersonPlaceDAOImpl extends CommonDAOImpl<PersonPlace, PersonPlaceId> implements PersonPlaceDAO {

    public PersonPlaceDAOImpl() {
        super(PersonPlace.class);
    }

    @Override
    public List<PersonPlace> getByPerson(Person person) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "from PersonPlace pp where pp.person = :person order by pp.place.name",
                    PersonPlace.class
            ).setParameter("person", person).list();
        }
    }

    @Override
    public List<PersonPlace> getByPlace(Place place) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "from PersonPlace pp where pp.place = :place order by pp.person.name",
                    PersonPlace.class
            ).setParameter("place", place).list();
        }
    }
}
