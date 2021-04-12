package it.debsite.rr.resolver;

import it.debsite.rr.arbac.ArbacInformation;
import it.debsite.rr.info.CanAssignRule;
import it.debsite.rr.info.CanRevokeRule;
import it.debsite.rr.info.Role;
import it.debsite.rr.info.UserToRolesAssignment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Description.
 *
 * @author Alessio De Biasi
 * @version 1.0 2021-04-12
 * @since version date
 */
public class RoleReachabilityResolver {

    public static boolean solveRoleReachabilityProblem(final ArbacInformation information) {
        final ReachingState initialNode = new ReachingState(information.getUserToRoleAssignments());

        return RoleReachabilityResolver.reaching(
            initialNode,
            new HashSet<>(),
            information.getCanAssignRules(),
            information.getCanRevokeRules(),
            information.getGoalRole()
        );
    }

    private static boolean reaching(
        final ReachingState nodeToExplore,
        final Set<? super ReachingState> exploredNodes,
        final List<? extends CanAssignRule> canAssignRules,
        final List<? extends CanRevokeRule> canRevokeRules,
        final Role goalRole
    ) {
        if (exploredNodes.contains(nodeToExplore)) {
            return false;
        }
        for (final UserToRolesAssignment assignment : nodeToExplore.getAssignments()) {
            if (assignment.getRoles().contains(goalRole)) {
                return true;
            }
        }
        exploredNodes.add(nodeToExplore);

        // USER -> {r1, r2, r3}
        for (final UserToRolesAssignment assignment : nodeToExplore.getAssignments()) {
            for (final CanAssignRule rule : canAssignRules) {
                if (assignment.getRoles().contains(rule.getAdministrativeRole())) {
                    int i = 0;
                    for (final UserToRolesAssignment otherAssignments : nodeToExplore.getAssignments()) {
                        if (
                            otherAssignments.getRoles().containsAll(rule.getPreconditions()) &&
                            Collections.disjoint(
                                otherAssignments.getRoles(),
                                rule.getNegativePreconditions()
                            )
                        ) {
                            final UserToRolesAssignment newAssignment = new UserToRolesAssignment(
                                otherAssignments
                            );
                            newAssignment.getRoles().add(rule.getRoleToAssign());
                            final List<UserToRolesAssignment> newAssignments = new ArrayList<>(
                                nodeToExplore.getAssignments()
                            );
                            newAssignments.set(i, newAssignment);
                            final ReachingState newNode = new ReachingState(newAssignments);
                            if (
                                RoleReachabilityResolver.reaching(
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
            for (final CanRevokeRule rule : canRevokeRules) {
                if (assignment.getRoles().contains(rule.getAdministrativeRole())) {
                    int i = 0;
                    for (final UserToRolesAssignment otherAssignments : nodeToExplore.getAssignments()) {
                        if (otherAssignments.getRoles().contains(rule.getRoleToRevoke())) {
                            final UserToRolesAssignment newAssignment = new UserToRolesAssignment(
                                otherAssignments
                            );
                            newAssignment.getRoles().remove(rule.getRoleToRevoke());
                            final List<UserToRolesAssignment> newAssignments = new ArrayList<>(
                                nodeToExplore.getAssignments()
                            );
                            newAssignments.set(i, newAssignment);
                            final ReachingState newNode = new ReachingState(newAssignments);
                            if (
                                RoleReachabilityResolver.reaching(
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
