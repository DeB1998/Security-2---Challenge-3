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
import org.jetbrains.annotations.NotNull;

/**
 * Class that solves the role-reachability problem.
 *
 * @author Alessio De Biasi
 * @version 1.1 2021-04-12
 * @since 1.1 2021-04-12
 */
public final class RoleReachabilityResolver {

    /**
     * Constructor that prevents this class from being instantiated.
     */
    private RoleReachabilityResolver() {}

    /**
     * Solve the role reachability problem on the given ARBAC information.
     *
     * @param information ARBAC information to use to solve the role reachability problem.
     * @return {@code true} if the rule specified into {@link ArbacInformation#getGoalRole()} is
     *         reachable from the initial user-to-roles assignments specified into {@link
     *         ArbacInformation#getUserToRoleAssignments()}, {@code false} otherwise.
     */
    @SuppressWarnings({ "FeatureEnvy", "BooleanMethodNameMustStartWithQuestion" })
    public static boolean solveRoleReachabilityProblem(
        @NotNull final ArbacInformation information
    ) {
        // Create the initial state
        final ReachingState initialNode = new ReachingState(information.getUserToRoleAssignments());

        // Solve the role reachability problem starting from the initial state
        return RoleReachabilityResolver.solveRoleReachabilityProblem(
            initialNode,
            new HashSet<>(),
            information.getCanAssignRules(),
            information.getCanRevokeRules(),
            information.getGoalRole()
        );
    }

    /**
     * Recursively solves the role reachability problem.
     *
     * @param stateToExplore State to analyze.
     * @param exploredStates Set of all the already-explored states.
     * @param canAssignRules List of <i>can-assign</i> rules.
     * @param canRevokeRules List of <i>can-revoke</i> rules.
     * @param goalRole Role to prove to be reachable.
     * @return {@code true} if {@code goalRole} is proved to be reachable from the {@code
     *         stateToExplore}, {@code false} otherwise.
     */
    @SuppressWarnings(
        {
            "BooleanMethodNameMustStartWithQuestion",
            "FeatureEnvy",
            "MethodWithMultipleReturnPoints",
            "OverlyComplexMethod",
            "OverlyNestedMethod",
            "ObjectAllocationInLoop"
        }
    )
    private static boolean solveRoleReachabilityProblem(
        @NotNull final ReachingState stateToExplore,
        @NotNull final Set<? super ReachingState> exploredStates,
        @NotNull final List<? extends CanAssignRule> canAssignRules,
        @NotNull final List<? extends CanRevokeRule> canRevokeRules,
        @NotNull final Role goalRole
    ) {
        // Check if the current state has already been analyzed
        if (exploredStates.contains(stateToExplore)) {
            return false;
        }
        // Check if the goal role is reached
        for (final UserToRolesAssignment assignment : stateToExplore.getAssignments()) {
            if (assignment.getRoles().contains(goalRole)) {
                return true;
            }
        }
        // Add this state to the explored states
        exploredStates.add(stateToExplore);

        // Loop over all the user-to-roles assignments to find any administrative role
        for (final UserToRolesAssignment assignment : stateToExplore.getAssignments()) {
            // Loop over the can-assign rules
            for (final CanAssignRule rule : canAssignRules) {
                // Check if there is a user with the administrative role stated in the rule
                if (assignment.getRoles().contains(rule.getAdministrativeRole())) {
                    // Look for the users the rule can be applied to
                    int i = 0;
                    for (final UserToRolesAssignment otherAssignments : stateToExplore.getAssignments()) {
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
                                stateToExplore.getAssignments()
                            );
                            newAssignments.set(i, newAssignment);
                            final ReachingState newNode = new ReachingState(newAssignments);
                            // Run the role reachability problem on the new state and check if
                            // the target role is reachable
                            if (
                                RoleReachabilityResolver.solveRoleReachabilityProblem(
                                    newNode,
                                    exploredStates,
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
                    for (final UserToRolesAssignment otherAssignments : stateToExplore.getAssignments()) {
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
                                stateToExplore.getAssignments()
                            );
                            newAssignments.set(i, newAssignment);
                            final ReachingState newNode = new ReachingState(newAssignments);
                            // Run the role reachability problem on the new state and check if
                            // the target role is reachable
                            if (
                                RoleReachabilityResolver.solveRoleReachabilityProblem(
                                    newNode,
                                    exploredStates,
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
