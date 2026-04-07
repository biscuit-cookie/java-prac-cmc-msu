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
        assertTitleEventually("Главная страница");

        driver.findElement(By.id("peopleListLink")).click();
        assertTitleEventually("Люди");

        driver.findElement(By.id("placesListLink")).click();
        assertTitleEventually("Места");

        driver.findElement(By.id("treeGeneratorLink")).click();
        assertTitleEventually("Параметры дерева");

        driver.findElement(By.id("rootLink")).click();
        assertTitleEventually("Главная страница");
    }

    @Test
    public void mainPageActionButtonsOpenTheirPages() {
        driver.get(baseUrl() + "/");
        assertTitleEventually("Главная страница");

        driver.findElement(By.id("allPersonsButton")).click();
        assertTitleEventually("Люди");

        driver.get(baseUrl() + "/");
        driver.findElement(By.id("allPlacesButton")).click();
        assertTitleEventually("Места");

        driver.get(baseUrl() + "/");
        driver.findElement(By.id("addPersonButton")).click();
        assertTitleEventually("Добавить человека");

        driver.get(baseUrl() + "/");
        driver.findElement(By.id("generateTreeButton")).click();
        assertTitleEventually("Параметры дерева");
    }

    @Test
    public void searchPersonFromMainPageOpensFilteredList() {
        driver.get(baseUrl() + "/");
        driver.findElement(By.id("homeSearch")).sendKeys("Игорь");
        driver.findElement(By.id("homeSearchButton")).click();

        assertTitleEventually("Люди");

        WebElement personsTable = driver.findElement(By.id("personsTable"));
        List<WebElement> rows = personsTable.findElements(By.tagName("tr"));
        assertTrue(rows.stream().anyMatch(row -> row.getText().contains("Игорь Михайлович")));
    }

    @Test
    public void personPageShowsMainInformation() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Игорь Михайлович")).click();
        assertTitleEventually("Информация о человеке");

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
    public void personPagePlaceLinkOpensPlaceCard() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Игорь Михайлович")).click();
        driver.findElement(By.id("personPlaceLink-2")).click();

        assertTitleEventually("Информация о месте");
        assertTrue(driver.findElement(By.id("placeInfo")).getText().contains("Москва"));
    }

    @Test
    public void personPageRelationLinkOpensRelatedPersonCard() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Игорь Михайлович")).click();
        driver.findElement(By.id("relationPersonLink-7")).click();

        assertTitleEventually("Информация о человеке");
        assertTrue(driver.findElement(By.id("personInfo")).getText().contains("Михаил Рюрикович"));
    }

    @Test
    public void addPlaceScenarioWorks() {
        driver.get(baseUrl() + "/places");
        assertTitleEventually("Места");

        driver.findElement(By.id("addPlaceButton")).click();
        assertTitleEventually("Добавить место");

        driver.findElement(By.id("placeName")).sendKeys("Тестовое место");
        driver.findElement(By.id("placeDescription")).sendKeys("Описание для системного теста.");
        driver.findElement(By.id("savePlaceButton")).click();
        waitForTitle("Информация о месте");

        assertTitleEventually("Информация о месте");

        WebElement placeInfo = driver.findElement(By.id("placeInfo"));
        String placeText = placeInfo.getText();
        assertTrue(placeText.contains("Тестовое место"));
        assertTrue(placeText.contains("Описание для системного теста."));
    }

    @Test
    public void generateTreeScenarioOpensResultPage() {
        openTreePage("1", null, "ancestors", "3");

        assertTitleEventually("Результат построения дерева");
        WebElement summary = driver.findElement(By.id("treeSummary"));
        assertTrue(summary.getText().contains("Глубина: 3"));
    }

    @Test
    public void treeShowsAncestorsForPersonWithParentsAndGrandparents() {
        openTreePage("6", null, "ancestors", "2");
        assertTitleEventually("Результат построения дерева");

        String pageText = driver.findElement(By.tagName("main")).getText();
        assertTrue(pageText.contains("Ветвь предков"));
        assertTrue(pageText.contains("Михаил Рюрикович"));
        assertTrue(pageText.contains("Елена Михайловна"));
        assertTrue(pageText.contains("Рюрик Старший"));
        assertTrue(pageText.contains("Анна Рюриковна"));
    }

    @Test
    public void treeShowsDescendantsForPersonWithChildrenAndGrandchildren() {
        openTreePage("6", null, "descendants", "2");
        assertTitleEventually("Результат построения дерева");

        String pageText = driver.findElement(By.tagName("main")).getText();
        assertTrue(pageText.contains("Ветвь потомков"));
        assertTrue(pageText.contains("Павел Игоревич"));
        assertTrue(pageText.contains("Наталья Игоревна"));
        assertTrue(pageText.contains("Алексей Павлович"));
    }

    @Test
    public void treeShowsMixedModeForPerson() {
        openTreePage("6", null, "mixed", "1");
        assertTitleEventually("Результат построения дерева");

        String pageText = driver.findElement(By.tagName("main")).getText();
        assertTrue(pageText.contains("Ветвь предков"));
        assertTrue(pageText.contains("Ветвь потомков"));
        assertTrue(pageText.contains("Михаил Рюрикович"));
        assertTrue(pageText.contains("Павел Игоревич"));
    }

    @Test
    public void treeShowsPartnersForPersonWhenTheyExist() {
        openTreePage("3", null, "ancestors", "1");
        assertTitleEventually("Результат построения дерева");

        String pageText = driver.findElement(By.tagName("main")).getText();
        assertTrue(pageText.contains("Супруги"));
        assertTrue(pageText.contains("Елена Михайловна"));
    }

    @Test
    public void treeShowsConnectionPathBetweenTwoPeople() {
        openTreePage("10", "3", "mixed", "2");
        assertTitleEventually("Результат построения дерева");

        String connectionText = driver.findElement(By.id("treeConnectionSection")).getText();
        assertTrue(connectionText.contains("Дерево связи между людьми"));
        assertTrue(connectionText.contains("Павел Игоревич"));
        assertTrue(connectionText.contains("Михаил Рюрикович"));
    }

    @Test
    public void treeShowsNoAncestorsMessageWhenTheyDoNotExist() {
        openTreePage("1", null, "ancestors", "2");
        assertTitleEventually("Результат построения дерева");
        assertTrue(driver.findElement(By.tagName("main")).getText()
                .contains("не найдено предков"));
    }

    @Test
    public void treeShowsNoDescendantsMessageWhenTheyDoNotExist() {
        openTreePage("10", null, "descendants", "2");
        assertTitleEventually("Результат построения дерева");
        assertTrue(driver.findElement(By.tagName("main")).getText()
                .contains("не найдено потомков"));
    }

    @Test
    public void treeShowsNoConnectionPathWhenPeopleAreDisconnected() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.id("addPersonFromListButton")).click();
        driver.findElement(By.id("personName")).sendKeys("Изолированный человек");
        new Select(driver.findElement(By.id("personGender"))).selectByVisibleText("Мужской");
        driver.findElement(By.id("birthYear")).sendKeys("2005");
        driver.findElement(By.id("characteristics")).sendKeys("Без родственных связей");
        driver.findElement(By.id("savePersonButton")).click();
        waitForTitle("Информация о человеке");
        String currentUrl = driver.getCurrentUrl();
        String isolatedPersonId = currentUrl.substring(currentUrl.indexOf("personId=") + "personId=".length());

        openTreePage(isolatedPersonId, "1", "mixed", "2");
        assertTitleEventually("Результат построения дерева");
        assertTrue(driver.findElement(By.tagName("main")).getText()
                .contains("не удалось найти цепочку связей"));
    }

    @Test
    public void generateTreeWithoutPrimaryPersonShowsInitialState() {
        driver.get(baseUrl() + "/generateTree");
        assertTitleEventually("Параметры дерева");
        driver.findElement(By.id("buildTreeButton")).click();

        assertTitleEventually("Результат построения дерева");
        assertTrue(driver.findElement(By.tagName("main")).getText()
                .contains("Параметры дерева еще не выбраны."));
    }

    @Test
    public void emptyTreePageButtonReturnsToParameterForm() {
        driver.get(baseUrl() + "/tree");
        assertTitleEventually("Результат построения дерева");

        driver.findElement(By.linkText("Задать параметры")).click();
        assertTitleEventually("Параметры дерева");
    }

    @Test
    public void personsListCanBeSortedByBirthYear() {
        driver.get(baseUrl() + "/persons");
        assertTitleEventually("Люди");
        new Select(driver.findElement(By.id("sort"))).selectByValue("birthDate");
        driver.findElement(By.id("searchPersonButton")).click();

        List<WebElement> rows = driver.findElement(By.id("personsTable")).findElements(By.tagName("tr"));
        assertTrue(rows.get(1).getText().contains("Рюрик Старший"));
        assertTrue(rows.get(2).getText().contains("Анна Рюриковна"));
    }

    @Test
    public void personsListCanBeSortedByName() {
        driver.get(baseUrl() + "/persons");
        assertTitleEventually("Люди");
        new Select(driver.findElement(By.id("sort"))).selectByValue("name");
        driver.findElement(By.id("searchPersonButton")).click();

        List<WebElement> rows = driver.findElement(By.id("personsTable")).findElements(By.tagName("tr"));
        assertTrue(rows.get(1).getText().contains("Алексей Павлович"));
        assertTrue(rows.get(2).getText().contains("Анна Рюриковна"));
    }

    @Test
    public void searchWithNoMatchesShowsEmptyState() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.id("search")).sendKeys("Несуществующий человек");
        driver.findElement(By.id("searchPersonButton")).click();

        WebElement personsTable = driver.findElement(By.id("personsTable"));
        assertTrue(personsTable.getText().contains("Подходящие люди не найдены."));
    }

    @Test
    public void placeSearchFindsExpectedPlace() {
        driver.get(baseUrl() + "/places");
        driver.findElement(By.id("placeSearch")).sendKeys("Казань");
        driver.findElement(By.id("searchPlaceButton")).click();

        String text = driver.findElement(By.id("placesList")).getText();
        assertTrue(text.contains("Казань"));
        assertFalse(text.contains("Москва"));
    }

    @Test
    public void placesListShowsAllSeededPlacesWithoutFilter() {
        driver.get(baseUrl() + "/places");
        assertTitleEventually("Места");

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
        driver.findElement(By.id("searchPlaceButton")).click();

        assertTrue(driver.findElement(By.id("placesList")).getText().contains("Подходящие места не найдены."));
    }

    @Test
    public void placePageShowsResidentsAndAllowsNavigationToPerson() {
        driver.get(baseUrl() + "/places");
        openPlaceFromList("Москва");
        assertTitleEventually("Информация о месте");

        WebElement residentsBlock = driver.findElement(By.tagName("main"));
        assertTrue(residentsBlock.getText().contains("Михаил Рюрикович"));
        assertTrue(residentsBlock.getText().contains("Игорь Михайлович"));

        driver.findElement(By.linkText("Игорь Михайлович")).click();
        assertTitleEventually("Информация о человеке");
        assertTrue(driver.findElement(By.id("personInfo")).getText().contains("Игорь Михайлович"));
    }

    @Test
    public void addPersonScenarioWorks() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.id("addPersonFromListButton")).click();
        assertTitleEventually("Добавить человека");

        driver.findElement(By.id("personName")).sendKeys("Тестовый Потомок");
        new Select(driver.findElement(By.id("personGender"))).selectByVisibleText("Мужской");
        driver.findElement(By.id("birthYear")).sendKeys("2001");
        driver.findElement(By.id("characteristics")).sendKeys("Добавлен системным тестом.");
        driver.findElement(By.id("savePersonButton")).click();
        waitForTitle("Информация о человеке");

        assertTitleEventually("Информация о человеке");
        String infoText = driver.findElement(By.id("personInfo")).getText();
        assertTrue(infoText.contains("Тестовый Потомок"));
        assertTrue(infoText.contains("2001"));
        assertTrue(infoText.contains("Добавлен системным тестом."));
    }

    @Test
    public void editPersonScenarioWorks() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Алексей Павлович")).click();
        driver.findElement(By.id("editPersonLink")).click();
        assertTitleEventually("Редактировать человека");

        WebElement nameField = driver.findElement(By.id("personName"));
        nameField.clear();
        nameField.sendKeys("Алексей Павлович Обновленный");

        WebElement characteristics = driver.findElement(By.id("characteristics"));
        characteristics.clear();
        characteristics.sendKeys("Обновленная характеристика.");

        driver.findElement(By.id("savePersonButton")).click();
        waitForTitle("Информация о человеке");

        assertTitleEventually("Информация о человеке");
        String infoText = driver.findElement(By.id("personInfo")).getText();
        assertTrue(infoText.contains("Алексей Павлович Обновленный"));
        assertTrue(infoText.contains("Обновленная характеристика."));
    }

    @Test
    public void personPageEditLinkOpensEditForm() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Игорь Михайлович")).click();
        driver.findElement(By.id("editPersonLink")).click();

        assertTitleEventually("Редактировать человека");
        assertEquals(driver.findElement(By.id("personName")).getAttribute("value"), "Игорь Михайлович");
    }

    @Test
    public void personPageTreeLinkOpensTreeResultForThatPerson() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Игорь Михайлович")).click();
        driver.findElement(By.id("personTreeLink")).click();

        assertTitleEventually("Результат построения дерева");
        WebElement summary = driver.findElement(By.id("treeSummary"));
        assertTrue(summary.getText().contains("Игорь Михайлович"));
    }

    @Test
    public void treePageAllowsNavigationToPersonCard() {
        openTreePage("6", null, "ancestors", "2");
        driver.findElement(By.linkText("Михаил Рюрикович")).click();

        assertTitleEventually("Информация о человеке");
        assertTrue(driver.findElement(By.id("personInfo")).getText().contains("Михаил Рюрикович"));
    }

    @Test
    public void treePageShowsDownloadLinkForJsonExport() {
        openTreePage("6", null, "ancestors", "2");

        WebElement downloadLink = driver.findElement(By.linkText("Скачать дерево в JSON"));
        String href = downloadLink.getAttribute("href");
        assertTrue(href.contains("/tree/download"));
        assertTrue(href.contains("primaryPersonId=6"));
        assertTrue(href.contains("direction=ancestors"));
        assertTrue(href.contains("depth=2"));
    }

    @Test
    public void editPlaceScenarioWorks() {
        driver.get(baseUrl() + "/places");
        openPlaceFromList("Казань");
        driver.findElement(By.id("editPlaceLink")).click();
        assertTitleEventually("Редактировать место");

        WebElement description = driver.findElement(By.id("placeDescription"));
        description.clear();
        description.sendKeys("Обновленное описание Казани.");
        driver.findElement(By.id("savePlaceButton")).click();
        waitForTitle("Информация о месте");

        assertTitleEventually("Информация о месте");
        assertTrue(driver.findElement(By.id("placeInfo")).getText().contains("Обновленное описание Казани."));
    }

    @Test
    public void placePageEditLinkOpensEditForm() {
        driver.get(baseUrl() + "/places");
        openPlaceFromList("Москва");
        driver.findElement(By.id("editPlaceLink")).click();

        assertTitleEventually("Редактировать место");
        assertEquals(driver.findElement(By.id("placeName")).getAttribute("value"), "Москва");
    }

    @Test
    public void addPersonFormCancelReturnsToPersonsList() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.id("addPersonFromListButton")).click();
        assertTitleEventually("Добавить человека");

        driver.findElement(By.linkText("Отмена")).click();
        assertTitleEventually("Люди");
    }

    @Test
    public void deletePersonScenarioWorks() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Алексей Павлович")).click();
        assertTitleEventually("Информация о человеке");

        driver.findElement(By.id("removePersonButton")).click();
        waitForTitle("Люди");
        assertTitleEventually("Люди");
        assertFalse(driver.findElement(By.id("personsTable")).getText().contains("Алексей Павлович"));
    }

    @Test
    public void deletePlaceScenarioWorks() {
        driver.get(baseUrl() + "/places");
        openPlaceFromList("Новосибирск");
        assertTitleEventually("Информация о месте");

        driver.findElement(By.id("removePlaceButton")).click();
        waitForTitle("Места");
        assertTitleEventually("Места");
        assertFalse(driver.findElement(By.id("placesList")).getText().contains("Новосибирск"));
    }

    @Test
    public void addPersonPlaceScenarioWorks() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Алексей Павлович")).click();
        driver.findElement(By.id("addPersonPlaceLink")).click();
        assertTitleEventually("Добавить место человеку");

        new Select(driver.findElement(By.id("personPlaceId"))).selectByValue("2");
        driver.findElement(By.id("savePersonPlaceButton")).click();
        waitForTitle("Информация о человеке");

        assertTitleEventually("Информация о человеке");
        String placesText = driver.findElement(By.id("personPlaces")).getText();
        assertTrue(placesText.contains("Москва"));
        assertTrue(placesText.contains("Новосибирск"));
    }

    @Test
    public void editPersonPlaceScenarioWorks() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Игорь Михайлович")).click();
        driver.findElement(By.id("editPersonPlaceLink-4")).click();
        assertTitleEventually("Редактировать место человека");

        new Select(driver.findElement(By.id("personPlaceId"))).selectByValue("5");
        driver.findElement(By.id("savePersonPlaceButton")).click();
        waitForTitle("Информация о человеке");

        assertTitleEventually("Информация о человеке");
        String placesText = driver.findElement(By.id("personPlaces")).getText();
        assertTrue(placesText.contains("Москва"));
        assertTrue(placesText.contains("Новосибирск"));
        assertFalse(placesText.contains("Казань"));
    }

    @Test
    public void deletePersonPlaceScenarioWorks() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Игорь Михайлович")).click();
        String beforeDelete = driver.findElement(By.id("personPlaces")).getText();
        assertTrue(beforeDelete.contains("Казань"));

        driver.findElement(By.id("removePersonPlaceButton-4")).click();
        waitForTitle("Информация о человеке");

        assertTitleEventually("Информация о человеке");
        String afterDelete = driver.findElement(By.id("personPlaces")).getText();
        assertFalse(afterDelete.contains("Казань"));
    }

    @Test
    public void personPlaceFormHtmlValidationPreventsEmptySubmit() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Игорь Михайлович")).click();
        driver.findElement(By.id("addPersonPlaceLink")).click();
        driver.findElement(By.id("savePersonPlaceButton")).click();

        assertTitleEventually("Добавить место человеку");
        String validationMessage = validationMessage(driver.findElement(By.id("personPlaceId")));
        assertFalse(validationMessage.isBlank());
    }

    @Test
    public void addRelationScenarioWorks() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Алексей Павлович")).click();
        driver.findElement(By.id("addRelationLink")).click();
        assertTitleEventually("Добавить связь");

        new Select(driver.findElement(By.id("relatedPersonId"))).selectByValue("9");
        new Select(driver.findElement(By.id("relationKind"))).selectByValue("PARTNER");
        driver.findElement(By.id("beginYear")).sendKeys("2020");
        driver.findElement(By.id("saveRelationButton")).click();
        waitForTitle("Информация о человеке");

        assertTitleEventually("Информация о человеке");
        String relationText = driver.findElement(By.id("personRelations")).getText();
        assertTrue(relationText.contains("Супруг(а)"));
        assertTrue(relationText.contains("Наталья Игоревна"));
        assertTrue(relationText.contains("2020"));
    }

    @Test
    public void editRelationScenarioWorks() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Игорь Михайлович")).click();
        driver.findElement(By.id("editRelationLink-7")).click();
        assertTitleEventually("Редактировать связь");

        new Select(driver.findElement(By.id("relationKind"))).selectByValue("ADOPTIVE_PARENT");
        WebElement endYearField = driver.findElement(By.id("endYear"));
        endYearField.clear();
        endYearField.sendKeys("1988");
        driver.findElement(By.id("saveRelationButton")).click();
        waitForTitle("Информация о человеке");

        assertTitleEventually("Информация о человеке");
        String relationText = driver.findElement(By.id("personRelations")).getText();
        assertTrue(relationText.contains("Приемный родитель"));
        assertTrue(relationText.contains("Михаил Рюрикович"));
        assertTrue(relationText.contains("1988"));
    }

    @Test
    public void deleteRelationScenarioWorks() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Игорь Михайлович")).click();
        String beforeDelete = driver.findElement(By.id("personRelations")).getText();
        assertTrue(beforeDelete.contains("Елена Михайловна"));

        driver.findElement(By.id("removeRelationButton-8")).click();
        waitForTitle("Информация о человеке");

        assertTitleEventually("Информация о человеке");
        String afterDelete = driver.findElement(By.id("personRelations")).getText();
        assertFalse(afterDelete.contains("Елена Михайловна"));
    }

    @Test
    public void relationFormHtmlValidationPreventsEmptySubmit() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Игорь Михайлович")).click();
        driver.findElement(By.id("addRelationLink")).click();
        driver.findElement(By.id("saveRelationButton")).click();

        assertTitleEventually("Добавить связь");
        String relatedValidation = validationMessage(driver.findElement(By.id("relatedPersonId")));
        String kindValidation = validationMessage(driver.findElement(By.id("relationKind")));
        assertTrue(!relatedValidation.isBlank() || !kindValidation.isBlank());
    }

    @Test
    public void editRelationFormCancelReturnsToPersonPage() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Игорь Михайлович")).click();
        driver.findElement(By.id("editRelationLink-7")).click();
        assertTitleEventually("Редактировать связь");

        driver.findElement(By.linkText("Отмена")).click();
        assertTitleEventually("Информация о человеке");
        assertTrue(driver.findElement(By.id("personInfo")).getText().contains("Игорь Михайлович"));
    }

    @Test
    public void invalidRelationDatesShowServerSideError() {
        driver.get(baseUrl() + "/persons");
        driver.findElement(By.linkText("Игорь Михайлович")).click();
        driver.findElement(By.id("addRelationLink")).click();
        new Select(driver.findElement(By.id("relatedPersonId"))).selectByValue("9");
        new Select(driver.findElement(By.id("relationKind"))).selectByValue("PARTNER");
        driver.findElement(By.id("beginYear")).sendKeys("2020");
        driver.findElement(By.id("endYear")).sendKeys("2010");
        driver.findElement(By.id("saveRelationButton")).click();

        waitForTitle("Ошибка");
        assertTitleEventually("Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Год начала связи не может быть больше года окончания."));
    }

    @Test
    public void invalidPersonDatesShowServerSideError() {
        driver.get(baseUrl() + "/editPerson");
        driver.findElement(By.id("personName")).sendKeys("Неверный человек");
        new Select(driver.findElement(By.id("personGender"))).selectByVisibleText("Женский");
        driver.findElement(By.id("birthYear")).sendKeys("2000");
        driver.findElement(By.id("deathYear")).sendKeys("1990");
        driver.findElement(By.id("savePersonButton")).click();

        waitForTitle("Ошибка");
        assertTitleEventually("Ошибка");
        assertTrue(driver.findElement(By.id("errorMessageBlock")).getText()
                .contains("Год рождения не может быть больше года смерти."));
    }

    @Test
    public void personFormHtmlValidationPreventsEmptySubmit() {
        driver.get(baseUrl() + "/editPerson");
        driver.findElement(By.id("savePersonButton")).click();

        assertTitleEventually("Добавить человека");
        WebElement personName = driver.findElement(By.id("personName"));
        String validationMessage = validationMessage(personName);
        assertFalse(validationMessage.isBlank());
    }

    @Test
    public void placeFormHtmlValidationPreventsEmptySubmit() {
        driver.get(baseUrl() + "/editPlace");
        driver.findElement(By.id("savePlaceButton")).click();

        assertTitleEventually("Добавить место");
        WebElement placeName = driver.findElement(By.id("placeName"));
        String validationMessage = validationMessage(placeName);
        assertFalse(validationMessage.isBlank());
    }

    private String validationMessage(WebElement element) {
        Object result = ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].validationMessage;", element);
        return result == null ? "" : result.toString();
    }

    private void assertTitleEventually(String expectedTitle) {
        waitForTitle(expectedTitle);
        assertEquals(driver.getTitle(), expectedTitle);
    }

    private void openTreePage(String primaryPersonId, String secondaryPersonId, String direction, String depth) {
        driver.get(baseUrl() + "/generateTree");
        assertTitleEventually("Параметры дерева");

        new Select(driver.findElement(By.id("primaryPersonId"))).selectByValue(primaryPersonId);
        if (secondaryPersonId != null) {
            new Select(driver.findElement(By.id("secondaryPersonId"))).selectByValue(secondaryPersonId);
        }
        new Select(driver.findElement(By.id("direction"))).selectByValue(direction);
        WebElement depthField = driver.findElement(By.id("depth"));
        depthField.clear();
        depthField.sendKeys(depth);
        driver.findElement(By.id("buildTreeButton")).click();
        assertTitleEventually("Результат построения дерева");
    }

    private void openPlaceFromList(String placeName) {
        driver.findElement(By.xpath("//div[@id='placesList']//a[.//div[@class='fw-semibold' and normalize-space()='" + placeName + "']]"))
                .click();
    }

    // У меня средний по мощности ноутбук, потому я буду чуток ждать, пока страница прогрузится. Логику тестов это не меняет,
    // просто зачастую тесты рандомно ложатся из-за того, что просто не успевает загрузиться другая страница
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
