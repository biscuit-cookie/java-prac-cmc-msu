package ru.cmc.msu.genealogy.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ResponseBody;
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
        TreeRequestResolution resolution = resolveTreeRequest(primaryPersonId, secondaryPersonId, direction, depth);
        if (resolution.errorMessage() != null) {
            return buildError(model, resolution.errorMessage());
        }

        model.addAttribute("primaryPerson", resolution.primaryPerson());
        model.addAttribute("secondaryPerson", resolution.secondaryPerson());
        model.addAttribute("direction", direction);
        model.addAttribute("depth", resolution.depth());
        if (resolution.primaryPerson() != null) {
            model.addAttribute("treeResult", genealogyTreeService.buildTree(
                    resolution.primaryPerson(),
                    resolution.secondaryPerson(),
                    direction,
                    resolution.depth()
            ));
        }
        return "tree";
    }

    @GetMapping(value = "/tree/download", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> downloadTree(@RequestParam(name = "primaryPersonId", required = false) Long primaryPersonId,
                                          @RequestParam(name = "secondaryPersonId", required = false) Long secondaryPersonId,
                                          @RequestParam(name = "direction", defaultValue = "ancestors") String direction,
                                          @RequestParam(name = "depth", defaultValue = "2") Integer depth) {
        TreeRequestResolution resolution = resolveTreeRequest(primaryPersonId, secondaryPersonId, direction, depth);
        if (resolution.errorMessage() != null) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new DownloadErrorView(resolution.errorMessage()));
        }

        if (resolution.primaryPerson() == null) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new DownloadErrorView("Не удалось скачать дерево: основной человек не выбран."));
        }

        GenealogyTreeService.TreeResult treeResult = genealogyTreeService.buildTree(
                resolution.primaryPerson(),
                resolution.secondaryPerson(),
                direction,
                resolution.depth()
        );

        String filename = "genealogy-tree-person-" + resolution.primaryPerson().getId() + ".json";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(treeResult);
    }

    @GetMapping("/error")
    public String error(@RequestParam(name = "message", required = false) String message, Model model) {
        model.addAttribute("errorMessage", message == null ? "Произошла ошибка при обработке запроса." : message);
        return "error";
    }

    private TreeRequestResolution resolveTreeRequest(Long primaryPersonId, Long secondaryPersonId,
                                                     String direction, Integer depth) {
        if (depth == null || depth < 1 || depth > 10) {
            return new TreeRequestResolution(null, null, direction, depth, "Глубина дерева должна быть числом от 1 до 10.");
        }

        Person primaryPerson = primaryPersonId == null ? null : personDAO.getById(primaryPersonId);
        Person secondaryPerson = secondaryPersonId == null ? null : personDAO.getById(secondaryPersonId);

        if (primaryPersonId != null && primaryPerson == null) {
            return new TreeRequestResolution(null, null, direction, depth,
                    "Не удалось построить дерево: человек с указанным ID не найден.");
        }
        if (secondaryPersonId != null && secondaryPerson == null) {
            return new TreeRequestResolution(null, null, direction, depth,
                    "Не удалось построить дерево: второй человек с указанным ID не найден.");
        }

        return new TreeRequestResolution(primaryPerson, secondaryPerson, direction, depth, null);
    }

    private String buildError(Model model, String message) {
        model.addAttribute("errorMessage", message);
        return "error";
    }

    private record TreeRequestResolution(Person primaryPerson, Person secondaryPerson,
                                         String direction, Integer depth, String errorMessage) {

    }

    private record DownloadErrorView(String errorMessage) {

    }
}
