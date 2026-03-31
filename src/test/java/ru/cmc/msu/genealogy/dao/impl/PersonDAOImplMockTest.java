package ru.cmc.msu.genealogy.dao.impl;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.testng.annotations.Test;
import ru.cmc.msu.genealogy.models.Person;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.expectThrows;

public class PersonDAOImplMockTest extends DAOImplTestSupport {

    @SuppressWarnings("unchecked")
    @Test
    public void testReadMethodsSuccessAndFailure() {
        Person person = new Person(1L, "Иван", "Мужской", null, null, "desc");

        PersonDAOImpl findByNameDao = new PersonDAOImpl();
        Session findByNameSession = mock(Session.class);
        Query<Person> findByNameQuery = mock(Query.class);
        when(findByNameSession.createQuery(anyString(), eq(Person.class))).thenReturn(findByNameQuery);
        when(findByNameQuery.setParameter(eq("name"), eq("%ив%"))).thenReturn(findByNameQuery);
        when(findByNameQuery.list()).thenReturn(Collections.singletonList(person));
        injectSessionFactory(findByNameDao, mockSessionFactoryReturning(findByNameSession));
        assertEquals(findByNameDao.findByName("ив").size(), 1);
        verify(findByNameSession).close();

        PersonDAOImpl exactDao = new PersonDAOImpl();
        Session exactSession = mock(Session.class);
        Query<Person> exactQuery = mock(Query.class);
        when(exactSession.createQuery(anyString(), eq(Person.class))).thenReturn(exactQuery);
        when(exactQuery.setParameter(eq("name"), eq("Иван"))).thenReturn(exactQuery);
        when(exactQuery.uniqueResult()).thenReturn(person);
        injectSessionFactory(exactDao, mockSessionFactoryReturning(exactSession));
        assertSame(exactDao.findExactByName("Иван"), person);
        verify(exactSession).close();

        PersonDAOImpl orderedByNameDao = new PersonDAOImpl();
        Session orderedByNameSession = mock(Session.class);
        Query<Person> orderedByNameQuery = mock(Query.class);
        when(orderedByNameSession.createQuery(anyString(), eq(Person.class))).thenReturn(orderedByNameQuery);
        when(orderedByNameQuery.list()).thenReturn(Collections.singletonList(person));
        injectSessionFactory(orderedByNameDao, mockSessionFactoryReturning(orderedByNameSession));
        assertEquals(orderedByNameDao.getAllOrderedByName().size(), 1);
        verify(orderedByNameSession).close();

        PersonDAOImpl orderedByBirthDao = new PersonDAOImpl();
        Session orderedByBirthSession = mock(Session.class);
        Query<Person> orderedByBirthQuery = mock(Query.class);
        when(orderedByBirthSession.createQuery(anyString(), eq(Person.class))).thenReturn(orderedByBirthQuery);
        when(orderedByBirthQuery.list()).thenReturn(Collections.singletonList(person));
        injectSessionFactory(orderedByBirthDao, mockSessionFactoryReturning(orderedByBirthSession));
        assertEquals(orderedByBirthDao.getAllOrderedByBirthDate().size(), 1);
        verify(orderedByBirthSession).close();

        PersonDAOImpl failureDao = new PersonDAOImpl();
        Session failureSession = mock(Session.class);
        when(failureSession.createQuery(anyString(), eq(Person.class))).thenThrow(new RuntimeException("query boom"));
        injectSessionFactory(failureDao, mockSessionFactoryReturning(failureSession));
        expectThrows(RuntimeException.class, () -> failureDao.findByName("x"));
        verify(failureSession).close();
    }
}
