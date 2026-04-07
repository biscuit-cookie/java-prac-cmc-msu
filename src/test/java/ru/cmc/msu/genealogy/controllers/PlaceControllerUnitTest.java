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

    @Test
    public void craftedPlaceRequestsReturnDedicatedErrors() {
        PlaceDAO placeDAO = mock(PlaceDAO.class);
        PersonPlaceDAO personPlaceDAO = mock(PersonPlaceDAO.class);
        PlaceController controller = new PlaceController(placeDAO, personPlaceDAO);

        ExtendedModelMap missingPlaceUpdateModel = new ExtendedModelMap();
        assertEquals(controller.savePlacePage(9999L, "Призрачное место", "Описание отсутствующей записи",
                missingPlaceUpdateModel), "error");
        assertEquals(missingPlaceUpdateModel.getAttribute("errorMessage"),
                "Нельзя сохранить изменения: место с ID = 9999 не найдено.");

        ExtendedModelMap missingPlaceDeleteModel = new ExtendedModelMap();
        assertEquals(controller.removePlacePage(9999L, missingPlaceDeleteModel), "error");
        assertEquals(missingPlaceDeleteModel.getAttribute("errorMessage"),
                "Нельзя удалить место: запись не найдена.");
    }

    @Test
    public void placeAndEditPlacePagesReturnErrorsForMissingEntities() {
        PlaceDAO placeDAO = mock(PlaceDAO.class);
        PersonPlaceDAO personPlaceDAO = mock(PersonPlaceDAO.class);
        PlaceController controller = new PlaceController(placeDAO, personPlaceDAO);

        ExtendedModelMap missingPlacePageModel = new ExtendedModelMap();
        assertEquals(controller.placePage(9999L, missingPlacePageModel), "error");
        assertEquals(missingPlacePageModel.getAttribute("errorMessage"), "В базе нет места с ID = 9999");

        ExtendedModelMap missingEditPlaceModel = new ExtendedModelMap();
        assertEquals(controller.editPlacePage(9999L, missingEditPlaceModel), "error");
        assertEquals(missingEditPlaceModel.getAttribute("errorMessage"), "В базе нет места с ID = 9999");
    }
}
