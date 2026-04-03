package ru.cmc.msu.genealogy.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.cmc.msu.genealogy.dao.PersonDAO;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.services.GenealogyTreeService;

@Controller
@RequestMapping
public class HomeController {

    private final PersonDAO personDAO;
    private final GenealogyTreeService genealogyTreeService;

    public HomeController(PersonDAO personDAO, GenealogyTreeService genealogyTreeService) {
        this.personDAO = personDAO;
        this.genealogyTreeService = genealogyTreeService;
    }

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        model.addAttribute("personCount", personDAO.getAll().size());
        return "index";
    }

    @GetMapping("/generateTree")
    public String generateTree(Model model) {
        model.addAttribute("people", personDAO.getAllOrderedByName());
        return "generateTree";
    }

    @GetMapping("/tree")
    public String tree(@RequestParam(name = "primaryPersonId", required = false) Long primaryPersonId,
                       @RequestParam(name = "secondaryPersonId", required = false) Long secondaryPersonId,
                       @RequestParam(name = "direction", defaultValue = "ancestors") String direction,
                       @RequestParam(name = "depth", defaultValue = "2") Integer depth,
                       Model model) {
        if (depth == null || depth < 1 || depth > 10) {
            return buildError(model, "Глубина дерева должна быть числом от 1 до 10.");
        }

        Person primaryPerson = primaryPersonId == null ? null : personDAO.getById(primaryPersonId);
        Person secondaryPerson = secondaryPersonId == null ? null : personDAO.getById(secondaryPersonId);

        if (primaryPersonId != null && primaryPerson == null) {
            return buildError(model, "Не удалось построить дерево: человек с указанным ID не найден.");
        }
        if (secondaryPersonId != null && secondaryPerson == null) {
            return buildError(model, "Не удалось построить дерево: второй человек с указанным ID не найден.");
        }

        model.addAttribute("primaryPerson", primaryPerson);
        model.addAttribute("secondaryPerson", secondaryPerson);
        model.addAttribute("direction", direction);
        model.addAttribute("depth", depth);
        if (primaryPerson != null) {
            model.addAttribute("treeResult", genealogyTreeService.buildTree(primaryPerson, secondaryPerson, direction, depth));
        }
        return "tree";
    }

    @GetMapping("/error")
    public String error(@RequestParam(name = "message", required = false) String message, Model model) {
        model.addAttribute("errorMessage", message == null ? "Произошла ошибка при обработке запроса." : message);
        return "error";
    }

    private String buildError(Model model, String message) {
        model.addAttribute("errorMessage", message);
        return "error";
    }
}
