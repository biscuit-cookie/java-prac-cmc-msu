package ru.cmc.msu.genealogy.dao.impl;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.test.util.ReflectionTestUtils;

abstract class DAOImplTestSupport {

    protected void injectSessionFactory(Object dao, SessionFactory sessionFactory) {
        ReflectionTestUtils.setField(dao, "sessionFactory", sessionFactory);
    }

    protected SessionFactory mockSessionFactoryReturning(Session session) {
        SessionFactory sessionFactory = org.mockito.Mockito.mock(SessionFactory.class);
        org.mockito.Mockito.when(sessionFactory.openSession()).thenReturn(session);
        return sessionFactory;
    }
}
