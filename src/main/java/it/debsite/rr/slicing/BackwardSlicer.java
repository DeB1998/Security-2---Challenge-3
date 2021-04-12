package it.debsite.rr.slicing;

import it.debsite.rr.arbac.ArbacInformation;
import it.debsite.rr.info.CanAssignRule;
import it.debsite.rr.info.CanRevokeRule;
import it.debsite.rr.info.Role;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * Class that implements the backward slicing algorithm.
 *
 * @author Alessio De Biasi
 * @version 1.1 2021-04-12
 * @since 1.0 2021-04-11
 */
public final class BackwardSlicer {

    /**
     * Constructor that prevents this class from being instantiated.
     */
    private BackwardSlicer() {}

    /**
     * Applies the backward slicing algorithm on the given ARBAC information, changing such
     * information.
     *
     * @param information ARBAC information to reduce by applying the backward slicing
     *         algorithm.
     * @return @{@code true} if the algorithm changed the information, {@code false} otherwise.
     */
    @SuppressWarnings({ "BooleanMethodNameMustStartWithQuestion", "FeatureEnvy" })
    public static boolean applyBackwardSlicing(@NotNull final ArbacInformation information) {
        // Extract the single information
        final Role goalRole = information.getGoalRole();
        final List<CanAssignRule> canAssignRules = information.getCanAssignRules();
        final List<? extends CanRevokeRule> canRevokeRules = information.getCanRevokeRules();
        final Set<Role> roles = information.getRoles();

        // Compute the fixpoint S*
        final Set<Role> fixpoint = BackwardSlicer.computeFixpoint(goalRole, canAssignRules);

        // Compute R \ S*
        final Collection<Role> rMinusS = new HashSet<>(roles);
        rMinusS.removeAll(fixpoint);

        // Delete the roles R \ S*, i.e., compute R \ (R \ S*)
        boolean changed = roles.retainAll(fixpoint);

        // Remove from CA all the rules that assign a role in R \ S*
        changed |= canAssignRules.removeIf(value -> rMinusS.contains(value.getRoleToAssign()));
        // Remove from CR all the rules that revoke a role in R n S*
        changed |= canRevokeRules.removeIf(value -> rMinusS.contains(value.getRoleToRevoke()));

        return changed;
    }

    /**
     * Computes the fixpoint of the backward slicing algorithm over the provided information.
     *
     * @param goalRole Role to start from.
     * @param canAssignRules List of <i>can-assign</i> rules.
     * @return The set S* of roles computed by the fixpoint algorithm.
     */
    @SuppressWarnings("FeatureEnvy")
    @NotNull
    private static Set<Role> computeFixpoint(
        @NotNull final Role goalRole,
        @NotNull final Iterable<? extends CanAssignRule> canAssignRules
    ) {
        // Start with the only role to reach
        final Set<Role> previous = new HashSet<>();
        previous.add(goalRole);

        // Initialize the new roles state S_i
        final Collection<Role> newRoles = new HashSet<>();
        do {
            // Create the S_i set as an empty set
            newRoles.clear();
            // Loop over the roles present into S_{i-1}
            for (final Role role : previous) {
                // Loop over the can-assign rules
                for (final CanAssignRule rule : canAssignRules) {
                    // Check if the rule assigns r_t
                    if (rule.getRoleToAssign().equals(role)) {
                        // Add the R_p, R_n and r_a sets to the S_i set
                        newRoles.addAll(rule.getPreconditions());
                        newRoles.addAll(rule.getNegativePreconditions());
                        newRoles.add(rule.getAdministrativeRole());
                    }
                }
            }
        } while (previous.addAll(newRoles));

        // Return the fixpoint S*
        return previous;
    }
}
