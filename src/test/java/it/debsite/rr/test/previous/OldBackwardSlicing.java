package it.debsite.rr.test.previous;

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
public class OldBackwardSlicing {
    
    public static boolean applyBackwardSlicing(
            final OldRole goalRole,
            final List<OldCanAssignRule> canAssignRules,
            final List<? extends OldCanRevokeRule> canRevokeRules,
            final Set<OldRole> roles
    ) {
        final Set<OldRole> fixpoint = OldBackwardSlicing.compute(goalRole, canAssignRules);
        
        // delete the roles R \ S
        final Set<OldRole> rMinusS = new HashSet<>(roles);
        rMinusS.removeAll(fixpoint);
        boolean changed = roles.retainAll(fixpoint);
        
        // remove from CA all the rules that assign a role in R \ S
        changed |= canAssignRules.removeIf(value -> rMinusS.contains(value.getRoleToAssign()));
        changed |= canRevokeRules.removeIf(value -> rMinusS.contains(value.getRoleToRevoke()));
        
        return changed;
    }
    
    private static Set<OldRole> compute(OldRole goalRole, List<OldCanAssignRule> canAssignRules) {
        final Set<OldRole> initialSet = new HashSet<>();
        initialSet.add(goalRole);

        return fixpoint(initialSet, canAssignRules);
    }

    private static Set<OldRole> fixpoint(Set<OldRole> previous, List<OldCanAssignRule> canAssignRules) {
        Set<OldRole> newRoles = new HashSet<>();
        // Si−1 ∪ {Rp ∪ Rn ∪ {ra} | (ra, Rp, Rn,rt) ∈ CA ∧ rt ∈ Si−1}

        for (OldRole r : previous) {
            for (OldCanAssignRule rule : canAssignRules) {
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

        return OldBackwardSlicing.fixpoint(previous, canAssignRules);
    }
}
