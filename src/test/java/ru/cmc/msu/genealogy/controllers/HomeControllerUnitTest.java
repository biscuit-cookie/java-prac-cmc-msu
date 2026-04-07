package ru.cmc.msu.genealogy.controllers;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.View;
import org.testng.annotations.Test;
import ru.cmc.msu.genealogy.dao.PersonDAO;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.services.GenealogyTreeService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
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
    public void treeRejectsDepthOutsideSupportedRange() {
        PersonDAO personDAO = mock(PersonDAO.class);
        GenealogyTreeService genealogyTreeService = mock(GenealogyTreeService.class);
        HomeController controller = new HomeController(personDAO, genealogyTreeService);

        ExtendedModelMap tooSmallModel = new ExtendedModelMap();
        assertEquals(controller.tree(1L, null, "ancestors", 0, tooSmallModel), "error");
        assertEquals(tooSmallModel.getAttribute("errorMessage"), "Глубина дерева должна быть числом от 1 до 10.");

        ExtendedModelMap tooLargeModel = new ExtendedModelMap();
        assertEquals(controller.tree(1L, null, "ancestors", 11, tooLargeModel), "error");
        assertEquals(tooLargeModel.getAttribute("errorMessage"), "Глубина дерева должна быть числом от 1 до 10.");
    }

    @Test
    public void treeReturnsInitialStateWhenParametersAreNotChosen() {
        PersonDAO personDAO = mock(PersonDAO.class);
        GenealogyTreeService genealogyTreeService = mock(GenealogyTreeService.class);
        HomeController controller = new HomeController(personDAO, genealogyTreeService);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(controller.tree(null, null, "ancestors", 2, model), "tree");
        assertEquals(model.getAttribute("primaryPerson"), null);
        assertEquals(model.getAttribute("secondaryPerson"), null);
        assertEquals(model.getAttribute("direction"), "ancestors");
        assertEquals(model.getAttribute("depth"), 2);
    }

    @Test
    public void treeRequestUsesDefaultDirectionAndDepthFromRequestParameters() throws Exception {
        PersonDAO personDAO = mock(PersonDAO.class);
        GenealogyTreeService genealogyTreeService = mock(GenealogyTreeService.class);
        HomeController controller = new HomeController(personDAO, genealogyTreeService);
        MockMvc mockMvc = mockMvcFor(controller);

        Person primary = new Person(6L, "Игорь", "Мужской", 1938, null, "desc");
        GenealogyTreeService.TreeResult result = new GenealogyTreeService.TreeResult(
                primary, null, GenealogyTreeService.Direction.ANCESTORS, 2,
                null, null, List.of(), List.of(), null
        );

        when(personDAO.getById(6L)).thenReturn(primary);
        when(genealogyTreeService.buildTree(primary, null, "ancestors", 2)).thenReturn(result);

        mockMvc.perform(get("/tree").param("primaryPersonId", "6"))
                .andExpect(status().isOk())
                .andExpect(view().name("tree"))
                .andExpect(model().attribute("direction", "ancestors"))
                .andExpect(model().attribute("depth", 2))
                .andExpect(model().attribute("primaryPerson", primary))
                .andExpect(model().attribute("treeResult", result));
    }

    @Test
    public void treeRequestShowsDedicatedErrorsForInvalidParameters() throws Exception {
        PersonDAO personDAO = mock(PersonDAO.class);
        GenealogyTreeService genealogyTreeService = mock(GenealogyTreeService.class);
        HomeController controller = new HomeController(personDAO, genealogyTreeService);
        MockMvc mockMvc = mockMvcFor(controller);

        mockMvc.perform(get("/tree").param("primaryPersonId", "9999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage",
                        "Не удалось построить дерево: человек с указанным ID не найден."));

        Person primary = new Person(1L, "Рюрик Старший", "Мужской", 1870, null, "desc");
        when(personDAO.getById(1L)).thenReturn(primary);

        mockMvc.perform(get("/tree").param("primaryPersonId", "1").param("secondaryPersonId", "9999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage",
                        "Не удалось построить дерево: второй человек с указанным ID не найден."));

        mockMvc.perform(get("/tree").param("primaryPersonId", "1").param("depth", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage",
                        "Глубина дерева должна быть числом от 1 до 10."));

        mockMvc.perform(get("/tree").param("primaryPersonId", "1").param("depth", "11"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("errorMessage",
                        "Глубина дерева должна быть числом от 1 до 10."));

        mockMvc.perform(get("/tree"))
                .andExpect(status().isOk())
                .andExpect(view().name("tree"))
                .andExpect(model().attribute("direction", "ancestors"))
                .andExpect(model().attribute("depth", 2))
                .andExpect(model().attributeDoesNotExist("treeResult"))
                .andExpect(result -> {
                    assertNull(result.getModelAndView().getModel().get("primaryPerson"));
                    assertNull(result.getModelAndView().getModel().get("secondaryPerson"));
                });
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

    private MockMvc mockMvcFor(HomeController controller) {
        return MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(this::noOpView)
                .build();
    }

    private View noOpView(String viewName, Locale locale) {
        return new View() {
            @Override
            public String getContentType() {
                return "text/html";
            }

            @Override
            public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {
                // ModelAndView assertions do not require template rendering
            }
        };
    }
}
