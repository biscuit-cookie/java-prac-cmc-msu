package ru.cmc.msu.genealogy.dao.impl;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.testng.annotations.Test;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.Relation;
import ru.cmc.msu.genealogy.models.RelationType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

public class RelationDAOImplMockTest extends DAOImplTestSupport {

    @SuppressWarnings("unchecked")
    @Test
    public void testQueryMethodsSuccessAndFailure() {
        Person person = new Person(1L, "Иван", "Мужской", null, null, "desc");
        Person other = new Person(2L, "Анна", "Женский", null, null, "desc");
        Relation relation = new Relation(1L, person, other, 1900, null, RelationType.PARTNER);

        RelationDAOImpl targetDao = new RelationDAOImpl();
        Session targetSession = mock(Session.class);
        Query<Relation> targetQuery = mock(Query.class);
        when(targetSession.createQuery(anyString(), eq(Relation.class))).thenReturn(targetQuery);
        when(targetQuery.setParameter(eq("person"), eq(person))).thenReturn(targetQuery);
        when(targetQuery.list()).thenReturn(Collections.singletonList(relation));
        injectSessionFactory(targetDao, mockSessionFactoryReturning(targetSession));
        assertEquals(targetDao.getRelationsByTargetPerson(person).size(), 1);
        verify(targetSession).close();

        RelationDAOImpl relatedDao = new RelationDAOImpl();
        Session relatedSession = mock(Session.class);
        Query<Relation> relatedQuery = mock(Query.class);
        when(relatedSession.createQuery(anyString(), eq(Relation.class))).thenReturn(relatedQuery);
        when(relatedQuery.setParameter(eq("person"), eq(person))).thenReturn(relatedQuery);
        when(relatedQuery.list()).thenReturn(Collections.singletonList(relation));
        injectSessionFactory(relatedDao, mockSessionFactoryReturning(relatedSession));
        assertEquals(relatedDao.getRelationsByRelatedPerson(person).size(), 1);
        verify(relatedSession).close();

        RelationDAOImpl peopleDao = new RelationDAOImpl();
        Session peopleSession = mock(Session.class);
        Query<Person> peopleQuery = mock(Query.class);
        when(peopleSession.createQuery(anyString(), eq(Person.class))).thenReturn(peopleQuery);
        when(peopleQuery.setParameter(eq("person"), eq(person))).thenReturn(peopleQuery);
        when(peopleQuery.setParameter(eq("relationType"), eq(RelationType.PARTNER))).thenReturn(peopleQuery);
        when(peopleQuery.list()).thenReturn(Collections.singletonList(other));
        injectSessionFactory(peopleDao, mockSessionFactoryReturning(peopleSession));
        assertEquals(peopleDao.getRelatedPeopleByType(person, RelationType.PARTNER).size(), 1);
        verify(peopleSession).close();

        RelationDAOImpl targetPeopleDao = new RelationDAOImpl();
        Session targetPeopleSession = mock(Session.class);
        Query<Person> targetPeopleQuery = mock(Query.class);
        when(targetPeopleSession.createQuery(anyString(), eq(Person.class))).thenReturn(targetPeopleQuery);
        when(targetPeopleQuery.setParameter(eq("person"), eq(person))).thenReturn(targetPeopleQuery);
        when(targetPeopleQuery.setParameter(eq("relationType"), eq(RelationType.PARTNER))).thenReturn(targetPeopleQuery);
        when(targetPeopleQuery.list()).thenReturn(Collections.singletonList(other));
        injectSessionFactory(targetPeopleDao, mockSessionFactoryReturning(targetPeopleSession));
        assertEquals(targetPeopleDao.getTargetPeopleByType(person, RelationType.PARTNER).size(), 1);
        verify(targetPeopleSession).close();

        RelationDAOImpl failureDao = new RelationDAOImpl();
        Session failureSession = mock(Session.class);
        when(failureSession.createQuery(anyString(), eq(Relation.class))).thenThrow(new RuntimeException("query boom"));
        injectSessionFactory(failureDao, mockSessionFactoryReturning(failureSession));
        expectThrows(RuntimeException.class, () -> failureDao.getRelationsByTargetPerson(person));
        verify(failureSession).close();
    }

    @Test
    public void testGetRelationsByPersonDeduplicatesAndKeepsOrder() {
        RelationDAOImpl dao = spy(new RelationDAOImpl());
        Person person = new Person(1L, "Иван", "Мужской", null, null, "desc");
        Relation first = new Relation(1L, person, new Person(2L, "A", "Женский", null, null, "a"), 1900, null, RelationType.PARTNER);
        Relation second = new Relation(2L, new Person(3L, "B", "Мужской", null, null, "b"), person, 1901, null, RelationType.WEDLOCK_CHILD);

        doReturn(Arrays.asList(first, second)).when(dao).getRelationsByTargetPerson(person);
        doReturn(Arrays.asList(second)).when(dao).getRelationsByRelatedPerson(person);

        List<Relation> relations = dao.getRelationsByPerson(person);
        assertEquals(relations.size(), 2);
        assertEquals(relations.get(0), first);
        assertEquals(relations.get(1), second);
    }
}
