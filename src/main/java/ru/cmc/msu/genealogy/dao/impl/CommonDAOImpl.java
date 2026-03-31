package ru.cmc.msu.genealogy.dao.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.cmc.msu.genealogy.dao.CommonDAO;
import ru.cmc.msu.genealogy.models.CommonEntity;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Repository
public abstract class CommonDAOImpl<T extends CommonEntity<ID>, ID extends Serializable> implements CommonDAO<T, ID> {

    @Autowired
    protected SessionFactory sessionFactory;

    private final Class<T> persistentClass;

    protected CommonDAOImpl(Class<T> persistentClass) {
        this.persistentClass = persistentClass;
    }

    @Override
    public T getById(ID id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(persistentClass, id);
        }
    }

    @Override
    public List<T> getAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from " + persistentClass.getSimpleName(), persistentClass).list();
        }
    }

    @Override
    public T save(T entity) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(entity);
            session.getTransaction().commit();
            return entity;
        }
    }

    @Override
    public void saveCollection(Collection<T> entities) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            for (T entity : entities) {
                session.save(entity);
            }
            session.getTransaction().commit();
        }
    }

    @Override
    public T update(T entity) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.update(entity);
            session.getTransaction().commit();
            return entity;
        }
    }

    @Override
    public void delete(T entity) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(session.merge(entity));
            session.getTransaction().commit();
        }
    }

    @Override
    public void deleteById(ID id) {
        T entity = getById(id);
        if (entity != null) {
            delete(entity);
        }
    }
}
