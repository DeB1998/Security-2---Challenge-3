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
                // Check if there is a user with the administrative role stated in the rule
                if (assignment.getRoles().contains(rule.getAdministrativeRole())) {
                    // Look for the users the rule can be applied to
                    int i = 0;
                    for (final UserToRolesAssignment otherAssignments :
                            nodeToExplore.getAssignments()) {
                        // Check if the rule is applicable
                        if (
                                otherAssignments.getRoles().containsAll(rule.getPreconditions()) &&
                                        Collections.disjoint(
                                                otherAssignments.getRoles(),
                                                rule.getNegativePreconditions()
                                        )
                        ) {
                            // Create a new user-to-roles assignment where the user is granted
                            // the new role
                            final UserToRolesAssignment newAssignment = new UserToRolesAssignment(
                                    otherAssignments
                            );
                            newAssignment.getRoles().add(rule.getRoleToAssign());
                            // Create a new state with the new assignments
                            final List<UserToRolesAssignment> newAssignments = new ArrayList<>(
                                    nodeToExplore.getAssignments()
                            );
                            newAssignments.set(i, newAssignment);
                            final ReachingState newNode = new ReachingState(newAssignments);
                            // Run the role reachability problem on the new state and check if
                            // the target role is reachable
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
            // Loop over the can-revoke rules
            for (final CanRevokeRule rule : canRevokeRules) {
                // Check if there is a user with the administrative role stated in the rule
                if (assignment.getRoles().contains(rule.getAdministrativeRole())) {
                    // Look for the users the rule can be applied to
                    int i = 0;
                    for (final UserToRolesAssignment otherAssignments :
                            nodeToExplore.getAssignments()) {
                        // Check if the rule is applicable
                        if (otherAssignments.getRoles().contains(rule.getRoleToRevoke())) {
                            // Create a new user-to-roles assignment where the user is revoked
                            // the role
                            final UserToRolesAssignment newAssignment = new UserToRolesAssignment(
                                    otherAssignments
                            );
                            newAssignment.getRoles().remove(rule.getRoleToRevoke());
                            // Create a new state with the new assignments
                            final List<UserToRolesAssignment> newAssignments = new ArrayList<>(
                                    nodeToExplore.getAssignments()
                            );
                            newAssignments.set(i, newAssignment);
                            final ReachingState newNode = new ReachingState(newAssignments);
                            // Run the role reachability problem on the new state and check if
                            // the target role is reachable
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
        
        // The target role is not reachable
        return false;
    }
}
