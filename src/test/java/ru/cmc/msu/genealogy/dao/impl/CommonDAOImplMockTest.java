package ru.cmc.msu.genealogy.dao.impl;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.testng.annotations.Test;
import ru.cmc.msu.genealogy.models.Person;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.expectThrows;

public class CommonDAOImplMockTest extends DAOImplTestSupport {

    @SuppressWarnings("unchecked")
    @Test
    public void testGetByIdAndGetAllSuccess() {
        PersonDAOImpl dao = new PersonDAOImpl();
        Session session = mock(Session.class);
        Query<Person> query = mock(Query.class);
        Person person = new Person(1L, "Иван", "Мужской", null, null, "desc");

        when(session.get(Person.class, 1L)).thenReturn(person);
        when(session.createQuery(anyString(), eq(Person.class))).thenReturn(query);
        when(query.list()).thenReturn(Collections.singletonList(person));
        injectSessionFactory(dao, mockSessionFactoryReturning(session));

        assertSame(dao.getById(1L), person);
        List<Person> result = dao.getAll();
        assertEquals(result.size(), 1);
        assertSame(result.get(0), person);
        verify(session, times(2)).close();
    }

    @Test
    public void testGetByIdAndGetAllFailure() {
        PersonDAOImpl getByIdDao = new PersonDAOImpl();
        Session getByIdSession = mock(Session.class);
        when(getByIdSession.get(Person.class, 1L)).thenThrow(new RuntimeException("getById boom"));
        injectSessionFactory(getByIdDao, mockSessionFactoryReturning(getByIdSession));
        expectThrows(RuntimeException.class, () -> getByIdDao.getById(1L));
        verify(getByIdSession).close();

        PersonDAOImpl getAllDao = new PersonDAOImpl();
        Session getAllSession = mock(Session.class);
        when(getAllSession.createQuery(anyString(), eq(Person.class))).thenThrow(new RuntimeException("getAll boom"));
        injectSessionFactory(getAllDao, mockSessionFactoryReturning(getAllSession));
        expectThrows(RuntimeException.class, getAllDao::getAll);
        verify(getAllSession).close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSaveAndSaveCollectionSuccess() {
        PersonDAOImpl saveDao = new PersonDAOImpl();
        Session saveSession = mock(Session.class);
        Transaction saveTx = mock(Transaction.class);
        when(saveSession.beginTransaction()).thenReturn(saveTx);
        when(saveSession.getTransaction()).thenReturn(saveTx);
        injectSessionFactory(saveDao, mockSessionFactoryReturning(saveSession));

        Person person = new Person(1L, "Иван", "Мужской", null, null, "desc");
        assertSame(saveDao.save(person), person);
        verify(saveSession).save(person);
        verify(saveTx).commit();
        verify(saveSession).close();

        PersonDAOImpl collectionDao = new PersonDAOImpl();
        Session collectionSession = mock(Session.class);
        Transaction collectionTx = mock(Transaction.class);
        when(collectionSession.beginTransaction()).thenReturn(collectionTx);
        when(collectionSession.getTransaction()).thenReturn(collectionTx);
        injectSessionFactory(collectionDao, mockSessionFactoryReturning(collectionSession));

        List<Person> people = Arrays.asList(
                new Person(2L, "A", "Мужской", null, null, "a"),
                new Person(3L, "B", "Женский", null, null, "b")
        );
        collectionDao.saveCollection(people);
        verify(collectionSession, times(2)).save(org.mockito.ArgumentMatchers.any(Person.class));
        verify(collectionTx).commit();
        verify(collectionSession).close();
    }

    @Test
    public void testSaveAndSaveCollectionFailure() {
        PersonDAOImpl saveDao = new PersonDAOImpl();
        Session saveSession = mock(Session.class);
        Transaction saveTx = mock(Transaction.class);
        when(saveSession.beginTransaction()).thenReturn(saveTx);
        when(saveSession.getTransaction()).thenReturn(saveTx);
        doThrow(new RuntimeException("save boom")).when(saveSession).save(org.mockito.ArgumentMatchers.any(Person.class));
        injectSessionFactory(saveDao, mockSessionFactoryReturning(saveSession));
        expectThrows(RuntimeException.class, () -> saveDao.save(new Person(1L, "X", "Мужской", null, null, "x")));
        verify(saveSession).close();

        PersonDAOImpl collectionDao = new PersonDAOImpl();
        Session collectionSession = mock(Session.class);
        Transaction collectionTx = mock(Transaction.class);
        when(collectionSession.beginTransaction()).thenReturn(collectionTx);
        when(collectionSession.getTransaction()).thenReturn(collectionTx);
        doThrow(new RuntimeException("collection boom")).when(collectionSession).save(org.mockito.ArgumentMatchers.any(Person.class));
        injectSessionFactory(collectionDao, mockSessionFactoryReturning(collectionSession));
        expectThrows(RuntimeException.class, () -> collectionDao.saveCollection(
                Collections.singletonList(new Person(2L, "Y", "Женский", null, null, "y"))
        ));
        verify(collectionSession).close();
    }

    @Test
    public void testUpdateDeleteAndDeleteByIdSuccessBranches() {
        Person person = new Person(1L, "Иван", "Мужской", null, null, "desc");

        PersonDAOImpl updateDao = new PersonDAOImpl();
        Session updateSession = mock(Session.class);
        Transaction updateTx = mock(Transaction.class);
        when(updateSession.beginTransaction()).thenReturn(updateTx);
        when(updateSession.getTransaction()).thenReturn(updateTx);
        injectSessionFactory(updateDao, mockSessionFactoryReturning(updateSession));
        assertSame(updateDao.update(person), person);
        verify(updateSession).update(person);
        verify(updateTx).commit();
        verify(updateSession).close();

        PersonDAOImpl deleteDao = new PersonDAOImpl();
        Session deleteSession = mock(Session.class);
        Transaction deleteTx = mock(Transaction.class);
        when(deleteSession.beginTransaction()).thenReturn(deleteTx);
        when(deleteSession.getTransaction()).thenReturn(deleteTx);
        when(deleteSession.merge(person)).thenReturn(person);
        injectSessionFactory(deleteDao, mockSessionFactoryReturning(deleteSession));
        deleteDao.delete(person);
        verify(deleteSession).merge(person);
        verify(deleteSession).delete(person);
        verify(deleteTx).commit();
        verify(deleteSession).close();

        PersonDAOImpl deleteByIdExistingDao = new PersonDAOImpl();
        Session firstSession = mock(Session.class);
        Session secondSession = mock(Session.class);
        Transaction secondTx = mock(Transaction.class);
        org.hibernate.SessionFactory sessionFactory = mock(org.hibernate.SessionFactory.class);
        when(sessionFactory.openSession()).thenReturn(firstSession, secondSession);
        when(firstSession.get(Person.class, 1L)).thenReturn(person);
        when(secondSession.beginTransaction()).thenReturn(secondTx);
        when(secondSession.getTransaction()).thenReturn(secondTx);
        when(secondSession.merge(person)).thenReturn(person);
        injectSessionFactory(deleteByIdExistingDao, sessionFactory);
        deleteByIdExistingDao.deleteById(1L);
        verify(firstSession).close();
        verify(secondSession).delete(person);
        verify(secondSession).close();

        PersonDAOImpl deleteByIdMissingDao = new PersonDAOImpl();
        Session missingSession = mock(Session.class);
        when(missingSession.get(Person.class, 99L)).thenReturn(null);
        injectSessionFactory(deleteByIdMissingDao, mockSessionFactoryReturning(missingSession));
        deleteByIdMissingDao.deleteById(99L);
        verify(missingSession).close();
    }

    @Test
    public void testUpdateAndDeleteFailure() {
        Person person = new Person(1L, "Иван", "Мужской", null, null, "desc");

        PersonDAOImpl updateDao = new PersonDAOImpl();
        Session updateSession = mock(Session.class);
        Transaction updateTx = mock(Transaction.class);
        when(updateSession.beginTransaction()).thenReturn(updateTx);
        when(updateSession.getTransaction()).thenReturn(updateTx);
        doThrow(new RuntimeException("update boom")).when(updateSession).update(person);
        injectSessionFactory(updateDao, mockSessionFactoryReturning(updateSession));
        expectThrows(RuntimeException.class, () -> updateDao.update(person));
        verify(updateSession).close();

        PersonDAOImpl deleteDao = new PersonDAOImpl();
        Session deleteSession = mock(Session.class);
        Transaction deleteTx = mock(Transaction.class);
        when(deleteSession.beginTransaction()).thenReturn(deleteTx);
        when(deleteSession.getTransaction()).thenReturn(deleteTx);
        when(deleteSession.merge(person)).thenReturn(person);
        doThrow(new RuntimeException("delete boom")).when(deleteSession).delete(person);
        injectSessionFactory(deleteDao, mockSessionFactoryReturning(deleteSession));
        expectThrows(RuntimeException.class, () -> deleteDao.delete(person));
        verify(deleteSession).close();
    }
}
