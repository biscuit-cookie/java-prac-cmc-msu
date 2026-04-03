package ru.cmc.msu.genealogy.controllers;

import org.springframework.ui.ExtendedModelMap;
import org.testng.annotations.Test;
import ru.cmc.msu.genealogy.dao.PersonDAO;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.services.GenealogyTreeService;

import java.util.Collections;
import java.util.List;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

public class HomeControllerUnitTest {

    @Test
    public void indexAndGenerateTreePopulateModel() {
        PersonDAO personDAO = mock(PersonDAO.class);
        GenealogyTreeService genealogyTreeService = mock(GenealogyTreeService.class);
        HomeController controller = new HomeController(personDAO, genealogyTreeService);

        Person person = new Person(1L, "Игорь", "Мужской", 1900, null, "desc");
        when(personDAO.getAll()).thenReturn(List.of(person, new Person(2L, "Мария", "Женский", 1901, null, "desc")));
        when(personDAO.getAllOrderedByName()).thenReturn(List.of(person));

        ExtendedModelMap indexModel = new ExtendedModelMap();
        assertEquals(controller.index(indexModel), "index");
        assertEquals(indexModel.getAttribute("personCount"), 2);

        ExtendedModelMap generateModel = new ExtendedModelMap();
        assertEquals(controller.generateTree(generateModel), "generateTree");
        assertSame(generateModel.getAttribute("people"), personDAO.getAllOrderedByName());
    }

    @Test
    public void errorPageUsesDefaultMessageWhenRequestMessageIsNull() {
        PersonDAO personDAO = mock(PersonDAO.class);
        GenealogyTreeService genealogyTreeService = mock(GenealogyTreeService.class);
        HomeController controller = new HomeController(personDAO, genealogyTreeService);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(controller.error(null, model), "error");
        assertEquals(model.getAttribute("errorMessage"), "Произошла ошибка при обработке запроса.");
    }

    @Test
    public void errorPageUsesExplicitMessageWhenItIsProvided() {
        PersonDAO personDAO = mock(PersonDAO.class);
        GenealogyTreeService genealogyTreeService = mock(GenealogyTreeService.class);
        HomeController controller = new HomeController(personDAO, genealogyTreeService);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(controller.error("Точное сообщение", model), "error");
        assertEquals(model.getAttribute("errorMessage"), "Точное сообщение");
    }

    @Test
    public void treeBuildsModelForExistingPeopleAndHandlesMissingOnes() {
        PersonDAO personDAO = mock(PersonDAO.class);
        GenealogyTreeService genealogyTreeService = mock(GenealogyTreeService.class);
        HomeController controller = new HomeController(personDAO, genealogyTreeService);

        Person primary = new Person(1L, "Игорь", "Мужской", 1900, null, "desc");
        Person secondary = new Person(2L, "Мария", "Женский", 1901, null, "desc");
        GenealogyTreeService.TreeResult result = new GenealogyTreeService.TreeResult(
                primary, secondary, GenealogyTreeService.Direction.MIXED, 2,
                null, null, List.of(), List.of(), null
        );

        when(personDAO.getById(1L)).thenReturn(primary);
        when(personDAO.getById(2L)).thenReturn(secondary);
        when(genealogyTreeService.buildTree(primary, secondary, "mixed", 2)).thenReturn(result);

        ExtendedModelMap successModel = new ExtendedModelMap();
        assertEquals(controller.tree(1L, 2L, "mixed", 2, successModel), "tree");
        assertSame(successModel.getAttribute("treeResult"), result);

        when(personDAO.getById(99L)).thenReturn(null);
        ExtendedModelMap missingPrimaryModel = new ExtendedModelMap();
        assertEquals(controller.tree(99L, null, "ancestors", 2, missingPrimaryModel), "error");

        ExtendedModelMap missingSecondaryModel = new ExtendedModelMap();
        assertEquals(controller.tree(1L, 99L, "ancestors", 2, missingSecondaryModel), "error");
    }

    @Test
    public void treeRejectsNullDepth() {
        PersonDAO personDAO = mock(PersonDAO.class);
        when(personDAO.getAll()).thenReturn(Collections.emptyList());
        GenealogyTreeService genealogyTreeService = mock(GenealogyTreeService.class);
        HomeController controller = new HomeController(personDAO, genealogyTreeService);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(controller.tree(null, null, "ancestors", null, model), "error");
        assertEquals(model.getAttribute("errorMessage"), "Глубина дерева должна быть числом от 1 до 10.");
    }

    @Test
    public void downloadTreeReturnsJsonAttachmentForValidRequest() {
        PersonDAO personDAO = mock(PersonDAO.class);
        GenealogyTreeService genealogyTreeService = mock(GenealogyTreeService.class);
        HomeController controller = new HomeController(personDAO, genealogyTreeService);

        Person primary = new Person(1L, "Игорь", "Мужской", 1900, null, "desc");
        GenealogyTreeService.TreeResult result = new GenealogyTreeService.TreeResult(
                primary, null, GenealogyTreeService.Direction.ANCESTORS, 2,
                null, null, List.of(), List.of(), null
        );

        when(personDAO.getById(1L)).thenReturn(primary);
        when(genealogyTreeService.buildTree(primary, null, "ancestors", 2)).thenReturn(result);

        var response = controller.downloadTree(1L, null, "ancestors", 2);
        assertEquals(response.getStatusCodeValue(), 200);
        assertEquals(response.getBody(), result);
        assertTrue(response.getHeaders().getFirst(CONTENT_DISPOSITION).contains("genealogy-tree-person-1.json"));
    }

    @Test
    public void downloadTreeReturnsJsonErrorForInvalidRequest() {
        PersonDAO personDAO = mock(PersonDAO.class);
        GenealogyTreeService genealogyTreeService = mock(GenealogyTreeService.class);
        HomeController controller = new HomeController(personDAO, genealogyTreeService);

        var response = controller.downloadTree(null, null, "ancestors", 0);
        assertEquals(response.getStatusCodeValue(), 400);
        assertTrue(response.getBody().toString().contains("Глубина дерева должна быть числом от 1 до 10."));
    }

    @Test
    public void downloadTreeReturnsJsonErrorWhenPrimaryPersonIsNotChosen() {
        PersonDAO personDAO = mock(PersonDAO.class);
        GenealogyTreeService genealogyTreeService = mock(GenealogyTreeService.class);
        HomeController controller = new HomeController(personDAO, genealogyTreeService);

        var response = controller.downloadTree(null, null, "mixed", 2);
        assertEquals(response.getStatusCodeValue(), 400);
        assertTrue(response.getBody().toString().contains("основной человек не выбран"));
    }
}
