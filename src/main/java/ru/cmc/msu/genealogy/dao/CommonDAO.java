package ru.cmc.msu.genealogy.dao;

import ru.cmc.msu.genealogy.models.CommonEntity;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface CommonDAO<T extends CommonEntity<ID>, ID extends Serializable> {
    // Returns an entity by its identifier or null if no such row exists.
    T getById(ID id);

    // Returns all rows of the mapped entity type.
    List<T> getAll();

    // Persists a new entity instance in the database.
    T save(T entity);

    // Persists several entity instances within one transaction.
    void saveCollection(Collection<T> entities);

    // Writes modified entity state back to the database.
    T update(T entity);

    // Removes the given entity from the database.
    void delete(T entity);

    // Removes an entity by identifier if it exists.
    void deleteById(ID id);
}
