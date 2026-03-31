package ru.cmc.msu.genealogy.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;

import javax.sql.DataSource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
public abstract class AbstractDAOTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private DataSource dataSource;

    @BeforeMethod
    public void resetDatabase() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("sql/create.sql"));
        populator.addScript(new ClassPathResource("sql/insert.sql"));
        populator.execute(dataSource);
    }
}
