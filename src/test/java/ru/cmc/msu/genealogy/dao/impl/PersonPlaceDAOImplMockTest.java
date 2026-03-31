package ru.cmc.msu.genealogy.dao.impl;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.testng.annotations.Test;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.PersonPlace;
import ru.cmc.msu.genealogy.models.PersonPlaceId;
import ru.cmc.msu.genealogy.models.Place;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class PersonPlaceDAOImplMockTest extends DAOImplTestSupport {

    @SuppressWarnings("unchecked")
    @Test
    public void testReadMethodsSuccessAndFailure() {
        Person person = new Person(1L, "Иван", "Мужской", null, null, "desc");
        Place place = new Place(2L, "Москва", "desc");
        PersonPlace link = new PersonPlace(new PersonPlaceId(2L, 1L), person, place);

        PersonPlaceDAOImpl byPersonDao = new PersonPlaceDAOImpl();
        Session byPersonSession = mock(Session.class);
        Query<PersonPlace> byPersonQuery = mock(Query.class);
        when(byPersonSession.createQuery(anyString(), eq(PersonPlace.class))).thenReturn(byPersonQuery);
        when(byPersonQuery.setParameter(eq("person"), eq(person))).thenReturn(byPersonQuery);
        when(byPersonQuery.list()).thenReturn(Collections.singletonList(link));
        injectSessionFactory(byPersonDao, mockSessionFactoryReturning(byPersonSession));
        assertEquals(byPersonDao.getByPerson(person).size(), 1);
        verify(byPersonSession).close();

        PersonPlaceDAOImpl byPlaceDao = new PersonPlaceDAOImpl();
        Session byPlaceSession = mock(Session.class);
        Query<PersonPlace> byPlaceQuery = mock(Query.class);
        when(byPlaceSession.createQuery(anyString(), eq(PersonPlace.class))).thenReturn(byPlaceQuery);
        when(byPlaceQuery.setParameter(eq("place"), eq(place))).thenReturn(byPlaceQuery);
        when(byPlaceQuery.list()).thenReturn(Collections.singletonList(link));
        injectSessionFactory(byPlaceDao, mockSessionFactoryReturning(byPlaceSession));
        assertEquals(byPlaceDao.getByPlace(place).size(), 1);
        verify(byPlaceSession).close();

        PersonPlaceDAOImpl failureDao = new PersonPlaceDAOImpl();
        Session failureSession = mock(Session.class);
        when(failureSession.createQuery(anyString(), eq(PersonPlace.class))).thenThrow(new RuntimeException("query boom"));
        injectSessionFactory(failureDao, mockSessionFactoryReturning(failureSession));
        expectThrows(RuntimeException.class, () -> failureDao.getByPerson(person));
        verify(failureSession).close();
    }
}
