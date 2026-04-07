package ru.cmc.msu.genealogy.controllers;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.testng.annotations.Test;
import ru.cmc.msu.genealogy.dao.PersonDAO;
import ru.cmc.msu.genealogy.dao.PersonPlaceDAO;
import ru.cmc.msu.genealogy.dao.PlaceDAO;
import ru.cmc.msu.genealogy.dao.RelationDAO;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.PersonPlace;
import ru.cmc.msu.genealogy.models.PersonPlaceId;
import ru.cmc.msu.genealogy.models.Place;
import ru.cmc.msu.genealogy.models.Relation;
import ru.cmc.msu.genealogy.models.RelationType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class PersonControllerUnitTest {

    @Test
    public void peopleListPageTreatsBlankSearchAsRegularListing() {
        PersonDAO personDAO = mock(PersonDAO.class);
        PlaceDAO placeDAO = mock(PlaceDAO.class);
        PersonPlaceDAO personPlaceDAO = mock(PersonPlaceDAO.class);
        RelationDAO relationDAO = mock(RelationDAO.class);
        PersonController controller = new PersonController(personDAO, placeDAO, personPlaceDAO, relationDAO);

        Person first = new Person(1L, "Игорь", "Мужской", 1938, null, "desc");
        Person second = new Person(2L, "Мария", "Женский", 1940, null, "desc");
        when(personDAO.getAllOrderedByName()).thenReturn(List.of(first, second));
        when(relationDAO.getRelationsByPerson(first)).thenReturn(List.of(new Relation()));
        when(relationDAO.getRelationsByPerson(second)).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals(controller.peopleListPage("   ", "name", model), "persons");
        assertEquals(model.getAttribute("search"), "   ");
        assertEquals(model.getAttribute("sort"), "name");
        @SuppressWarnings("unchecked")
        List<Person> people = (List<Person>) model.getAttribute("people");
        assertEquals(people.size(), 2);
    }

    @Test
    public void editPersonPlacePageReturnsErrorsForMissingPersonAndMissingLink() {
        PersonDAO personDAO = mock(PersonDAO.class);
        PlaceDAO placeDAO = mock(PlaceDAO.class);
        PersonPlaceDAO personPlaceDAO = mock(PersonPlaceDAO.class);
        RelationDAO relationDAO = mock(RelationDAO.class);
        PersonController controller = new PersonController(personDAO, placeDAO, personPlaceDAO, relationDAO);

        ExtendedModelMap missingPersonModel = new ExtendedModelMap();
        when(personDAO.getById(1L)).thenReturn(null);
        assertEquals(controller.editPersonPlacePage(1L, null, missingPersonModel), "error");
        assertEquals(missingPersonModel.getAttribute("errorMessage"), "В базе нет человека с ID = 1");

        Person person = new Person(2L, "Игорь", "Мужской", 1938, null, "desc");
        Place place = new Place(4L, "Казань", "desc");
        when(personDAO.getById(2L)).thenReturn(person);
        when(placeDAO.getById(4L)).thenReturn(place);
        when(personPlaceDAO.getById(new PersonPlaceId(4L, 2L))).thenReturn(null);
        ExtendedModelMap missingLinkModel = new ExtendedModelMap();
        assertEquals(controller.editPersonPlacePage(2L, 4L, missingLinkModel), "error");
        assertEquals(missingLinkModel.getAttribute("errorMessage"),
                "У человека с ID = 2 нет связи с местом ID = 4");
    }

    @Test
    public void editRelationPageReturnsErrorsForMissingPersonAndForeignRelation() {
        PersonDAO personDAO = mock(PersonDAO.class);
        PlaceDAO placeDAO = mock(PlaceDAO.class);
        PersonPlaceDAO personPlaceDAO = mock(PersonPlaceDAO.class);
        RelationDAO relationDAO = mock(RelationDAO.class);
        PersonController controller = new PersonController(personDAO, placeDAO, personPlaceDAO, relationDAO);

        ExtendedModelMap missingPersonModel = new ExtendedModelMap();
        when(personDAO.getById(1L)).thenReturn(null);
        assertEquals(controller.editRelationPage(1L, null, missingPersonModel), "error");
        assertEquals(missingPersonModel.getAttribute("errorMessage"), "В базе нет человека с ID = 1");

        Person current = new Person(2L, "Игорь", "Мужской", 1938, null, "desc");
        Person target = new Person(3L, "Павел", "Мужской", 1968, null, "desc");
        Person related = new Person(4L, "Наталья", "Женский", 1972, null, "desc");
        Relation foreignRelation = new Relation(9L, target, related, null, null, RelationType.PARTNER);

        when(personDAO.getById(2L)).thenReturn(current);
        when(relationDAO.getById(9L)).thenReturn(foreignRelation);

        ExtendedModelMap foreignRelationModel = new ExtendedModelMap();
        assertEquals(controller.editRelationPage(2L, 9L, foreignRelationModel), "error");
        assertEquals(foreignRelationModel.getAttribute("errorMessage"),
                "Связь с ID = 9 не относится к человеку с ID = 2");
    }

    @Test
    public void editRelationPageAcceptsRelationWherePersonIsRelatedSide() {
        PersonDAO personDAO = mock(PersonDAO.class);
        PlaceDAO placeDAO = mock(PlaceDAO.class);
        PersonPlaceDAO personPlaceDAO = mock(PersonPlaceDAO.class);
        RelationDAO relationDAO = mock(RelationDAO.class);
        PersonController controller = new PersonController(personDAO, placeDAO, personPlaceDAO, relationDAO);

        Person current = new Person(2L, "Игорь", "Мужской", 1938, null, "desc");
        Person target = new Person(3L, "Павел", "Мужской", 1968, null, "desc");
        Relation relation = new Relation(9L, target, current, 1990, 2001, RelationType.WEDLOCK_CHILD);

        when(personDAO.getById(2L)).thenReturn(current);
        when(personDAO.getAllOrderedByName()).thenReturn(List.of(current, target));
        when(relationDAO.getById(9L)).thenReturn(relation);

        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals(controller.editRelationPage(2L, 9L, model), "editRelation");
        PersonController.RelationFormView form =
                (PersonController.RelationFormView) model.getAttribute("relationForm");
        assertEquals(form.getRelationId(), Long.valueOf(9L));
        assertEquals(form.getRelatedPersonId(), Long.valueOf(3L));
        assertEquals(form.getRelationKind(), "CHILD");
    }

    @Test
    public void personAndEditPagesReturnErrorsForMissingEntities() {
        PersonDAO personDAO = mock(PersonDAO.class);
        PlaceDAO placeDAO = mock(PlaceDAO.class);
        PersonPlaceDAO personPlaceDAO = mock(PersonPlaceDAO.class);
        RelationDAO relationDAO = mock(RelationDAO.class);
        PersonController controller = new PersonController(personDAO, placeDAO, personPlaceDAO, relationDAO);

        ExtendedModelMap missingPersonPageModel = new ExtendedModelMap();
        assertEquals(controller.personPage(9999L, missingPersonPageModel), "error");
        assertEquals(missingPersonPageModel.getAttribute("errorMessage"), "В базе нет человека с ID = 9999");

        ExtendedModelMap missingEditPersonModel = new ExtendedModelMap();
        assertEquals(controller.editPersonPage(9999L, missingEditPersonModel), "error");
        assertEquals(missingEditPersonModel.getAttribute("errorMessage"), "В базе нет человека с ID = 9999");

        Person existingPerson = new Person(6L, "Игорь", "Мужской", 1938, null, "desc");
        when(personDAO.getById(6L)).thenReturn(existingPerson);

        ExtendedModelMap missingEditPersonPlaceModel = new ExtendedModelMap();
        assertEquals(controller.editPersonPlacePage(6L, 9999L, missingEditPersonPlaceModel), "error");
        assertEquals(missingEditPersonPlaceModel.getAttribute("errorMessage"), "В базе нет места с ID = 9999");

        ExtendedModelMap missingEditRelationModel = new ExtendedModelMap();
        assertEquals(controller.editRelationPage(6L, 9999L, missingEditRelationModel), "error");
        assertEquals(missingEditRelationModel.getAttribute("errorMessage"), "В базе нет связи с ID = 9999");
    }

    @Test
    public void savePersonRejectsBlankNameAndBlankGender() {
        PersonController controller = new PersonController(mock(PersonDAO.class), mock(PlaceDAO.class),
                mock(PersonPlaceDAO.class), mock(RelationDAO.class));

        ExtendedModelMap blankNameModel = new ExtendedModelMap();
        assertEquals(controller.savePersonPage(null, "   ", "Мужской", 2000, null, null, blankNameModel), "error");
        assertEquals(blankNameModel.getAttribute("errorMessage"), "Имя и пол обязательны для заполнения.");

        ExtendedModelMap blankGenderModel = new ExtendedModelMap();
        assertEquals(controller.savePersonPage(null, "Иван", "   ", 2000, null, null, blankGenderModel), "error");
        assertEquals(blankGenderModel.getAttribute("errorMessage"), "Имя и пол обязательны для заполнения.");
    }

    @Test
    public void savePersonAcceptsOpenEndedLifeDates() {
        PersonDAO personDAO = mock(PersonDAO.class);
        PersonController createController = new PersonController(personDAO, mock(PlaceDAO.class),
                mock(PersonPlaceDAO.class), mock(RelationDAO.class));

        when(personDAO.save(any(Person.class))).thenAnswer(invocation -> {
            Person saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        ExtendedModelMap createModel = new ExtendedModelMap();
        assertEquals(createController.savePersonPage(null, " Иван ", " Мужской ", null, 2010, "  text  ", createModel),
                "redirect:/person?personId=10");

        Person existing = new Person(11L, "Мария", "Женский", 1990, null, "desc");
        when(personDAO.getById(11L)).thenReturn(existing);

        ExtendedModelMap updateModel = new ExtendedModelMap();
        assertEquals(createController.savePersonPage(11L, " Мария ", " Женский ", 1990, null, "   ", updateModel),
                "redirect:/person?personId=11");
        assertNull(existing.getDateOfDeath());
    }

    @Test
    public void savePersonAcceptsConsistentBirthAndDeathYears() {
        PersonDAO personDAO = mock(PersonDAO.class);
        PersonController controller = new PersonController(personDAO, mock(PlaceDAO.class),
                mock(PersonPlaceDAO.class), mock(RelationDAO.class));

        Person existing = new Person(12L, "Анна", "Женский", 1990, null, "desc");
        when(personDAO.getById(12L)).thenReturn(existing);

        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals(controller.savePersonPage(12L, " Анна ", " Женский ", 1990, 2000, " text ", model),
                "redirect:/person?personId=12");
        assertEquals(existing.getDateOfBirth(), Integer.valueOf(1990));
        assertEquals(existing.getDateOfDeath(), Integer.valueOf(2000));
    }

    @Test
    public void savePersonPlaceSkipsRewriteWhenPlaceDidNotChange() {
        PersonDAO personDAO = mock(PersonDAO.class);
        PlaceDAO placeDAO = mock(PlaceDAO.class);
        PersonPlaceDAO personPlaceDAO = mock(PersonPlaceDAO.class);
        RelationDAO relationDAO = mock(RelationDAO.class);
        PersonController controller = new PersonController(personDAO, placeDAO, personPlaceDAO, relationDAO);

        Person person = new Person(2L, "Игорь", "Мужской", 1938, null, "desc");
        Place place = new Place(4L, "Казань", "desc");
        PersonPlace existingLink = new PersonPlace(person, place);

        when(personDAO.getById(2L)).thenReturn(person);
        when(placeDAO.getById(4L)).thenReturn(place);
        when(personPlaceDAO.getById(new PersonPlaceId(4L, 2L))).thenReturn(existingLink);

        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals(controller.savePersonPlacePage(2L, 4L, 4L, model), "redirect:/person?personId=2");
        verify(personPlaceDAO, never()).delete(any(PersonPlace.class));
        verify(personPlaceDAO, never()).save(any(PersonPlace.class));
    }

    @Test
    public void saveRelationAcceptsOpenEndedInterval() {
        PersonDAO personDAO = mock(PersonDAO.class);
        PlaceDAO placeDAO = mock(PlaceDAO.class);
        PersonPlaceDAO personPlaceDAO = mock(PersonPlaceDAO.class);
        RelationDAO relationDAO = mock(RelationDAO.class);
        PersonController controller = new PersonController(personDAO, placeDAO, personPlaceDAO, relationDAO);

        Person current = new Person(2L, "Игорь", "Мужской", 1938, null, "desc");
        Person other = new Person(3L, "Павел", "Мужской", 1968, null, "desc");
        when(personDAO.getById(2L)).thenReturn(current);
        when(personDAO.getById(3L)).thenReturn(other);
        when(relationDAO.save(any(Relation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExtendedModelMap model = new ExtendedModelMap();
        assertEquals(controller.saveRelationPage(2L, null, 3L, "CHILD", null, 2000, model),
                "redirect:/person?personId=2");
        verify(relationDAO).save(any(Relation.class));
    }

    @Test
    public void craftedPersonAndPersonPlaceRequestsReturnDedicatedErrors() {
        PersonDAO personDAO = mock(PersonDAO.class);
        PlaceDAO placeDAO = mock(PlaceDAO.class);
        PersonPlaceDAO personPlaceDAO = mock(PersonPlaceDAO.class);
        RelationDAO relationDAO = mock(RelationDAO.class);
        PersonController controller = new PersonController(personDAO, placeDAO, personPlaceDAO, relationDAO);

        ExtendedModelMap missingPersonUpdateModel = new ExtendedModelMap();
        assertEquals(controller.savePersonPage(9999L, "Призрак", "Мужской", 1950, null,
                "Несуществующая запись", missingPersonUpdateModel), "error");
        assertEquals(missingPersonUpdateModel.getAttribute("errorMessage"),
                "Нельзя сохранить изменения: человек с ID = 9999 не найден.");

        ExtendedModelMap missingPersonDeleteModel = new ExtendedModelMap();
        assertEquals(controller.removePersonPage(9999L, missingPersonDeleteModel), "error");
        assertEquals(missingPersonDeleteModel.getAttribute("errorMessage"),
                "Нельзя удалить человека: запись не найдена.");

        Person existingPerson = new Person(6L, "Игорь", "Мужской", 1938, null, "desc");
        Place moscow = new Place(2L, "Москва", "desc");
        Place tver = new Place(3L, "Тверь", "desc");
        Place kazan = new Place(4L, "Казань", "desc");
        when(personDAO.getById(6L)).thenReturn(existingPerson);
        when(placeDAO.getById(2L)).thenReturn(moscow);
        when(placeDAO.getById(3L)).thenReturn(tver);
        when(placeDAO.getById(4L)).thenReturn(kazan);
        when(personPlaceDAO.getById(new PersonPlaceId(2L, 6L))).thenReturn(new PersonPlace(existingPerson, moscow));
        when(personPlaceDAO.getById(new PersonPlaceId(4L, 6L))).thenReturn(new PersonPlace(existingPerson, kazan));

        ExtendedModelMap missingPersonForPlaceModel = new ExtendedModelMap();
        assertEquals(controller.savePersonPlacePage(9999L, null, 2L, missingPersonForPlaceModel), "error");
        assertEquals(missingPersonForPlaceModel.getAttribute("errorMessage"),
                "Нельзя сохранить место: человек с ID = 9999 не найден.");

        ExtendedModelMap missingPlaceModel = new ExtendedModelMap();
        assertEquals(controller.savePersonPlacePage(6L, null, 9999L, missingPlaceModel), "error");
        assertEquals(missingPlaceModel.getAttribute("errorMessage"),
                "Нельзя сохранить место: место с ID = 9999 не найдено.");

        ExtendedModelMap duplicatePlaceModel = new ExtendedModelMap();
        assertEquals(controller.savePersonPlacePage(6L, null, 2L, duplicatePlaceModel), "error");
        assertEquals(duplicatePlaceModel.getAttribute("errorMessage"),
                "Нельзя добавить место: такая связь уже существует.");

        ExtendedModelMap missingOriginalLinkModel = new ExtendedModelMap();
        assertEquals(controller.savePersonPlacePage(6L, 5L, 3L, missingOriginalLinkModel), "error");
        assertEquals(missingOriginalLinkModel.getAttribute("errorMessage"),
                "Нельзя сохранить изменения: исходная связь человека с местом не найдена.");

        ExtendedModelMap duplicateTargetPlaceModel = new ExtendedModelMap();
        assertEquals(controller.savePersonPlacePage(6L, 4L, 2L, duplicateTargetPlaceModel), "error");
        assertEquals(duplicateTargetPlaceModel.getAttribute("errorMessage"),
                "Нельзя сохранить изменения: у человека уже есть связь с этим местом.");

        ExtendedModelMap missingRemovedPlaceLinkModel = new ExtendedModelMap();
        assertEquals(controller.removePersonPlacePage(6L, 5L, missingRemovedPlaceLinkModel), "error");
        assertEquals(missingRemovedPlaceLinkModel.getAttribute("errorMessage"),
                "Нельзя удалить место у человека: связь не найдена.");
    }

    @Test
    public void craftedRelationRequestsReturnDedicatedErrors() {
        PersonDAO personDAO = mock(PersonDAO.class);
        PlaceDAO placeDAO = mock(PlaceDAO.class);
        PersonPlaceDAO personPlaceDAO = mock(PersonPlaceDAO.class);
        RelationDAO relationDAO = mock(RelationDAO.class);
        PersonController controller = new PersonController(personDAO, placeDAO, personPlaceDAO, relationDAO);

        Person current = new Person(6L, "Игорь", "Мужской", 1938, null, "desc");
        Person other = new Person(9L, "Наталья", "Женский", 1972, null, "desc");
        when(personDAO.getById(6L)).thenReturn(current);
        when(personDAO.getById(9L)).thenReturn(other);

        ExtendedModelMap samePersonModel = new ExtendedModelMap();
        assertEquals(controller.saveRelationPage(6L, null, 6L, "PARTNER", 2000, null, samePersonModel), "error");
        assertEquals(samePersonModel.getAttribute("errorMessage"),
                "Нельзя создать связь человека с самим собой.");

        ExtendedModelMap missingRelationModel = new ExtendedModelMap();
        assertEquals(controller.saveRelationPage(6L, 9999L, 9L, "PARTNER", 2000, null, missingRelationModel), "error");
        assertEquals(missingRelationModel.getAttribute("errorMessage"),
                "Нельзя сохранить изменения: связь с ID = 9999 не найдена.");

        ExtendedModelMap missingCurrentPersonModel = new ExtendedModelMap();
        assertEquals(controller.saveRelationPage(9999L, null, 9L, "PARTNER", 2000, null, missingCurrentPersonModel), "error");
        assertEquals(missingCurrentPersonModel.getAttribute("errorMessage"),
                "Нельзя сохранить связь: человек с ID = 9999 не найден.");

        ExtendedModelMap missingRelatedPersonModel = new ExtendedModelMap();
        assertEquals(controller.saveRelationPage(6L, null, 9999L, "PARTNER", 2000, null, missingRelatedPersonModel), "error");
        assertEquals(missingRelatedPersonModel.getAttribute("errorMessage"),
                "Нельзя сохранить связь: второй человек с ID = 9999 не найден.");

        ExtendedModelMap unknownTypeModel = new ExtendedModelMap();
        assertEquals(controller.saveRelationPage(6L, null, 9L, "UNKNOWN_KIND", 2000, null, unknownTypeModel), "error");
        assertEquals(unknownTypeModel.getAttribute("errorMessage"),
                "Нельзя сохранить связь: указан неизвестный тип связи.");

        ExtendedModelMap missingRelationDeleteModel = new ExtendedModelMap();
        assertEquals(controller.removeRelationPage(6L, 9999L, missingRelationDeleteModel), "error");
        assertEquals(missingRelationDeleteModel.getAttribute("errorMessage"),
                "Нельзя удалить связь: запись не найдена.");
    }

    @Test
    public void privateHelpersCoverRelationKindsAndBlankNormalization() {
        PersonController controller = new PersonController(mock(PersonDAO.class), mock(PlaceDAO.class),
                mock(PersonPlaceDAO.class), mock(RelationDAO.class));
        Person current = new Person(1L, "Текущий", "Мужской", 1980, null, "desc");
        Person other = new Person(2L, "Другой", "Женский", 1982, null, "desc");

        Relation partnerRelation = new Relation(7L, current, other, null, null, RelationType.PARTNER);
        PersonController.RelationKindOption partnerKind = ReflectionTestUtils.invokeMethod(
                controller, "relationKindForPerson", current, partnerRelation);
        assertEquals(partnerKind.getCode(), "PARTNER");

        Relation adoptedRelation = new Relation(1L, other, current, null, null, RelationType.ADOPTED_CHILD);
        PersonController.RelationKindOption adoptedChild = ReflectionTestUtils.invokeMethod(
                controller, "relationKindForPerson", current, adoptedRelation);
        assertEquals(adoptedChild.getCode(), "ADOPTED_CHILD");

        Relation adoptedAsTarget = new Relation(8L, current, other, null, null, RelationType.ADOPTED_CHILD);
        PersonController.RelationKindOption adoptiveParent = ReflectionTestUtils.invokeMethod(
                controller, "relationKindForPerson", current, adoptedAsTarget);
        assertEquals(adoptiveParent.getCode(), "ADOPTIVE_PARENT");

        assertEquals(ReflectionTestUtils.invokeMethod(controller, "relationTypeLabel", partnerRelation, current), "Супруг(а)");
        assertEquals(ReflectionTestUtils.invokeMethod(controller, "relatedPersonForView", partnerRelation, current), other);

        Relation bastardAsTarget = new Relation(2L, current, other, null, null, RelationType.BASTARD_CHILD);
        PersonController.RelationKindOption parentOutOfWedlock = ReflectionTestUtils.invokeMethod(
                controller, "relationKindForPerson", current, bastardAsTarget);
        assertEquals(parentOutOfWedlock.getCode(), "PARENT_OUT_OF_WEDLOCK");

        Relation bastardAsRelated = new Relation(3L, other, current, null, null, RelationType.BASTARD_CHILD);
        PersonController.RelationKindOption bastardChild = ReflectionTestUtils.invokeMethod(
                controller, "relationKindForPerson", current, bastardAsRelated);
        assertEquals(bastardChild.getCode(), "BASTARD_CHILD");

        Relation relation = new Relation();
        for (String code : List.of("PARTNER", "PARENT", "ADOPTIVE_PARENT",
                "PARENT_OUT_OF_WEDLOCK", "CHILD", "ADOPTED_CHILD", "BASTARD_CHILD")) {
            PersonController.RelationKindOption option = new PersonController.RelationKindOption(code, code);
            ReflectionTestUtils.invokeMethod(controller, "applyRelationKind", relation, current, other, option);
            assertTrue(relation.getTargetPerson() != null);
            assertTrue(relation.getRelatedPerson() != null);
            assertTrue(relation.getRelationshipType() != null);
        }

        PersonController.RelationFormView emptyForm = new PersonController.RelationFormView();
        assertEquals(emptyForm.getRelationKind(), "");
        assertNull(emptyForm.getRelationId());
        assertNull(emptyForm.getRelatedPersonId());
        assertNull(emptyForm.getBeginYear());
        assertNull(emptyForm.getEndYear());

        PersonController.RelationKindOption option = new PersonController.RelationKindOption("CODE", "Label");
        assertEquals(option.getLabel(), "Label");

        PersonController.RelationView relationView = new PersonController.RelationView(5L, "Супруг(а)", other, 1990, 2000);
        assertEquals(relationView.getId(), Long.valueOf(5L));
        assertEquals(relationView.getLabel(), "Супруг(а)");
        assertEquals(relationView.getPerson(), other);
        assertEquals(relationView.getBeginYear(), Integer.valueOf(1990));
        assertEquals(relationView.getEndYear(), Integer.valueOf(2000));

        Place place = new Place(9L, "Москва", "desc");
        PersonController.PersonPlaceView placeView = new PersonController.PersonPlaceView(9L, place);
        assertEquals(placeView.getPlaceId(), Long.valueOf(9L));
        assertEquals(placeView.getPlace(), place);

        IllegalArgumentException exception = expectThrows(IllegalArgumentException.class, () ->
                ReflectionTestUtils.invokeMethod(controller, "applyRelationKind", relation, current, other,
                        new PersonController.RelationKindOption("UNKNOWN", "unknown")));
        assertTrue(exception.getMessage().contains("Unsupported relation kind"));

        assertNull(ReflectionTestUtils.invokeMethod(controller, "blankToNull", (Object) null));
        assertNull(ReflectionTestUtils.invokeMethod(controller, "blankToNull", "   "));
        assertEquals(ReflectionTestUtils.invokeMethod(controller, "blankToNull", "  text  "), "text");
    }
}
