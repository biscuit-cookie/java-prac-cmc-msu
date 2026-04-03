package ru.cmc.msu.genealogy.services;

import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.Test;
import ru.cmc.msu.genealogy.dao.RelationDAO;
import ru.cmc.msu.genealogy.models.Person;
import ru.cmc.msu.genealogy.models.Relation;
import ru.cmc.msu.genealogy.models.RelationType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class GenealogyTreeServiceTest {

    @Test
    public void buildTreeUsesFallbackDirectionAndCollectsAncestorsPartners() {
        RelationDAO relationDAO = mock(RelationDAO.class);
        GenealogyTreeService service = new GenealogyTreeService(relationDAO);

        Person primary = person(1L, "Игорь");
        Person parent = person(2L, "Михаил");
        Person partner = person(3L, "Елена");
        Person child = person(4L, "Павел");

        Relation parentRelation = relation(primary, parent, RelationType.WEDLOCK_CHILD);
        Relation partnerRelation = relation(partner, primary, RelationType.PARTNER);
        Relation childRelation = relation(child, primary, RelationType.WEDLOCK_CHILD);

        when(relationDAO.getAll()).thenReturn(List.of(parentRelation, partnerRelation, childRelation));

        GenealogyTreeService.TreeResult result = service.buildTree(primary, null, "unsupported", 0);

        assertEquals(result.direction(), GenealogyTreeService.Direction.ANCESTORS);
        assertEquals(result.depth(), 1);
        assertNotNull(result.ancestorsRoot());
        assertEquals(result.ancestorsRoot().children().size(), 1);
        assertEquals(result.ancestorsRoot().children().get(0).person().getId(), Long.valueOf(2L));
        assertEquals(result.partners().size(), 1);
        assertEquals(result.partners().get(0).person().getId(), Long.valueOf(3L));
    }

    @Test
    public void buildTreeIgnoresRelationsWithMissingPeopleInFilters() {
        RelationDAO relationDAO = mock(RelationDAO.class);
        GenealogyTreeService service = new GenealogyTreeService(relationDAO);

        Person primary = person(1L, "Игорь");
        Person parent = person(2L, "Михаил");
        Person child = person(3L, "Павел");
        Person partner = person(4L, "Елена");

        Relation validParent = relation(primary, parent, RelationType.WEDLOCK_CHILD);
        Relation validChild = relation(child, primary, RelationType.WEDLOCK_CHILD);
        Relation validPartner = relation(partner, primary, RelationType.PARTNER);
        Relation missingTarget = relationWithNulls(null, parent, RelationType.WEDLOCK_CHILD);
        Relation missingRelated = relationWithNulls(child, null, RelationType.WEDLOCK_CHILD);
        Relation missingPartnerTarget = relationWithNulls(null, primary, RelationType.PARTNER);
        Relation missingPartnerRelated = relationWithNulls(partner, null, RelationType.PARTNER);

        when(relationDAO.getAll()).thenReturn(List.of(
                validParent, missingTarget, validChild, missingRelated,
                validPartner, missingPartnerTarget, missingPartnerRelated
        ));
        when(relationDAO.getRelationsByPerson(any(Person.class))).thenReturn(List.of(validParent, validChild, validPartner));

        GenealogyTreeService.TreeResult result = service.buildTree(primary, null, "mixed", 2);

        assertEquals(result.ancestorsRoot().children().size(), 1);
        assertEquals(result.ancestorsRoot().children().get(0).person().getId(), Long.valueOf(2L));
        assertEquals(result.descendantsRoot().children().size(), 1);
        assertEquals(result.descendantsRoot().children().get(0).person().getId(), Long.valueOf(3L));
        assertEquals(result.partners().size(), 1);
        assertEquals(result.partners().get(0).person().getId(), Long.valueOf(4L));
    }

    @Test
    public void buildTreeSkipsAlreadyVisitedNodesInCycles() {
        RelationDAO relationDAO = mock(RelationDAO.class);
        GenealogyTreeService service = new GenealogyTreeService(relationDAO);

        Person primary = person(1L, "A");
        Person parent = person(2L, "B");

        when(relationDAO.getAll()).thenReturn(List.of(
                relation(primary, parent, RelationType.WEDLOCK_CHILD),
                relation(parent, primary, RelationType.WEDLOCK_CHILD)
        ));

        GenealogyTreeService.TreeResult result = service.buildTree(primary, null, "ancestors", 3);

        assertEquals(result.ancestorsRoot().children().size(), 1);
        assertTrue(result.ancestorsRoot().children().get(0).children().isEmpty());
    }

    @Test
    public void buildTreeSupportsDescendantsOnlyDirectionAndPartnerOnTargetSide() {
        RelationDAO relationDAO = mock(RelationDAO.class);
        GenealogyTreeService service = new GenealogyTreeService(relationDAO);

        Person primary = person(1L, "Игорь");
        Person child = person(2L, "Павел");
        Person partner = person(3L, "Елена");

        Relation childRelation = relation(child, primary, RelationType.WEDLOCK_CHILD);
        Relation partnerRelation = relation(primary, partner, RelationType.PARTNER);

        when(relationDAO.getAll()).thenReturn(List.of(childRelation, partnerRelation));

        GenealogyTreeService.TreeResult result = service.buildTree(primary, null, "descendants", 2);

        assertNull(result.ancestorsRoot());
        assertNotNull(result.descendantsRoot());
        assertEquals(result.descendantsRoot().children().size(), 1);
        assertEquals(result.descendantsRoot().children().get(0).person().getId(), Long.valueOf(2L));
        assertEquals(result.partners().size(), 1);
        assertEquals(result.partners().get(0).person().getId(), Long.valueOf(3L));
    }

    @Test
    public void buildTreeWithSecondaryPersonCoversMissingAndSamePersonPaths() {
        RelationDAO relationDAO = mock(RelationDAO.class);
        GenealogyTreeService service = new GenealogyTreeService(relationDAO);

        Person primary = person(1L, "Игорь");
        Person secondary = person(9L, "Наталья");

        when(relationDAO.getAll()).thenReturn(Collections.emptyList());
        when(relationDAO.getRelationsByPerson(any(Person.class))).thenReturn(Collections.emptyList());

        GenealogyTreeService.TreeResult unrelated = service.buildTree(primary, secondary, "mixed", 2);
        assertTrue(unrelated.connectionPath().isEmpty());
        assertNull(unrelated.connectionTree());

        @SuppressWarnings("unchecked")
        List<GenealogyTreeService.ConnectionStepView> samePath =
                (List<GenealogyTreeService.ConnectionStepView>) ReflectionTestUtils.invokeMethod(
                        service, "buildConnectionPath", primary, primary);
        assertEquals(samePath.size(), 1);
        assertEquals(samePath.get(0).label(), "это один и тот же человек");

        GenealogyTreeService.ConnectionTreeNodeView sameTree = ReflectionTestUtils.invokeMethod(
                service, "buildConnectionTree", primary, primary, samePath);
        assertNotNull(sameTree);
        assertTrue(sameTree.primary());
        assertTrue(sameTree.secondary());
    }

    @Test
    public void privateHelpersCoverRelationLabelsAndFallbackBranches() {
        RelationDAO relationDAO = mock(RelationDAO.class);
        GenealogyTreeService service = new GenealogyTreeService(relationDAO);

        Person primary = person(1L, "Игорь");
        Person secondary = person(2L, "Павел");

        Relation partnerRelation = relation(primary, secondary, RelationType.PARTNER);
        assertEquals(ReflectionTestUtils.invokeMethod(service, "relationLabelFromPerspective", primary, partnerRelation),
                "супруг(а)");

        Relation adoptedRelation = relation(primary, secondary, RelationType.ADOPTED_CHILD);
        assertEquals(ReflectionTestUtils.invokeMethod(service, "relationLabelFromPerspective", primary, adoptedRelation),
                "приемный родитель");
        assertEquals(ReflectionTestUtils.invokeMethod(service, "relationLabelFromPerspective", secondary, adoptedRelation),
                "приемный ребенок");

        Relation bastardRelation = relation(primary, secondary, RelationType.BASTARD_CHILD);
        assertEquals(ReflectionTestUtils.invokeMethod(service, "relationLabelFromPerspective", primary, bastardRelation),
                "родитель вне брака");
        assertEquals(ReflectionTestUtils.invokeMethod(service, "relationLabelFromPerspective", secondary, bastardRelation),
                "внебрачный ребенок");

        Relation wedlockRelation = relation(primary, secondary, RelationType.WEDLOCK_CHILD);
        assertEquals(ReflectionTestUtils.invokeMethod(service, "relationLabelFromPerspective", primary, wedlockRelation),
                "родитель");
        assertEquals(ReflectionTestUtils.invokeMethod(service, "relationLabelFromPerspective", secondary, wedlockRelation),
                "ребенок");

        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(service, "isParentChildRelation", partnerRelation));
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(service, "isParentChildRelation", adoptedRelation));
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(service, "isParentChildRelation", bastardRelation));

        Map<Long, Object> emptyPathMap = new HashMap<>();
        @SuppressWarnings("unchecked")
        List<GenealogyTreeService.ConnectionStepView> emptyPath =
                (List<GenealogyTreeService.ConnectionStepView>) ReflectionTestUtils.invokeMethod(
                        service, "buildPathSteps", primary, secondary, emptyPathMap);
        assertTrue(emptyPath.isEmpty());

        assertEquals(GenealogyTreeService.Direction.fromCode("???"), GenealogyTreeService.Direction.ANCESTORS);
        assertEquals(GenealogyTreeService.Direction.ANCESTORS.getCode(), "ancestors");
        assertEquals(GenealogyTreeService.Direction.MIXED.getLabel(), "Смешанный режим");
    }

    private static Person person(Long id, String name) {
        return new Person(id, name, "Мужской", 1900, null, "desc");
    }

    private static Relation relation(Person target, Person related, RelationType type) {
        return new Relation(null, target, related, null, null, type);
    }

    private static Relation relationWithNulls(Person target, Person related, RelationType type) {
        Relation relation = mock(Relation.class);
        when(relation.getTargetPerson()).thenReturn(target);
        when(relation.getRelatedPerson()).thenReturn(related);
        when(relation.getRelationshipType()).thenReturn(type);
        return relation;
    }
}
