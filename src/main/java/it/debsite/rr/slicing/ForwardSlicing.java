package it.debsite.rr.slicing;

import it.debsite.rr.CanAssignRule;
import it.debsite.rr.Role;
import it.debsite.rr.UserToRoleAssignment;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-11
 * @since version date
 */
public class ForwardSlicing {
    
    public static Set<Role> compute(
        final List<? extends UserToRoleAssignment> userToRoleAssignments,
        final List<CanAssignRule> canAssignRules,
        final Set<Role> roles
    ) {
        final Set<Role> initialSet = new HashSet<>();
        for (final UserToRoleAssignment u : userToRoleAssignments) {
            initialSet.addAll(u.getRoles());
        }

        return ForwardSlicing.fixpoint(initialSet, canAssignRules, roles);
    }

    private static Set<Role> fixpoint(
        final Set<Role> previous,
        final List<CanAssignRule> canAssignRules,
        final Set<Role> roles
    ) {
        final Set<Role> newRoles = new HashSet<>();

        for (final Role role : roles) {
            if (
                    // {rt ∈ R | (ra, Rp, Rn,rt) ∈ CA ∧ Rp ∪ {ra} ⊆ Si−1}
                canAssignRules
                    .stream()
                    .anyMatch(
                        value ->
                            value.getRoleToAssign().equals(role) &&
                            previous.containsAll(value.getPreconditions()) &&
                            previous.contains(value.getAdministrativeRole())
                    )
            ) {
                newRoles.add(role);
            }
        }

        if (!previous.addAll(newRoles)) {
            return previous;
        }

        return ForwardSlicing.fixpoint(previous, canAssignRules, roles);
    }
}
