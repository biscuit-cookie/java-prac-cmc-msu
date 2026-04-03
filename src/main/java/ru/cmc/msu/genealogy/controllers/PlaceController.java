package ru.cmc.msu.genealogy.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.cmc.msu.genealogy.dao.PersonPlaceDAO;
import ru.cmc.msu.genealogy.dao.PlaceDAO;
import ru.cmc.msu.genealogy.models.PersonPlace;
import ru.cmc.msu.genealogy.models.Place;

import java.util.List;
import java.util.Comparator;

@Controller
public class PlaceController {

    private final PlaceDAO placeDAO;
    private final PersonPlaceDAO personPlaceDAO;

    public PlaceController(PlaceDAO placeDAO, PersonPlaceDAO personPlaceDAO) {
        this.placeDAO = placeDAO;
        this.personPlaceDAO = personPlaceDAO;
    }

    @GetMapping("/places")
    public String placesListPage(@RequestParam(name = "search", required = false) String search, Model model) {
        List<Place> places = (search == null || search.isBlank())
                ? placeDAO.getAll()
                : placeDAO.findByName(search.trim());
        places = places.stream()
                .sorted(Comparator.comparing(Place::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        model.addAttribute("places", places);
        model.addAttribute("search", search == null ? "" : search);
        return "places";
    }

    @GetMapping("/place")
    public String placePage(@RequestParam(name = "placeId") Long placeId, Model model) {
        Place place = placeDAO.getById(placeId);
        if (place == null) {
            return buildError(model, "В базе нет места с ID = " + placeId);
        }

        List<PersonPlace> residents = personPlaceDAO.getByPlace(place);
        model.addAttribute("place", place);
        model.addAttribute("residents", residents);
        return "place";
    }

    @GetMapping("/editPlace")
    public String editPlacePage(@RequestParam(name = "placeId", required = false) Long placeId, Model model) {
        if (placeId == null) {
            model.addAttribute("place", new Place());
            return "editPlace";
        }

        Place place = placeDAO.getById(placeId);
        if (place == null) {
            return buildError(model, "В базе нет места с ID = " + placeId);
        }

        model.addAttribute("place", place);
        return "editPlace";
    }

    @PostMapping("/savePlace")
    public String savePlacePage(@RequestParam(name = "placeId", required = false) Long placeId,
                                @RequestParam(name = "name") String name,
                                @RequestParam(name = "description") String description,
                                Model model) {
        if (name.isBlank() || description.isBlank()) {
            return buildError(model, "Название и описание места обязательны для заполнения.");
        }

        Place place;
        if (placeId == null) {
            place = new Place(name.trim(), description.trim());
            placeDAO.save(place);
        } else {
            place = placeDAO.getById(placeId);
            if (place == null) {
                return buildError(model, "Нельзя сохранить изменения: место с ID = " + placeId + " не найдено.");
            }
            place.setName(name.trim());
            place.setDescription(description.trim());
            placeDAO.update(place);
        }

        return "redirect:/place?placeId=" + place.getId();
    }

    @PostMapping("/removePlace")
    public String removePlacePage(@RequestParam(name = "placeId") Long placeId, Model model) {
        Place place = placeDAO.getById(placeId);
        if (place == null) {
            return buildError(model, "Нельзя удалить место: запись не найдена.");
        }

        placeDAO.delete(place);
        return "redirect:/places";
    }

    private String buildError(Model model, String message) {
        model.addAttribute("errorMessage", message);
        return "error";
    }
}
