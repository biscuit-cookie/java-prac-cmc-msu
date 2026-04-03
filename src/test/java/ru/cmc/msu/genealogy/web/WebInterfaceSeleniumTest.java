package ru.cmc.msu.genealogy.web;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class WebInterfaceSeleniumTest extends AbstractWebSeleniumTest {

    @Test
    public void mainPageAndHeaderNavigationWork() {
        driver.get(baseUrl() + "/");
        assertEquals(driver.getTitle(), "Главная страница");

        driver.findElement(By.id("peopleListLink")).click();
        assertEquals(driver.getTitle(), "Люди");

        driver.findElement(By.id("placesListLink")).click();
        assertEquals(driver.getTitle(), "Места");

        driver.findElement(By.id("treeGeneratorLink")).click();
        assertEquals(driver.getTitle(), "Параметры дерева");

        driver.findElement(By.id("rootLink")).click();
        assertEquals(driver.getTitle(), "Главная страница");
    }

    @Test
    public void searchPersonFromMainPageOpensFilteredList() {
        driver.get(baseUrl() + "/");
        driver.findElement(By.id("homeSearch")).sendKeys("Игорь");
        submitContainingForm(driver.findElement(By.id("homeSearchButton")));

        assertEquals(driver.getTitle(), "Люди");

        WebElement personsTable = driver.findElement(By.id("personsTable"));
        List<WebElement> rows = personsTable.findElements(By.tagName("tr"));
        assertTrue(rows.stream().anyMatch(row -> row.getText().contains("Игорь Михайлович")));
    }

    @Test
    public void personPageShowsMainInformation() {
        driver.get(baseUrl() + "/person?personId=6");
        assertEquals(driver.getTitle(), "Информация о человеке");

        WebElement personInfo = driver.findElement(By.id("personInfo"));
        String infoText = personInfo.getText();
        assertTrue(infoText.contains("Игорь Михайлович"));
        assertTrue(infoText.contains("Мужской"));
        assertTrue(infoText.contains("1938"));

        WebElement personPlaces = driver.findElement(By.id("personPlaces"));
        String placesText = personPlaces.getText();
        assertTrue(placesText.contains("Москва"));
        assertTrue(placesText.contains("Казань"));

        WebElement personRelations = driver.findElement(By.id("personRelations"));
        String relationsText = personRelations.getText();
        assertTrue(relationsText.contains("Михаил Рюрикович"));
    }

    @Test
    public void addPlaceScenarioWorks() {
        driver.get(baseUrl() + "/places");
        assertEquals(driver.getTitle(), "Места");

        driver.findElement(By.id("addPlaceButton")).click();
        assertEquals(driver.getTitle(), "Добавить место");

        driver.findElement(By.id("placeName")).sendKeys("Тестовое место");
        driver.findElement(By.id("placeDescription")).sendKeys("Описание для системного теста.");
        submitContainingForm(driver.findElement(By.id("savePlaceButton")));

        assertEquals(driver.getTitle(), "Информация о месте");

        WebElement placeInfo = driver.findElement(By.id("placeInfo"));
        String placeText = placeInfo.getText();
        assertTrue(placeText.contains("Тестовое место"));
        assertTrue(placeText.contains("Описание для системного теста."));
    }

    @Test
    public void generateTreeScenarioOpensResultPage() {
        driver.get(baseUrl() + "/generateTree");
        assertEquals(driver.getTitle(), "Параметры дерева");

        new Select(driver.findElement(By.id("primaryPersonId"))).selectByValue("1");
        driver.findElement(By.id("depth")).clear();
        driver.findElement(By.id("depth")).sendKeys("3");
        submitContainingForm(driver.findElement(By.id("buildTreeButton")));

        assertEquals(driver.getTitle(), "Результат построения дерева");
        WebElement summary = driver.findElement(By.id("treeSummary"));
        assertTrue(summary.getText().contains("Глубина: 3"));
    }

    @Test
    public void treeShowsAncestorsForPersonWithParentsAndGrandparents() {
        driver.get(baseUrl() + "/tree?primaryPersonId=6&direction=ancestors&depth=2");
        assertEquals(driver.getTitle(), "Результат построения дерева");

        String pageText = driver.findElement(By.tagName("main")).getText();
        assertTrue(pageText.contains("Ветвь предков"));
        assertTrue(pageText.contains("Михаил Рюрикович"));
        assertTrue(pageText.contains("Елена Михайловна"));
        assertTrue(pageText.contains("Рюрик Старший"));
        assertTrue(pageText.contains("Анна Рюриковна"));
    }

    @Test
    public void treeShowsDescendantsForPersonWithChildrenAndGrandchildren() {
        driver.get(baseUrl() + "/tree?primaryPersonId=6&direction=descendants&depth=2");
        assertEquals(driver.getTitle(), "Результат построения дерева");

        String pageText = driver.findElement(By.tagName("main")).getText();
        assertTrue(pageText.contains("Ветвь потомков"));
        assertTrue(pageText.contains("Павел Игоревич"));
        assertTrue(pageText.contains("Наталья Игоревна"));
        assertTrue(pageText.contains("Алексей Павлович"));
    }

    @Test
    public void treeShowsMixedModeForPerson() {
        driver.get(baseUrl() + "/tree?primaryPersonId=6&direction=mixed&depth=1");
        assertEquals(driver.getTitle(), "Результат построения дерева");

        String pageText = driver.findElement(By.tagName("main")).getText();
        assertTrue(pageText.contains("Ветвь предков"));
        assertTrue(pageText.contains("Ветвь потомков"));
        assertTrue(pageText.contains("Михаил Рюрикович"));
        assertTrue(pageText.contains("Павел Игоревич"));
    }

    @Test
    public void treeShowsPartnersForPersonWhenTheyExist() {
        driver.get(baseUrl() + "/tree?primaryPersonId=3&direction=ancestors&depth=1");
        assertEquals(driver.getTitle(), "Результат построения дерева");

        String pageText = driver.findElement(By.tagName("main")).getText();
        assertTrue(pageText.contains("Супруги"));
        assertTrue(pageText.contains("Елена Михайловна"));
    }

    @Test
    public void treeShowsConnectionPathBetweenTwoPeople() {
        driver.get(baseUrl() + "/tree?primaryPersonId=10&secondaryPersonId=3&direction=mixed&depth=2");
        assertEquals(driver.getTitle(), "Результат построения дерева");

        String connectionText = driver.findElement(By.id("treeConnectionSection")).getText();
        assertTrue(connectionText.contains("Дерево связи между людьми"));
        assertTrue(connectionText.contains("Павел Игоревич"));
        assertTrue(connectionText.contains("Михаил Рюрикович"));
    }

    @Test
    public void treeShowsNoAncestorsMessageWhenTheyDoNotExist() {
        driver.get(baseUrl() + "/tree?primaryPersonId=1&direction=ancestors&depth=2");
        assertEquals(driver.getTitle(), "Результат построения дерева");
        assertTrue(driver.findElement(By.tagName("main")).getText()
                .contains("не найдено предков"));
    }

    @Test
    public void treeShowsNoDescendantsMessageWhenTheyDoNotExist() {
        driver.get(baseUrl() + "/tree?primaryPersonId=10&direction=descendants&depth=2");
        assertEquals(driver.getTitle(), "Результат построения дерева");
        assertTrue(driver.findElement(By.tagName("main")).getText()
                .contains("не найдено потомков"));
    }

    @Test
    public void treeShowsNoConnectionPathWhenPeopleAreDisconnected() {
        submitPostForm("/savePerson", new String[][]{
                {"name", "Изолированный человек"},
                {"gender", "Мужской"},
                {"birthYear", "2005"},
                {"characteristics", "Без родственных связей"}
        });
        String currentUrl = driver.getCurrentUrl();
        String isolatedPersonId = currentUrl.substring(currentUrl.indexOf("personId=") + "personId=".length());

        driver.get(baseUrl() + "/tree?primaryPersonId=" + isolatedPersonId + "&secondaryPersonId=1&direction=mixed&depth=2");
        assertEquals(driver.getTitle(), "Результат построения дерева");
        assertTrue(driver.findElement(By.tagName("main")).getText()
                .contains("не удалось найти цепочку связей"));
    }

    @Test
    public void generateTreeWithoutPrimaryPersonShowsInitialState() {
        driver.get(baseUrl() + "/generateTree");
        assertEquals(driver.getTitle(), "Параметры дерева");
        submitContainingForm(driver.findElement(By.id("buildTreeButton")));

        assertEquals(driver.getTitle(), "Результат построения дерева");
        assertTrue(driver.findElement(By.tagName("main")).getText()
                .contains("Параметры дерева еще не выбраны."));
    }

    @Test
    public void treeWithoutDirectionUsesDefaultAncestorsMode() {
        driver.get(baseUrl() + "/tree?primaryPersonId=6&depth=2");
        assertEquals(driver.getTitle(), "Результат построения дерева");
        String pageText = driver.findElement(By.tagName("main")).getText();
        assertTrue(pageText.contains("Направление: Предки"));
        assertTrue(pageText.contains("Ветвь предков"));
    }

    @Test
    public void treeWithoutDepthUsesDefaultDepth() {
        driver.get(baseUrl() + "/tree?primaryPersonId=6&direction=ancestors");
        assertEquals(driver.getTitle(), "Результат построения дерева");
        assertTrue(driver.findElement(By.id("treeSummary")).getText().contains("Глубина: 2"));
    }

    @Test
    public void personsListCanBeSortedByBirthYear() {
        driver.get(baseUrl() + "/persons?sort=birthDate");
        assertEquals(driver.getTitle(), "Люди");

        List<WebElement> rows = driver.findElement(By.id("personsTable")).findElements(By.tagName("tr"));
        assertTrue(rows.get(1).getText().contains("Рюрик Старший"));
        assertTrue(rows.get(2).getText().contains("Анна Рюриковна"));
    }

    @Test
    public void personsListCanBeSortedByName() {
        driver.get(baseUrl() + "/persons?sort=name");
        assertEquals(driver.getTitle(), "Люди");

        List<WebElement> rows = driver.findElement(By.id("personsTable")).findElements(By.tagName("tr"));
        assertTrue(rows.get(1).getText().contains("Алексей Павлович"));
        assertTrue(rows.get(2).getText().contains("Анна Рюриковна"));
    }

    @Test
    public void searchWithNoMatchesShowsEmptyState() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.id("search")).sendKeys("Несуществующий человек");
        submitContainingForm(driver.findElement(By.id("searchPersonButton")));

        WebElement personsTable = driver.findElement(By.id("personsTable"));
        assertTrue(personsTable.getText().contains("Подходящие люди не найдены."));
    }

    @Test
    public void placeSearchFindsExpectedPlace() {
        driver.get(baseUrl() + "/places");
        driver.findElement(By.id("placeSearch")).sendKeys("Казань");
        submitContainingForm(driver.findElement(By.id("searchPlaceButton")));

        String text = driver.findElement(By.id("placesList")).getText();
        assertTrue(text.contains("Казань"));
        assertFalse(text.contains("Москва"));
    }

    @Test
    public void placesListShowsAllSeededPlacesWithoutFilter() {
        driver.get(baseUrl() + "/places");
        assertEquals(driver.getTitle(), "Места");

        String text = driver.findElement(By.id("placesList")).getText();
        assertTrue(text.contains("Тверь"));
        assertTrue(text.contains("Москва"));
        assertTrue(text.contains("Санкт-Петербург"));
        assertTrue(text.contains("Казань"));
        assertTrue(text.contains("Новосибирск"));
    }

    @Test
    public void placeSearchWithNoMatchesShowsEmptyState() {
        driver.get(baseUrl() + "/places");
        driver.findElement(By.id("placeSearch")).sendKeys("Несуществующее место");
        submitContainingForm(driver.findElement(By.id("searchPlaceButton")));

        assertTrue(driver.findElement(By.id("placesList")).getText().contains("Подходящие места не найдены."));
    }

    @Test
    public void placePageShowsResidentsAndAllowsNavigationToPerson() {
        driver.get(baseUrl() + "/place?placeId=2");
        assertEquals(driver.getTitle(), "Информация о месте");

        WebElement residentsBlock = driver.findElement(By.tagName("main"));
        assertTrue(residentsBlock.getText().contains("Михаил Рюрикович"));
        assertTrue(residentsBlock.getText().contains("Игорь Михайлович"));

        driver.findElement(By.linkText("Игорь Михайлович")).click();
        assertEquals(driver.getTitle(), "Информация о человеке");
        assertTrue(driver.findElement(By.id("personInfo")).getText().contains("Игорь Михайлович"));
    }

    @Test
    public void addPersonScenarioWorks() {
        driver.get(baseUrl() + "/editPerson");
        assertEquals(driver.getTitle(), "Добавить человека");

        driver.findElement(By.id("personName")).sendKeys("Тестовый Потомок");
        new Select(driver.findElement(By.id("personGender"))).selectByVisibleText("Мужской");
        driver.findElement(By.id("birthYear")).sendKeys("2001");
        driver.findElement(By.id("characteristics")).sendKeys("Добавлен системным тестом.");
        submitContainingForm(driver.findElement(By.id("savePersonButton")));

        assertEquals(driver.getTitle(), "Информация о человеке");
        String infoText = driver.findElement(By.id("personInfo")).getText();
        assertTrue(infoText.contains("Тестовый Потомок"));
        assertTrue(infoText.contains("2001"));
        assertTrue(infoText.contains("Добавлен системным тестом."));
    }

    @Test
    public void editPersonScenarioWorks() {
        driver.get(baseUrl() + "/editPerson?personId=10");
        assertEquals(driver.getTitle(), "Редактировать человека");

        WebElement nameField = driver.findElement(By.id("personName"));
        nameField.clear();
        nameField.sendKeys("Алексей Павлович Обновленный");

        WebElement characteristics = driver.findElement(By.id("characteristics"));
        characteristics.clear();
        characteristics.sendKeys("Обновленная характеристика.");

        submitContainingForm(driver.findElement(By.id("savePersonButton")));

        assertEquals(driver.getTitle(), "Информация о человеке");
        String infoText = driver.findElement(By.id("personInfo")).getText();
        assertTrue(infoText.contains("Алексей Павлович Обновленный"));
        assertTrue(infoText.contains("Обновленная характеристика."));
    }

    @Test
    public void personPageEditLinkOpensEditForm() {
        driver.get(baseUrl() + "/person?personId=6");
        driver.findElement(By.id("editPersonLink")).click();

        assertEquals(driver.getTitle(), "Редактировать человека");
        assertEquals(driver.findElement(By.id("personName")).getAttribute("value"), "Игорь Михайлович");
    }

    @Test
    public void personPageTreeLinkOpensTreeResultForThatPerson() {
        driver.get(baseUrl() + "/person?personId=6");
        driver.findElement(By.id("personTreeLink")).click();

        assertEquals(driver.getTitle(), "Результат построения дерева");
        WebElement summary = driver.findElement(By.id("treeSummary"));
        assertTrue(summary.getText().contains("Игорь Михайлович"));
    }

    @Test
    public void editPlaceScenarioWorks() {
        driver.get(baseUrl() + "/editPlace?placeId=4");
        assertEquals(driver.getTitle(), "Редактировать место");

        WebElement description = driver.findElement(By.id("placeDescription"));
        description.clear();
        description.sendKeys("Обновленное описание Казани.");
        submitContainingForm(driver.findElement(By.id("savePlaceButton")));

        assertEquals(driver.getTitle(), "Информация о месте");
        assertTrue(driver.findElement(By.id("placeInfo")).getText().contains("Обновленное описание Казани."));
    }

    @Test
    public void placePageEditLinkOpensEditForm() {
        driver.get(baseUrl() + "/place?placeId=2");
        driver.findElement(By.id("editPlaceLink")).click();

        assertEquals(driver.getTitle(), "Редактировать место");
        assertEquals(driver.findElement(By.id("placeName")).getAttribute("value"), "Москва");
    }

    @Test
    public void deletePersonScenarioWorks() {
        driver.get(baseUrl() + "/person?personId=10");
        assertEquals(driver.getTitle(), "Информация о человеке");

        driver.findElement(By.id("removePersonButton")).click();
        assertEquals(driver.getTitle(), "Люди");
        assertFalse(driver.findElement(By.id("personsTable")).getText().contains("Алексей Павлович"));
    }

    @Test
    public void deletePlaceScenarioWorks() {
        driver.get(baseUrl() + "/place?placeId=5");
        assertEquals(driver.getTitle(), "Информация о месте");

        submitContainingForm(driver.findElement(By.id("removePlaceButton")));
        assertEquals(driver.getTitle(), "Места");
        assertFalse(driver.findElement(By.id("placesList")).getText().contains("Новосибирск"));
    }

    @Test
    public void addPersonPlaceScenarioWorks() {
        driver.get(baseUrl() + "/person?personId=10");
        driver.findElement(By.id("addPersonPlaceLink")).click();
        assertEquals(driver.getTitle(), "Добавить место человеку");

        new Select(driver.findElement(By.id("personPlaceId"))).selectByValue("2");
        submitContainingForm(driver.findElement(By.id("savePersonPlaceButton")));

        assertEquals(driver.getTitle(), "Информация о человеке");
        String placesText = driver.findElement(By.id("personPlaces")).getText();
        assertTrue(placesText.contains("Москва"));
        assertTrue(placesText.contains("Новосибирск"));
    }

    @Test
    public void editPersonPlaceScenarioWorks() {
        driver.get(baseUrl() + "/person?personId=6");
        driver.findElement(By.id("editPersonPlaceLink-4")).click();
        assertEquals(driver.getTitle(), "Редактировать место человека");

        new Select(driver.findElement(By.id("personPlaceId"))).selectByValue("5");
        submitContainingForm(driver.findElement(By.id("savePersonPlaceButton")));

        assertEquals(driver.getTitle(), "Информация о человеке");
        String placesText = driver.findElement(By.id("personPlaces")).getText();
        assertTrue(placesText.contains("Москва"));
        assertTrue(placesText.contains("Новосибирск"));
        assertFalse(placesText.contains("Казань"));
    }

    @Test
    public void deletePersonPlaceScenarioWorks() {
        driver.get(baseUrl() + "/person?personId=6");
        String beforeDelete = driver.findElement(By.id("personPlaces")).getText();
        assertTrue(beforeDelete.contains("Казань"));

        submitContainingForm(driver.findElement(By.id("removePersonPlaceButton-4")));

        assertEquals(driver.getTitle(), "Информация о человеке");
        String afterDelete = driver.findElement(By.id("personPlaces")).getText();
        assertFalse(afterDelete.contains("Казань"));
    }

    @Test
    public void personPlaceFormHtmlValidationPreventsEmptySubmit() {
        driver.get(baseUrl() + "/editPersonPlace?personId=6");
        driver.findElement(By.id("savePersonPlaceButton")).click();

        assertEquals(driver.getTitle(), "Добавить место человеку");
        String validationMessage = validationMessage(driver.findElement(By.id("personPlaceId")));
        assertFalse(validationMessage.isBlank());
    }

    @Test
    public void invalidEditPersonPlaceIdShowsErrorPage() {
        driver.get(baseUrl() + "/editPersonPlace?personId=6&placeId=9999");
        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("В базе нет места с ID = 9999"));
    }

    @Test
    public void craftedSavePersonPlaceWithMissingPersonShowsDedicatedError() {
        submitPostForm("/savePersonPlace", new String[][]{
                {"personId", "9999"},
                {"placeId", "2"}
        });

        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Нельзя сохранить место: человек с ID = 9999 не найден."));
    }

    @Test
    public void craftedSavePersonPlaceWithMissingPlaceShowsDedicatedError() {
        submitPostForm("/savePersonPlace", new String[][]{
                {"personId", "6"},
                {"placeId", "9999"}
        });

        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Нельзя сохранить место: место с ID = 9999 не найдено."));
    }

    @Test
    public void craftedSavePersonPlaceWithDuplicateLinkShowsDedicatedError() {
        submitPostForm("/savePersonPlace", new String[][]{
                {"personId", "6"},
                {"placeId", "2"}
        });

        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Нельзя добавить место: такая связь уже существует."));
    }

    @Test
    public void craftedSavePersonPlaceWithMissingOriginalLinkShowsDedicatedError() {
        submitPostForm("/savePersonPlace", new String[][]{
                {"personId", "6"},
                {"originalPlaceId", "5"},
                {"placeId", "3"}
        });

        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Нельзя сохранить изменения: исходная связь человека с местом не найдена."));
    }

    @Test
    public void craftedSavePersonPlaceWithDuplicateTargetPlaceShowsDedicatedError() {
        submitPostForm("/savePersonPlace", new String[][]{
                {"personId", "6"},
                {"originalPlaceId", "4"},
                {"placeId", "2"}
        });

        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Нельзя сохранить изменения: у человека уже есть связь с этим местом."));
    }

    @Test
    public void craftedRemovePersonPlaceWithMissingLinkShowsDedicatedError() {
        submitPostForm("/removePersonPlace", new String[][]{
                {"personId", "6"},
                {"placeId", "5"}
        });

        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Нельзя удалить место у человека: связь не найдена."));
    }

    @Test
    public void addRelationScenarioWorks() {
        driver.get(baseUrl() + "/person?personId=10");
        driver.findElement(By.id("addRelationLink")).click();
        assertEquals(driver.getTitle(), "Добавить связь");

        new Select(driver.findElement(By.id("relatedPersonId"))).selectByValue("9");
        new Select(driver.findElement(By.id("relationKind"))).selectByValue("PARTNER");
        driver.findElement(By.id("beginYear")).sendKeys("2020");
        submitContainingForm(driver.findElement(By.id("saveRelationButton")));

        assertEquals(driver.getTitle(), "Информация о человеке");
        String relationText = driver.findElement(By.id("personRelations")).getText();
        assertTrue(relationText.contains("Супруг(а)"));
        assertTrue(relationText.contains("Наталья Игоревна"));
        assertTrue(relationText.contains("2020"));
    }

    @Test
    public void editRelationScenarioWorks() {
        driver.get(baseUrl() + "/person?personId=6");
        driver.findElement(By.id("editRelationLink-7")).click();
        assertEquals(driver.getTitle(), "Редактировать связь");

        new Select(driver.findElement(By.id("relationKind"))).selectByValue("ADOPTIVE_PARENT");
        driver.findElement(By.id("endYear")).sendKeys("1988");
        submitContainingForm(driver.findElement(By.id("saveRelationButton")));

        assertEquals(driver.getTitle(), "Информация о человеке");
        String relationText = driver.findElement(By.id("personRelations")).getText();
        assertTrue(relationText.contains("Приемный родитель"));
        assertTrue(relationText.contains("Михаил Рюрикович"));
        assertTrue(relationText.contains("1988"));
    }

    @Test
    public void deleteRelationScenarioWorks() {
        driver.get(baseUrl() + "/person?personId=6");
        String beforeDelete = driver.findElement(By.id("personRelations")).getText();
        assertTrue(beforeDelete.contains("Елена Михайловна"));

        submitContainingForm(driver.findElement(By.id("removeRelationButton-8")));

        assertEquals(driver.getTitle(), "Информация о человеке");
        String afterDelete = driver.findElement(By.id("personRelations")).getText();
        assertFalse(afterDelete.contains("Елена Михайловна"));
    }

    @Test
    public void relationFormHtmlValidationPreventsEmptySubmit() {
        driver.get(baseUrl() + "/editRelation?personId=6");
        driver.findElement(By.id("saveRelationButton")).click();

        assertEquals(driver.getTitle(), "Добавить связь");
        String relatedValidation = validationMessage(driver.findElement(By.id("relatedPersonId")));
        String kindValidation = validationMessage(driver.findElement(By.id("relationKind")));
        assertTrue(!relatedValidation.isBlank() || !kindValidation.isBlank());
    }

    @Test
    public void relationCannotPointToSamePerson() {
        submitPostForm("/saveRelation", new String[][]{
                {"personId", "6"},
                {"relatedPersonId", "6"},
                {"relationKind", "PARTNER"},
                {"beginYear", "2000"}
        });

        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Нельзя создать связь человека с самим собой."));
    }

    @Test
    public void invalidRelationDatesShowServerSideError() {
        driver.get(baseUrl() + "/editRelation?personId=6");
        new Select(driver.findElement(By.id("relatedPersonId"))).selectByValue("9");
        new Select(driver.findElement(By.id("relationKind"))).selectByValue("PARTNER");
        driver.findElement(By.id("beginYear")).sendKeys("2020");
        driver.findElement(By.id("endYear")).sendKeys("2010");
        submitContainingForm(driver.findElement(By.id("saveRelationButton")));

        waitForTitle("Ошибка");
        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Год начала связи не может быть больше года окончания."));
    }

    @Test
    public void invalidEditRelationIdShowsErrorPage() {
        driver.get(baseUrl() + "/editRelation?personId=6&relationId=9999");
        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("В базе нет связи с ID = 9999"));
    }

    @Test
    public void craftedSaveRelationWithMissingRelationShowsDedicatedError() {
        submitPostForm("/saveRelation", new String[][]{
                {"personId", "6"},
                {"relationId", "9999"},
                {"relatedPersonId", "9"},
                {"relationKind", "PARTNER"},
                {"beginYear", "2000"}
        });

        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Нельзя сохранить изменения: связь с ID = 9999 не найдена."));
    }

    @Test
    public void craftedSaveRelationWithMissingCurrentPersonShowsDedicatedError() {
        submitPostForm("/saveRelation", new String[][]{
                {"personId", "9999"},
                {"relatedPersonId", "9"},
                {"relationKind", "PARTNER"},
                {"beginYear", "2000"}
        });

        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Нельзя сохранить связь: человек с ID = 9999 не найден."));
    }

    @Test
    public void craftedSaveRelationWithMissingRelatedPersonShowsDedicatedError() {
        submitPostForm("/saveRelation", new String[][]{
                {"personId", "6"},
                {"relatedPersonId", "9999"},
                {"relationKind", "PARTNER"},
                {"beginYear", "2000"}
        });

        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Нельзя сохранить связь: второй человек с ID = 9999 не найден."));
    }

    @Test
    public void craftedSaveRelationWithUnknownTypeShowsDedicatedError() {
        submitPostForm("/saveRelation", new String[][]{
                {"personId", "6"},
                {"relatedPersonId", "9"},
                {"relationKind", "UNKNOWN_KIND"},
                {"beginYear", "2000"}
        });

        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Нельзя сохранить связь: указан неизвестный тип связи."));
    }

    @Test
    public void craftedRemoveRelationWithMissingEntityShowsDedicatedError() {
        submitPostForm("/removeRelation", new String[][]{
                {"personId", "6"},
                {"relationId", "9999"}
        });

        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Нельзя удалить связь: запись не найдена."));
    }

    @Test
    public void invalidPersonIdShowsErrorPage() {
        driver.get(baseUrl() + "/person?personId=9999");
        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText().contains("В базе нет человека с ID = 9999"));
    }

    @Test
    public void invalidPlaceIdShowsErrorPage() {
        driver.get(baseUrl() + "/place?placeId=9999");
        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText().contains("В базе нет места с ID = 9999"));
    }

    @Test
    public void invalidEditPersonIdShowsErrorPage() {
        driver.get(baseUrl() + "/editPerson?personId=9999");
        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText().contains("В базе нет человека с ID = 9999"));
    }

    @Test
    public void invalidEditPlaceIdShowsErrorPage() {
        driver.get(baseUrl() + "/editPlace?placeId=9999");
        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText().contains("В базе нет места с ID = 9999"));
    }

    @Test
    public void invalidPersonDatesShowServerSideError() {
        driver.get(baseUrl() + "/editPerson");
        driver.findElement(By.id("personName")).sendKeys("Неверный человек");
        new Select(driver.findElement(By.id("personGender"))).selectByVisibleText("Женский");
        driver.findElement(By.id("birthYear")).sendKeys("2000");
        driver.findElement(By.id("deathYear")).sendKeys("1990");
        submitContainingForm(driver.findElement(By.id("savePersonButton")));

        waitForTitle("Ошибка");
        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Год рождения не может быть больше года смерти."));
    }

    @Test
    public void personFormHtmlValidationPreventsEmptySubmit() {
        driver.get(baseUrl() + "/editPerson");
        driver.findElement(By.id("savePersonButton")).click();

        assertEquals(driver.getTitle(), "Добавить человека");
        WebElement personName = driver.findElement(By.id("personName"));
        String validationMessage = validationMessage(personName);
        assertFalse(validationMessage.isBlank());
    }

    @Test
    public void placeFormHtmlValidationPreventsEmptySubmit() {
        driver.get(baseUrl() + "/editPlace");
        driver.findElement(By.id("savePlaceButton")).click();

        assertEquals(driver.getTitle(), "Добавить место");
        WebElement placeName = driver.findElement(By.id("placeName"));
        String validationMessage = validationMessage(placeName);
        assertFalse(validationMessage.isBlank());
    }

    @Test
    public void invalidTreePersonShowsErrorPage() {
        driver.get(baseUrl() + "/tree?primaryPersonId=9999");
        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Не удалось построить дерево: человек с указанным ID не найден."));
    }

    @Test
    public void invalidSecondTreePersonShowsErrorPage() {
        driver.get(baseUrl() + "/tree?primaryPersonId=1&secondaryPersonId=9999");
        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Не удалось построить дерево: второй человек с указанным ID не найден."));
    }

    @Test
    public void invalidTreeDepthTooSmallShowsErrorPage() {
        driver.get(baseUrl() + "/tree?primaryPersonId=1&depth=0");
        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Глубина дерева должна быть числом от 1 до 10."));
    }

    @Test
    public void invalidTreeDepthTooLargeShowsErrorPage() {
        driver.get(baseUrl() + "/tree?primaryPersonId=1&depth=11");
        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Глубина дерева должна быть числом от 1 до 10."));
    }

    @Test
    public void treePageWithoutParametersShowsInitialState() {
        driver.get(baseUrl() + "/tree");
        assertEquals(driver.getTitle(), "Результат построения дерева");
        assertTrue(driver.findElement(By.tagName("main")).getText().contains("Параметры дерева еще не выбраны."));
    }

    @Test
    public void craftedSavePersonWithMissingEntityShowsDedicatedError() {
        submitPostForm("/savePerson", new String[][]{
                {"personId", "9999"},
                {"name", "Призрак"},
                {"gender", "Мужской"},
                {"birthYear", "1950"},
                {"characteristics", "Несуществующая запись"}
        });

        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Нельзя сохранить изменения: человек с ID = 9999 не найден."));
    }

    @Test
    public void craftedRemovePersonWithMissingEntityShowsDedicatedError() {
        submitPostForm("/removePerson", new String[][]{
                {"personId", "9999"}
        });

        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Нельзя удалить человека: запись не найдена."));
    }

    @Test
    public void craftedSavePlaceWithMissingEntityShowsDedicatedError() {
        submitPostForm("/savePlace", new String[][]{
                {"placeId", "9999"},
                {"name", "Призрачное место"},
                {"description", "Описание отсутствующей записи"}
        });

        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Нельзя сохранить изменения: место с ID = 9999 не найдено."));
    }

    @Test
    public void craftedRemovePlaceWithMissingEntityShowsDedicatedError() {
        submitPostForm("/removePlace", new String[][]{
                {"placeId", "9999"}
        });

        assertEquals(driver.getTitle(), "Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Нельзя удалить место: запись не найдена."));
    }

    private void submitPostForm(String path, String[][] fields) {
        String script = ""
                + "const form = document.createElement('form');"
                + "form.method = 'post';"
                + "form.action = arguments[0];"
                + "for (const field of arguments[1]) {"
                + "  const input = document.createElement('input');"
                + "  input.type = 'hidden';"
                + "  input.name = field[0];"
                + "  input.value = field[1];"
                + "  form.appendChild(input);"
                + "}"
                + "document.body.appendChild(form);"
                + "form.submit();";
        driver.get(baseUrl() + "/");
        ((JavascriptExecutor) driver).executeScript(script, baseUrl() + path, fields);
    }

    private String validationMessage(WebElement element) {
        Object result = ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].validationMessage;", element);
        return result == null ? "" : result.toString();
    }

    private void submitContainingForm(WebElement elementInsideForm) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].closest('form').submit();", elementInsideForm);
    }

    private void waitForTitle(String expectedTitle) {
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            if (expectedTitle.equals(driver.getTitle())) {
                return;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
