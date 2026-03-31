package ru.cmc.msu.genealogy.dao.impl;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.testng.annotations.Test;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.Place;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class PlaceDAOImplMockTest extends DAOImplTestSupport {

    @SuppressWarnings("unchecked")
    @Test
    public void testReadMethodsSuccessAndFailure() {
        PlaceDAOImpl dao = new PlaceDAOImpl();
        Session session = mock(Session.class);
        Query<Place> query = mock(Query.class);
        Place place = new Place(1L, "Москва", "desc");

        when(session.createQuery(anyString(), eq(Place.class))).thenReturn(query);
        when(query.setParameter(eq("name"), eq("%моск%"))).thenReturn(query);
        when(query.list()).thenReturn(Collections.singletonList(place));
        injectSessionFactory(dao, mockSessionFactoryReturning(session));
        assertEquals(dao.findByName("моск").size(), 1);
        verify(session).close();

        PlaceDAOImpl personDao = new PlaceDAOImpl();
        Session personSession = mock(Session.class);
        Query<Place> personQuery = mock(Query.class);
        when(personSession.createQuery(anyString(), eq(Place.class))).thenReturn(personQuery);
        when(personQuery.setParameter(eq("person"), eq(new Person(1L, "Иван", "Мужской", null, null, "desc"))))
                .thenReturn(personQuery);
        when(personQuery.list()).thenReturn(Collections.singletonList(place));
        injectSessionFactory(personDao, mockSessionFactoryReturning(personSession));
        assertEquals(personDao.getPlacesByPerson(new Person(1L, "Иван", "Мужской", null, null, "desc")).size(), 1);
        verify(personSession).close();

        PlaceDAOImpl failureDao = new PlaceDAOImpl();
        Session failureSession = mock(Session.class);
        when(failureSession.createQuery(anyString(), eq(Place.class))).thenThrow(new RuntimeException("query boom"));
        injectSessionFactory(failureDao, mockSessionFactoryReturning(failureSession));
        expectThrows(RuntimeException.class, () -> failureDao.findByName("x"));
        verify(failureSession).close();
    }
}
