package ru.cmc.msu.genealogy.controllers;

import org.springframework.ui.ExtendedModelMap;
import org.testng.annotations.Test;
import ru.cmc.msu.genealogy.dao.PersonPlaceDAO;
import ru.cmc.msu.genealogy.dao.PlaceDAO;
import ru.cmc.msu.genealogy.models.Place;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class PlaceControllerUnitTest {

    @Test
    public void placesListTreatsBlankSearchAsEmpty() {
        PlaceDAO placeDAO = mock(PlaceDAO.class);
        PersonPlaceDAO personPlaceDAO = mock(PersonPlaceDAO.class);
        when(placeDAO.getAll()).thenReturn(Collections.singletonList(new Place(1L, "Москва", "desc")));
        PlaceController controller = new PlaceController(placeDAO, personPlaceDAO);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals(controller.placesListPage("   ", model), "places");
        assertEquals(model.getAttribute("search"), "   ");
    }

    @Test
    public void savePlaceRejectsBlankNameAndBlankDescription() {
        PlaceDAO placeDAO = mock(PlaceDAO.class);
        PersonPlaceDAO personPlaceDAO = mock(PersonPlaceDAO.class);
        PlaceController controller = new PlaceController(placeDAO, personPlaceDAO);

        ExtendedModelMap blankNameModel = new ExtendedModelMap();
        assertEquals(controller.savePlacePage(null, "   ", "desc", blankNameModel), "error");
        assertEquals(blankNameModel.getAttribute("errorMessage"),
                "Название и описание места обязательны для заполнения.");

        ExtendedModelMap blankDescriptionModel = new ExtendedModelMap();
        assertEquals(controller.savePlacePage(null, "Name", "   ", blankDescriptionModel), "error");
        assertEquals(blankDescriptionModel.getAttribute("errorMessage"),
                "Название и описание места обязательны для заполнения.");
    }
}
