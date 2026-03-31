package ru.cmc.msu.genealogy.dao.impl;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import ru.cmc.msu.genealogy.dao.PersonDAO;
import ru.cmc.msu.genealogy.models.Person;

import java.util.List;

@Repository
public class PersonDAOImpl extends CommonDAOImpl<Person, Long> implements PersonDAO {

    public PersonDAOImpl() {
        super(Person.class);
    }

    @Override
    public List<Person> findByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "from Person p where lower(p.name) like lower(:name) order by p.name",
                    Person.class
            ).setParameter("name", "%" + name + "%").list();
        }
    }

    @Override
    public Person findExactByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "from Person p where lower(p.name) = lower(:name)",
                    Person.class
            ).setParameter("name", name).uniqueResult();
        }
    }

    @Override
    public List<Person> getAllOrderedByName() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "from Person p order by p.name asc",
                    Person.class
            ).list();
        }
    }

    @Override
    public List<Person> getAllOrderedByBirthDate() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                    "from Person p order by p.dateOfBirth asc nulls last, p.name asc",
                    Person.class
            ).list();
        }
    }
}
