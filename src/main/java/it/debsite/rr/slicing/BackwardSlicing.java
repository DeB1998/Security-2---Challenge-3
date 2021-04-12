package it.debsite.rr.slicing;

import it.debsite.rr.CanAssignRule;
import it.debsite.rr.Role;
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
public class BackwardSlicing {

    public static Set<Role> compute(Role goalRole, List<CanAssignRule> canAssignRules) {
        final Set<Role> initialSet = new HashSet<>();
        initialSet.add(goalRole);

        return fixpoint(initialSet, canAssignRules);
    }

    private static Set<Role> fixpoint(Set<Role> previous, List<CanAssignRule> canAssignRules) {
        Set<Role> newRoles = new HashSet<>();
        // Si−1 ∪ {Rp ∪ Rn ∪ {ra} | (ra, Rp, Rn,rt) ∈ CA ∧ rt ∈ Si−1}

        for (Role r : previous) {
            for (CanAssignRule rule : canAssignRules) {
                if (rule.getRoleToAssign().equals(r)) {
                    newRoles.addAll(rule.getPreconditions());
                    newRoles.addAll(rule.getNegativePreconditions());
                    newRoles.add(rule.getAdministrativeRole());
                }
            }
        }

        if (!previous.addAll(newRoles)) {
            return previous;
        }

        return BackwardSlicing.fixpoint(previous, canAssignRules);
    }
}
