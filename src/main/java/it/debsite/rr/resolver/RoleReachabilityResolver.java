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
 * Class that solves the role-reachability problem.
 *
 * @author Alessio De Biasi
 * @version 1.1 2021-04-12
 * @since 1.1 2021-04-12
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
        // Check if the current state has already been analyzed
        if (exploredNodes.contains(nodeToExplore)) {
            return false;
        }
        // Check if the goal role is reached
        for (final UserToRolesAssignment assignment : nodeToExplore.getAssignments()) {
            if (assignment.getRoles().contains(goalRole)) {
                return true;
            }
        }
        // Add this state to the explored states
        exploredNodes.add(nodeToExplore);

        // Loop over all the user-to-roles assignments to find any administrative role
        for (final UserToRolesAssignment assignment : nodeToExplore.getAssignments()) {
            // Loop over the can-assign rules
            for (final CanAssignRule rule : canAssignRules) {
                // Check if there is a user with the administrative role
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
