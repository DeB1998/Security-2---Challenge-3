package it.debsite.rr.slicing;

import it.debsite.rr.arbac.ArbacInformation;
import it.debsite.rr.info.CanAssignRule;
import it.debsite.rr.info.CanRevokeRule;
import it.debsite.rr.info.Role;
import it.debsite.rr.info.UserToRolesAssignment;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * Class that implements the forward slicing algorithm.
 *
 * @author Alessio De Biasi
 * @version 1.1 2021-04-12
 * @since 1.0 2021-04-11
 */
public final class ForwardSlicer {

    /**
     * Constructor that prevents this class from being instantiated.
     */
    private ForwardSlicer() {}

    /**
     * Applies the forward slicing algorithm on the given ARBAC information, changing such
     * information.
     *
     * @param information ARBAC information to reduce by applying the forward slicing
     *         algorithm.
     * @return @{@code true} if the algorithm changed the information, {@code false} otherwise.
     */
    @SuppressWarnings({ "BooleanMethodNameMustStartWithQuestion", "FeatureEnvy" })
    public static boolean applyForwardSlicing(@NotNull final ArbacInformation information) {
        // Extract the single information
        //noinspection LocalVariableNamingConvention
        final List<UserToRolesAssignment> userToRoleAssignments =
                information.getUserToRoleAssignments();
        final List<CanAssignRule> canAssignRules = information.getCanAssignRules();
        final List<CanRevokeRule> canRevokeRules = information.getCanRevokeRules();
        final Set<Role> roles = information.getRoles();

        // Compute the fixpoint S*
        final Set<Role> fixpoint = ForwardSlicer.computeFixpoint(
            userToRoleAssignments,
            canAssignRules,
            roles
        );

        // Compute R \ S*
        final Set<Role> rMinusS = new HashSet<>(roles);
        rMinusS.removeAll(fixpoint);
        // Delete the R \ S* roles, i.e., compute R \ (R \ S*)
        boolean changed = roles.retainAll(fixpoint);

        // remove from CA all the rules that include any role in R \ S* in:
        //     - the positive preconditions
        //     - in the target
        final Iterator<CanAssignRule> iterator = canAssignRules.iterator();
        while (iterator.hasNext()) {
            final CanAssignRule rule = iterator.next();
            boolean toRemove = false;
            for (final Role role : rule.getPreconditions()) {
                if (rMinusS.contains(role)) {
                    toRemove = true;
                    break;
                }
            }
            toRemove = toRemove || rMinusS.contains(rule.getRoleToAssign());
            if (toRemove) {
                iterator.remove();
                changed = true;
            } else {
                // Remove the roles R \ S* from the negative preconditions of all rules
                //noinspection ObjectAllocationInLoop
                changed |= rule.getNegativePreconditions().removeIf(rMinusS::contains);
            }
        }

        // Remove from CR all the rules that mention any role in R \ S*
        changed |=
            canRevokeRules.removeIf(
                role ->
                    rMinusS.contains(role.getRoleToRevoke()) ||
                    rMinusS.contains(role.getAdministrativeRole())
            );

        return changed;
    }

    /**
     * Computes the fixpoint of the forward slicing algorithm over the provided information.
     *
     * @param userToRoleAssignments List of <i>user-to-roles</i> assignments.
     * @param canAssignRules List of <i>can-assign</i> rules.
     * @param roles Set of roles.
     * @return The set S* of roles computed by the fixpoint algorithm.
     */
    @SuppressWarnings({ "MethodParameterNamingConvention", "FeatureEnvy" })
    @NotNull
    private static Set<Role> computeFixpoint(
        @NotNull final Iterable<? extends UserToRolesAssignment> userToRoleAssignments,
        final @NotNull Iterable<? extends CanAssignRule> canAssignRules,
        final @NotNull Iterable<? extends Role> roles
    ) {
        // Start with the initially assigned roles
        final Set<Role> previous = new HashSet<>();
        for (final UserToRolesAssignment u : userToRoleAssignments) {
            previous.addAll(u.getRoles());
        }
        // Initialize the new roles state S_i
        final Collection<Role> newRoles = new HashSet<>();

        do {
            // Create the S_i set as an empty set
            newRoles.clear();

            // Loop over the possible roles
            for (final Role role : roles) {
                // Loop over the can assign rules
                for (final CanAssignRule value : canAssignRules) {
                    // Check if the previous state S_{i-1} contains the preconditions and the
                    // administrative role mentioned in the rule and check the rule assigns the
                    // role r_t
                    if (
                        value.getRoleToAssign().equals(role) &&
                        previous.containsAll(value.getPreconditions()) &&
                        previous.contains(value.getAdministrativeRole())
                    ) {
                        // Add the role to S_i
                        newRoles.add(role);
                        break;
                    }
                }
            }
        } while (previous.addAll(newRoles));

        // Return the fixpoint S*
        return previous;
    }
}
