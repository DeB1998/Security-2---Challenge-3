package it.debsite.rr.test.previous;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-12
 * @since version date
 */
public class OldReaching {

    public static boolean reaching(
        final OldGraphNode nodeToExplore,
        final Set<OldGraphNode> exploredNodes,
        final List<OldCanAssignRule> canAssignRules,
        final List<OldCanRevokeRule> canRevokeRules,
        final OldRole goalRole
    ) {
        if (exploredNodes.contains(nodeToExplore)) {
            return false;
        }
        for (OldUserToRoleAssignment assignment : nodeToExplore.getAssignments()) {
            if (assignment.getRoles().contains(goalRole)) {
                return true;
            }
        }
        exploredNodes.add(nodeToExplore);

        // USER -> {r1, r2, r3}
        for (final OldUserToRoleAssignment assignment : nodeToExplore.getAssignments()) {
            for (final OldCanAssignRule rule : canAssignRules) {
                if (assignment.getRoles().contains(rule.getAdministrativeRole())) {
                    int i = 0;
                    for (final OldUserToRoleAssignment otherAssignments : nodeToExplore.getAssignments()) {
                        if (
                            otherAssignments.getRoles().containsAll(rule.getPreconditions()) &&
                            Collections.disjoint(
                                otherAssignments.getRoles(),
                                rule.getNegativePreconditions()
                            )
                        ) {
                            final OldUserToRoleAssignment newAssignment = new OldUserToRoleAssignment(
                                otherAssignments
                            );
                            newAssignment.getRoles().add(rule.getRoleToAssign());
                            final List<OldUserToRoleAssignment> newAssignments = new ArrayList<>(
                                nodeToExplore.getAssignments()
                            );
                            newAssignments.set(i, newAssignment);
                            final OldGraphNode newNode = new OldGraphNode(newAssignments);
                            if (
                                reaching(
                                    newNode,
                                    exploredNodes,
                                    canAssignRules,
                                    canRevokeRules,
                                    goalRole
                                )
                            ) {
                                return true;
                            }
                        }
                        i++;
                    }
                }
            }
            for (final OldCanRevokeRule rule : canRevokeRules) {
                if (assignment.getRoles().contains(rule.getAdministrativeRole())) {
                    int i = 0;
                    for (final OldUserToRoleAssignment otherAssignments : nodeToExplore.getAssignments()) {
                        if (otherAssignments.getRoles().contains(rule.getRoleToRevoke())) {
                            final OldUserToRoleAssignment newAssignment = new OldUserToRoleAssignment(
                                otherAssignments
                            );
                            newAssignment.getRoles().remove(rule.getRoleToRevoke());
                            final List<OldUserToRoleAssignment> newAssignments = new ArrayList<>(
                                nodeToExplore.getAssignments()
                            );
                            newAssignments.set(i, newAssignment);
                            final OldGraphNode newNode = new OldGraphNode(newAssignments);
                            if (
                                reaching(
                                    newNode,
                                    exploredNodes,
                                    canAssignRules,
                                    canRevokeRules,
                                    goalRole
                                )
                            ) {
                                return true;
                            }
                        }
                        i++;
                    }
                }
            }
        }

        return false;
    }
}
