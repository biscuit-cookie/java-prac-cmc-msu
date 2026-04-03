package ru.cmc.msu.genealogy.services;

import lombok.Getter;
import org.springframework.stereotype.Service;
import ru.cmc.msu.genealogy.dao.RelationDAO;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.Relation;
import ru.cmc.msu.genealogy.models.RelationType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GenealogyTreeService {

    private final RelationDAO relationDAO;

    public GenealogyTreeService(RelationDAO relationDAO) {
        this.relationDAO = relationDAO;
    }

    public TreeResult buildTree(Person primaryPerson, Person secondaryPerson, String direction, int depth) {
        int normalizedDepth = Math.max(1, depth);
        Direction normalizedDirection = Direction.fromCode(direction);

        TreeNodeView ancestorsRoot = null;
        TreeNodeView descendantsRoot = null;
        List<PersonSummaryView> partners = partnerViews(primaryPerson);

        if (normalizedDirection == Direction.ANCESTORS || normalizedDirection == Direction.MIXED) {
            ancestorsRoot = buildAncestorsTree(primaryPerson, normalizedDepth);
        }
        if (normalizedDirection == Direction.DESCENDANTS || normalizedDirection == Direction.MIXED) {
            descendantsRoot = buildDescendantsTree(primaryPerson, normalizedDepth);
        }

        List<ConnectionStepView> connectionPath = secondaryPerson == null
                ? Collections.emptyList()
                : buildConnectionPath(primaryPerson, secondaryPerson);
        ConnectionTreeNodeView connectionTree = secondaryPerson == null
                ? null
                : buildConnectionTree(primaryPerson, secondaryPerson, connectionPath);

        return new TreeResult(primaryPerson, secondaryPerson, normalizedDirection, normalizedDepth,
                ancestorsRoot, descendantsRoot, partners, connectionPath, connectionTree);
    }

    private TreeNodeView buildAncestorsTree(Person person, int depth) {
        return buildTreeRecursive(person, depth, new HashSet<>(), TraversalMode.ANCESTORS);
    }

    private TreeNodeView buildDescendantsTree(Person person, int depth) {
        return buildTreeRecursive(person, depth, new HashSet<>(), TraversalMode.DESCENDANTS);
    }

    private TreeNodeView buildTreeRecursive(Person person, int depth, Set<Long> pathVisited, TraversalMode mode) {
        pathVisited.add(person.getId());
        List<TreeNodeView> branches = new ArrayList<>();

        if (depth > 0) {
            List<Person> nextPeople = mode == TraversalMode.ANCESTORS
                    ? sortedPeople(getParents(person))
                    : sortedPeople(getChildren(person));
            for (Person nextPerson : nextPeople) {
                if (pathVisited.contains(nextPerson.getId())) {
                    continue;
                }
                branches.add(buildTreeRecursive(nextPerson, depth - 1, new HashSet<>(pathVisited), mode));
            }
        }
        return new TreeNodeView(person, branches);
    }

    private List<PersonSummaryView> partnerViews(Person person) {
        return sortedPeople(getPartners(person)).stream()
                .map(PersonSummaryView::new)
                .toList();
    }

    private List<Person> getParents(Person person) {
        return relationDAO.getAll().stream()
                .filter(this::isParentChildRelation)
                .filter(relation -> relation.getTargetPerson() != null
                        && relation.getTargetPerson().getId().equals(person.getId()))
                .map(Relation::getRelatedPerson)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }

    private List<Person> getChildren(Person person) {
        return relationDAO.getAll().stream()
                .filter(this::isParentChildRelation)
                .filter(relation -> relation.getRelatedPerson() != null
                        && relation.getRelatedPerson().getId().equals(person.getId()))
                .map(Relation::getTargetPerson)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }

    private List<Person> getPartners(Person person) {
        return relationDAO.getAll().stream()
                .filter(relation -> relation.getRelationshipType() == RelationType.PARTNER)
                .filter(relation -> relation.getTargetPerson() != null && relation.getRelatedPerson() != null)
                .filter(relation -> relation.getTargetPerson().getId().equals(person.getId())
                        || relation.getRelatedPerson().getId().equals(person.getId()))
                .map(relation -> relation.getTargetPerson().getId().equals(person.getId())
                        ? relation.getRelatedPerson()
                        : relation.getTargetPerson())
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }

    private List<ConnectionStepView> buildConnectionPath(Person primaryPerson, Person secondaryPerson) {
        if (primaryPerson.getId().equals(secondaryPerson.getId())) {
            return List.of(new ConnectionStepView(primaryPerson, secondaryPerson, "это один и тот же человек"));
        }

        Map<Long, PathEdge> previousEdgeByPersonId = new HashMap<>();
        Deque<Person> queue = new ArrayDeque<>();
        Set<Long> visited = new HashSet<>();
        queue.add(primaryPerson);
        visited.add(primaryPerson.getId());

        while (!queue.isEmpty()) {
            Person current = queue.removeFirst();
            for (PathEdge edge : adjacentEdges(current)) {
                Long nextPersonId = edge.to().getId();
                if (visited.contains(nextPersonId)) {
                    continue;
                }
                visited.add(nextPersonId);
                previousEdgeByPersonId.put(nextPersonId, edge);
                if (nextPersonId.equals(secondaryPerson.getId())) {
                    return buildPathSteps(primaryPerson, secondaryPerson, previousEdgeByPersonId);
                }
                queue.addLast(edge.to());
            }
        }

        return Collections.emptyList();
    }

    private List<PathEdge> adjacentEdges(Person person) {
        return relationDAO.getRelationsByPerson(person).stream()
                .map(relation -> toPathEdge(person, relation))
                .sorted(Comparator.comparing(edge -> edge.to().getName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private PathEdge toPathEdge(Person fromPerson, Relation relation) {
        Person toPerson = relation.getTargetPerson().equals(fromPerson)
                ? relation.getRelatedPerson()
                : relation.getTargetPerson();
        return new PathEdge(fromPerson, toPerson, relationLabelFromPerspective(fromPerson, relation));
    }

    private List<ConnectionStepView> buildPathSteps(Person primaryPerson, Person secondaryPerson,
                                                    Map<Long, PathEdge> previousEdgeByPersonId) {
        List<ConnectionStepView> reversed = new ArrayList<>();
        Person cursor = secondaryPerson;
        while (!cursor.getId().equals(primaryPerson.getId())) {
            PathEdge edge = previousEdgeByPersonId.get(cursor.getId());
            if (edge == null) {
                return Collections.emptyList();
            }
            reversed.add(new ConnectionStepView(edge.from(), edge.to(), edge.label()));
            cursor = edge.from();
        }
        Collections.reverse(reversed);
        return reversed;
    }

    private ConnectionTreeNodeView buildConnectionTree(Person primaryPerson, Person secondaryPerson,
                                                       List<ConnectionStepView> connectionPath) {
        if (primaryPerson.getId().equals(secondaryPerson.getId())) {
            return new ConnectionTreeNodeView(primaryPerson, null, List.of(), true, true);
        }
        if (connectionPath.isEmpty()) {
            return null;
        }

        ConnectionTreeNodeView node = new ConnectionTreeNodeView(
                secondaryPerson,
                connectionPath.get(connectionPath.size() - 1).label(),
                List.of(),
                false,
                true
        );

        for (int i = connectionPath.size() - 1; i >= 0; i--) {
            ConnectionStepView step = connectionPath.get(i);
            String incomingLabel = i == 0 ? null : connectionPath.get(i - 1).label();
            node = new ConnectionTreeNodeView(
                    step.from(),
                    incomingLabel,
                    List.of(node),
                    step.from().getId().equals(primaryPerson.getId()),
                    step.from().getId().equals(secondaryPerson.getId())
            );
        }

        return node;
    }

    private String relationLabelFromPerspective(Person perspective, Relation relation) {
        RelationType relationType = relation.getRelationshipType();
        boolean perspectiveIsTarget = relation.getTargetPerson().equals(perspective);
        return switch (relationType) {
            case PARTNER -> "супруг(а)";
            case WEDLOCK_CHILD -> perspectiveIsTarget ? "родитель" : "ребенок";
            case ADOPTED_CHILD -> perspectiveIsTarget ? "приемный родитель" : "приемный ребенок";
            case BASTARD_CHILD -> perspectiveIsTarget ? "родитель вне брака" : "внебрачный ребенок";
        };
    }

    private boolean isParentChildRelation(Relation relation) {
        RelationType relationType = relation.getRelationshipType();
        return relationType == RelationType.WEDLOCK_CHILD
                || relationType == RelationType.ADOPTED_CHILD
                || relationType == RelationType.BASTARD_CHILD;
    }

    private List<Person> sortedPeople(Collection<Person> people) {
        return people.stream()
                .sorted(Comparator.comparing(Person::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Getter
    public enum Direction {
        ANCESTORS("ancestors", "Предки"),
        DESCENDANTS("descendants", "Потомки"),
        MIXED("mixed", "Смешанный режим");

        private final String code;
        private final String label;

        Direction(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public static Direction fromCode(String code) {
            for (Direction direction : values()) {
                if (direction.code.equals(code)) {
                    return direction;
                }
            }
            return ANCESTORS;
        }
    }

    private enum TraversalMode {
        ANCESTORS,
        DESCENDANTS
    }

    public record TreeResult(Person primaryPerson, Person secondaryPerson, Direction direction, int depth,
                             TreeNodeView ancestorsRoot, TreeNodeView descendantsRoot, List<PersonSummaryView> partners,
                             List<ConnectionStepView> connectionPath, ConnectionTreeNodeView connectionTree) {

    }

    public record TreeNodeView(Person person, List<TreeNodeView> children) {

    }

    public record PersonSummaryView(Person person) {

    }

    public record ConnectionStepView(Person from, Person to, String label) {

    }

    public record ConnectionTreeNodeView(Person person, String incomingLabel, List<ConnectionTreeNodeView> children,
                                         boolean primary, boolean secondary) {

    }

    private record PathEdge(Person from, Person to, String label) {

    }

}
