package ru.cmc.msu.genealogy.dao.impl;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import ru.cmc.msu.genealogy.dao.PlaceDAO;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.Place;

import java.util.List;

@Repository
public class PlaceDAOImpl extends CommonDAOImpl<Place, Long> implements PlaceDAO {

    public PlaceDAOImpl() {
        super(Place.class);
    }

    @Override
    public List<Place> findByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "from Place p where lower(p.name) like lower(:name) order by p.name",
                    Place.class
            ).setParameter("name", "%" + name + "%").list();
        }
    }

    @Override
    public List<Place> getPlacesByPerson(Person person) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "select pp.place from PersonPlace pp where pp.person = :person order by pp.place.name",
                    Place.class
            ).setParameter("person", person).list();
        }
    }
}
