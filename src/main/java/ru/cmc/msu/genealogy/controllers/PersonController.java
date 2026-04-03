package ru.cmc.msu.genealogy.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.cmc.msu.genealogy.dao.PersonDAO;
import ru.cmc.msu.genealogy.dao.PersonPlaceDAO;
import ru.cmc.msu.genealogy.dao.PlaceDAO;
import ru.cmc.msu.genealogy.dao.RelationDAO;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.PersonPlace;
import ru.cmc.msu.genealogy.models.PersonPlaceId;
import ru.cmc.msu.genealogy.models.Relation;
import ru.cmc.msu.genealogy.models.RelationType;
import ru.cmc.msu.genealogy.models.Place;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PersonController {

    private final PersonDAO personDAO;
    private final PlaceDAO placeDAO;
    private final PersonPlaceDAO personPlaceDAO;
    private final RelationDAO relationDAO;

    public PersonController(PersonDAO personDAO, PlaceDAO placeDAO, PersonPlaceDAO personPlaceDAO, RelationDAO relationDAO) {
        this.personDAO = personDAO;
        this.placeDAO = placeDAO;
        this.personPlaceDAO = personPlaceDAO;
        this.relationDAO = relationDAO;
    }

    @GetMapping("/persons")
    public String peopleListPage(@RequestParam(name = "search", required = false) String search,
                                 @RequestParam(name = "sort", defaultValue = "name") String sort,
                                 Model model) {
        List<Person> people;
        if (search != null && !search.isBlank()) {
            people = personDAO.findByName(search.trim());
        } else if ("birthDate".equals(sort)) {
            people = personDAO.getAllOrderedByBirthDate();
        } else {
            people = personDAO.getAllOrderedByName();
        }

        Map<Long, Integer> relationCounts = new LinkedHashMap<>();
        for (Person person : people) {
            relationCounts.put(person.getId(), relationDAO.getRelationsByPerson(person).size());
        }

        model.addAttribute("people", people);
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("sort", sort);
        model.addAttribute("relationCounts", relationCounts);
        return "persons";
    }

    @GetMapping("/person")
    public String personPage(@RequestParam(name = "personId") Long personId, Model model) {
        Person person = personDAO.getById(personId);
        if (person == null) {
            return buildError(model, "В базе нет человека с ID = " + personId);
        }

        model.addAttribute("person", person);
        model.addAttribute("personPlacesForView", buildPersonPlacesForView(personPlaceDAO.getByPerson(person)));
        model.addAttribute("relationsForView", buildRelationsForView(person, relationDAO.getRelationsByPerson(person)));
        return "person";
    }

    @GetMapping("/editPersonPlace")
    public String editPersonPlacePage(@RequestParam(name = "personId") Long personId,
                                      @RequestParam(name = "placeId", required = false) Long placeId,
                                      Model model) {
        Person person = personDAO.getById(personId);
        if (person == null) {
            return buildError(model, "В базе нет человека с ID = " + personId);
        }

        PersonPlaceFormView personPlaceForm = new PersonPlaceFormView();
        if (placeId != null) {
            Place place = placeDAO.getById(placeId);
            if (place == null) {
                return buildError(model, "В базе нет места с ID = " + placeId);
            }
            PersonPlace personPlace = personPlaceDAO.getById(new PersonPlaceId(placeId, personId));
            if (personPlace == null) {
                return buildError(model, "У человека с ID = " + personId + " нет связи с местом ID = " + placeId);
            }
            personPlaceForm = new PersonPlaceFormView(placeId, placeId);
        }

        List<Place> availablePlaces = placeDAO.getAll().stream()
                .sorted(Comparator.comparing(Place::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        model.addAttribute("person", person);
        model.addAttribute("personPlaceForm", personPlaceForm);
        model.addAttribute("availablePlaces", availablePlaces);
        return "editPersonPlace";
    }

    @GetMapping("/editRelation")
    public String editRelationPage(@RequestParam(name = "personId") Long personId,
                                   @RequestParam(name = "relationId", required = false) Long relationId,
                                   Model model) {
        Person person = personDAO.getById(personId);
        if (person == null) {
            return buildError(model, "В базе нет человека с ID = " + personId);
        }

        Relation relation = null;
        RelationFormView relationForm = new RelationFormView();
        if (relationId != null) {
            relation = relationDAO.getById(relationId);
            if (relation == null) {
                return buildError(model, "В базе нет связи с ID = " + relationId);
            }
            if (!person.equals(relation.getTargetPerson()) && !person.equals(relation.getRelatedPerson())) {
                return buildError(model, "Связь с ID = " + relationId + " не относится к человеку с ID = " + personId);
            }
            relationForm = buildRelationFormView(person, relation);
        }

        List<Person> availablePeople = personDAO.getAllOrderedByName().stream()
                .filter(candidate -> !candidate.getId().equals(personId))
                .sorted(Comparator.comparing(Person::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        model.addAttribute("person", person);
        model.addAttribute("relation", relation);
        model.addAttribute("relationForm", relationForm);
        model.addAttribute("availablePeople", availablePeople);
        model.addAttribute("relationKinds", relationKindOptions());
        return "editRelation";
    }

    @GetMapping("/editPerson")
    public String editPersonPage(@RequestParam(name = "personId", required = false) Long personId, Model model) {
        if (personId == null) {
            model.addAttribute("person", new Person());
            return "editPerson";
        }

        Person person = personDAO.getById(personId);
        if (person == null) {
            return buildError(model, "В базе нет человека с ID = " + personId);
        }

        model.addAttribute("person", person);
        return "editPerson";
    }

    @PostMapping("/savePerson")
    public String savePersonPage(@RequestParam(name = "personId", required = false) Long personId,
                                 @RequestParam(name = "name") String name,
                                 @RequestParam(name = "gender") String gender,
                                 @RequestParam(name = "birthYear", required = false) Integer birthYear,
                                 @RequestParam(name = "deathYear", required = false) Integer deathYear,
                                 @RequestParam(name = "characteristics", required = false) String characteristics,
                                 Model model) {
        if (name.isBlank() || gender.isBlank()) {
            return buildError(model, "Имя и пол обязательны для заполнения.");
        }
        if (birthYear != null && deathYear != null && birthYear > deathYear) {
            return buildError(model, "Год рождения не может быть больше года смерти.");
        }

        Person person;
        if (personId == null) {
            person = new Person(name.trim(), gender.trim());
            person.setDateOfBirth(birthYear);
            person.setDateOfDeath(deathYear);
            person.setCharacteristics(blankToNull(characteristics));
            personDAO.save(person);
        } else {
            person = personDAO.getById(personId);
            if (person == null) {
                return buildError(model, "Нельзя сохранить изменения: человек с ID = " + personId + " не найден.");
            }
            person.setName(name.trim());
            person.setGender(gender.trim());
            person.setDateOfBirth(birthYear);
            person.setDateOfDeath(deathYear);
            person.setCharacteristics(blankToNull(characteristics));
            personDAO.update(person);
        }

        return "redirect:/person?personId=" + person.getId();
    }

    @PostMapping("/removePerson")
    public String removePersonPage(@RequestParam(name = "personId") Long personId, Model model) {
        Person person = personDAO.getById(personId);
        if (person == null) {
            return buildError(model, "Нельзя удалить человека: запись не найдена.");
        }

        personDAO.delete(person);
        return "redirect:/persons";
    }

    @PostMapping("/savePersonPlace")
    public String savePersonPlacePage(@RequestParam(name = "personId") Long personId,
                                      @RequestParam(name = "originalPlaceId", required = false) Long originalPlaceId,
                                      @RequestParam(name = "placeId") Long placeId,
                                      Model model) {
        Person person = personDAO.getById(personId);
        if (person == null) {
            return buildError(model, "Нельзя сохранить место: человек с ID = " + personId + " не найден.");
        }

        Place place = placeDAO.getById(placeId);
        if (place == null) {
            return buildError(model, "Нельзя сохранить место: место с ID = " + placeId + " не найдено.");
        }

        PersonPlace existingLink = personPlaceDAO.getById(new PersonPlaceId(placeId, personId));
        if (originalPlaceId == null) {
            if (existingLink != null) {
                return buildError(model, "Нельзя добавить место: такая связь уже существует.");
            }
            personPlaceDAO.save(new PersonPlace(person, place));
        } else {
            PersonPlace originalLink = personPlaceDAO.getById(new PersonPlaceId(originalPlaceId, personId));
            if (originalLink == null) {
                return buildError(model, "Нельзя сохранить изменения: исходная связь человека с местом не найдена.");
            }
            if (!originalPlaceId.equals(placeId) && existingLink != null) {
                return buildError(model, "Нельзя сохранить изменения: у человека уже есть связь с этим местом.");
            }
            if (!originalPlaceId.equals(placeId)) {
                personPlaceDAO.delete(originalLink);
                personPlaceDAO.save(new PersonPlace(person, place));
            }
        }

        return "redirect:/person?personId=" + personId;
    }

    @PostMapping("/removePersonPlace")
    public String removePersonPlacePage(@RequestParam(name = "personId") Long personId,
                                        @RequestParam(name = "placeId") Long placeId,
                                        Model model) {
        PersonPlace personPlace = personPlaceDAO.getById(new PersonPlaceId(placeId, personId));
        if (personPlace == null) {
            return buildError(model, "Нельзя удалить место у человека: связь не найдена.");
        }

        personPlaceDAO.delete(personPlace);
        return "redirect:/person?personId=" + personId;
    }

    @PostMapping("/saveRelation")
    public String saveRelationPage(@RequestParam(name = "personId") Long personId,
                                   @RequestParam(name = "relationId", required = false) Long relationId,
                                   @RequestParam(name = "relatedPersonId") Long relatedPersonId,
                                   @RequestParam(name = "relationKind") String relationKind,
                                   @RequestParam(name = "beginYear", required = false) Integer beginYear,
                                   @RequestParam(name = "endYear", required = false) Integer endYear,
                                   Model model) {
        Person currentPerson = personDAO.getById(personId);
        if (currentPerson == null) {
            return buildError(model, "Нельзя сохранить связь: человек с ID = " + personId + " не найден.");
        }

        Person otherPerson = personDAO.getById(relatedPersonId);
        if (otherPerson == null) {
            return buildError(model, "Нельзя сохранить связь: второй человек с ID = " + relatedPersonId + " не найден.");
        }
        if (currentPerson.getId().equals(otherPerson.getId())) {
            return buildError(model, "Нельзя создать связь человека с самим собой.");
        }
        if (beginYear != null && endYear != null && beginYear > endYear) {
            return buildError(model, "Год начала связи не может быть больше года окончания.");
        }

        RelationKindOption relationKindOption = relationKindFromCode(relationKind);
        if (relationKindOption == null) {
            return buildError(model, "Нельзя сохранить связь: указан неизвестный тип связи.");
        }

        Relation relation;
        if (relationId == null) {
            relation = new Relation();
        } else {
            relation = relationDAO.getById(relationId);
            if (relation == null) {
                return buildError(model, "Нельзя сохранить изменения: связь с ID = " + relationId + " не найдена.");
            }
        }

        applyRelationKind(relation, currentPerson, otherPerson, relationKindOption);
        relation.setDateOfBeginning(beginYear);
        relation.setDateOfEnd(endYear);

        if (relationId == null) {
            relationDAO.save(relation);
        } else {
            relationDAO.update(relation);
        }

        return "redirect:/person?personId=" + personId;
    }

    @PostMapping("/removeRelation")
    public String removeRelationPage(@RequestParam(name = "personId") Long personId,
                                     @RequestParam(name = "relationId") Long relationId,
                                     Model model) {
        Relation relation = relationDAO.getById(relationId);
        if (relation == null) {
            return buildError(model, "Нельзя удалить связь: запись не найдена.");
        }

        relationDAO.delete(relation);
        return "redirect:/person?personId=" + personId;
    }

    private List<RelationView> buildRelationsForView(Person person, List<Relation> relations) {
        return relations.stream()
                .map(relation -> new RelationView(
                        relation.getId(),
                        relationTypeLabel(relation, person),
                        relatedPersonForView(relation, person),
                        relation.getDateOfBeginning(),
                        relation.getDateOfEnd()
                ))
                .toList();
    }

    private List<PersonPlaceView> buildPersonPlacesForView(List<PersonPlace> personPlaces) {
        return personPlaces.stream()
                .map(personPlace -> new PersonPlaceView(personPlace.getPlace().getId(), personPlace.getPlace()))
                .toList();
    }

    private RelationFormView buildRelationFormView(Person currentPerson, Relation relation) {
        RelationKindOption relationKindOption = relationKindForPerson(currentPerson, relation);
        Person relatedPersonForForm = relatedPersonForView(relation, currentPerson);
        return new RelationFormView(
                relation.getId(),
                relatedPersonForForm.getId(),
                relationKindOption == null ? "" : relationKindOption.getCode(),
                relation.getDateOfBeginning(),
                relation.getDateOfEnd()
        );
    }

    private String relationTypeLabel(Relation relation, Person person) {
        RelationKindOption relationKindOption = relationKindForPerson(person, relation);
        return relationKindOption == null ? relation.getRelationshipType().name() : relationKindOption.getLabel();
    }

    private Person relatedPersonForView(Relation relation, Person person) {
        return person.equals(relation.getTargetPerson()) ? relation.getRelatedPerson() : relation.getTargetPerson();
    }

    private RelationKindOption relationKindForPerson(Person person, Relation relation) {
        RelationType relationType = relation.getRelationshipType();
        if (relationType == RelationType.PARTNER) {
            return new RelationKindOption("PARTNER", "Супруг(а)");
        }

        boolean personIsTarget = person.equals(relation.getTargetPerson());
        switch (relationType) {
            case WEDLOCK_CHILD:
                return personIsTarget
                        ? new RelationKindOption("PARENT", "Родитель")
                        : new RelationKindOption("CHILD", "Ребенок");
            case ADOPTED_CHILD:
                return personIsTarget
                        ? new RelationKindOption("ADOPTIVE_PARENT", "Приемный родитель")
                        : new RelationKindOption("ADOPTED_CHILD", "Приемный ребенок");
            case BASTARD_CHILD:
                return personIsTarget
                        ? new RelationKindOption("PARENT_OUT_OF_WEDLOCK", "Родитель вне брака")
                        : new RelationKindOption("BASTARD_CHILD", "Внебрачный ребенок");
            default:
                return null;
        }
    }

    private List<RelationKindOption> relationKindOptions() {
        List<RelationKindOption> options = new ArrayList<>();
        options.add(new RelationKindOption("PARTNER", "Супруг(а)"));
        options.add(new RelationKindOption("PARENT", "Родитель"));
        options.add(new RelationKindOption("ADOPTIVE_PARENT", "Приемный родитель"));
        options.add(new RelationKindOption("PARENT_OUT_OF_WEDLOCK", "Родитель вне брака"));
        options.add(new RelationKindOption("CHILD", "Ребенок"));
        options.add(new RelationKindOption("ADOPTED_CHILD", "Приемный ребенок"));
        options.add(new RelationKindOption("BASTARD_CHILD", "Внебрачный ребенок"));
        return options;
    }

    private RelationKindOption relationKindFromCode(String code) {
        return relationKindOptions().stream()
                .filter(option -> option.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    private void applyRelationKind(Relation relation, Person currentPerson, Person otherPerson, RelationKindOption option) {
        switch (option.getCode()) {
            case "PARTNER":
                relation.setTargetPerson(currentPerson);
                relation.setRelatedPerson(otherPerson);
                relation.setRelationshipType(RelationType.PARTNER);
                break;
            case "PARENT":
                relation.setTargetPerson(currentPerson);
                relation.setRelatedPerson(otherPerson);
                relation.setRelationshipType(RelationType.WEDLOCK_CHILD);
                break;
            case "ADOPTIVE_PARENT":
                relation.setTargetPerson(currentPerson);
                relation.setRelatedPerson(otherPerson);
                relation.setRelationshipType(RelationType.ADOPTED_CHILD);
                break;
            case "PARENT_OUT_OF_WEDLOCK":
                relation.setTargetPerson(currentPerson);
                relation.setRelatedPerson(otherPerson);
                relation.setRelationshipType(RelationType.BASTARD_CHILD);
                break;
            case "CHILD":
                relation.setTargetPerson(otherPerson);
                relation.setRelatedPerson(currentPerson);
                relation.setRelationshipType(RelationType.WEDLOCK_CHILD);
                break;
            case "ADOPTED_CHILD":
                relation.setTargetPerson(otherPerson);
                relation.setRelatedPerson(currentPerson);
                relation.setRelationshipType(RelationType.ADOPTED_CHILD);
                break;
            case "BASTARD_CHILD":
                relation.setTargetPerson(otherPerson);
                relation.setRelatedPerson(currentPerson);
                relation.setRelationshipType(RelationType.BASTARD_CHILD);
                break;
            default:
                throw new IllegalArgumentException("Unsupported relation kind: " + option.getCode());
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String buildError(Model model, String message) {
        model.addAttribute("errorMessage", message);
        return "error";
    }

    public static class RelationView {
        private final Long id;
        private final String label;
        private final Person person;
        private final Integer beginYear;
        private final Integer endYear;

        public RelationView(Long id, String label, Person person, Integer beginYear, Integer endYear) {
            this.id = id;
            this.label = label;
            this.person = person;
            this.beginYear = beginYear;
            this.endYear = endYear;
        }

        public Long getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public Person getPerson() {
            return person;
        }

        public Integer getBeginYear() {
            return beginYear;
        }

        public Integer getEndYear() {
            return endYear;
        }
    }

    public static class RelationFormView {
        private final Long relationId;
        private final Long relatedPersonId;
        private final String relationKind;
        private final Integer beginYear;
        private final Integer endYear;

        public RelationFormView() {
            this(null, null, "", null, null);
        }

        public RelationFormView(Long relationId, Long relatedPersonId, String relationKind, Integer beginYear, Integer endYear) {
            this.relationId = relationId;
            this.relatedPersonId = relatedPersonId;
            this.relationKind = relationKind;
            this.beginYear = beginYear;
            this.endYear = endYear;
        }

        public Long getRelationId() {
            return relationId;
        }

        public Long getRelatedPersonId() {
            return relatedPersonId;
        }

        public String getRelationKind() {
            return relationKind;
        }

        public Integer getBeginYear() {
            return beginYear;
        }

        public Integer getEndYear() {
            return endYear;
        }
    }

    public static class RelationKindOption {
        private final String code;
        private final String label;

        public RelationKindOption(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() {
            return code;
        }

        public String getLabel() {
            return label;
        }
    }

    public static class PersonPlaceView {
        private final Long placeId;
        private final Place place;

        public PersonPlaceView(Long placeId, Place place) {
            this.placeId = placeId;
            this.place = place;
        }

        public Long getPlaceId() {
            return placeId;
        }

        public Place getPlace() {
            return place;
        }
    }

    public static class PersonPlaceFormView {
        private final Long originalPlaceId;
        private final Long placeId;

        public PersonPlaceFormView() {
            this(null, null);
        }

        public PersonPlaceFormView(Long originalPlaceId, Long placeId) {
            this.originalPlaceId = originalPlaceId;
            this.placeId = placeId;
        }

        public Long getOriginalPlaceId() {
            return originalPlaceId;
        }

        public Long getPlaceId() {
            return placeId;
        }
    }
}
