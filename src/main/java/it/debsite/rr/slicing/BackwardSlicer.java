package it.debsite.rr.slicing;

import it.debsite.rr.file.ArbacInformation;
import it.debsite.rr.info.CanAssignRule;
import it.debsite.rr.info.CanRevokeRule;
import it.debsite.rr.info.Role;
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
public class BackwardSlicer {

    public static boolean applyBackwardSlicing(final ArbacInformation information) {
        final Role goalRole = information.getGoalRole();
        final List<CanAssignRule> canAssignRules = information.getCanAssignRules();
        final List<? extends CanRevokeRule> canRevokeRules = information.getCanRevokeRules();
        final Set<Role> roles = information.getRoles();

        final Set<Role> fixpoint = BackwardSlicer.computeFixpoint(goalRole, canAssignRules);

        // delete the roles R \ S
        final Set<Role> rMinusS = new HashSet<>(roles);
        rMinusS.removeAll(fixpoint);
        boolean changed = roles.retainAll(fixpoint);

        // remove from CA all the rules that assign a role in R \ S
        changed |= canAssignRules.removeIf(value -> rMinusS.contains(value.getRoleToAssign()));
        changed |= canRevokeRules.removeIf(value -> rMinusS.contains(value.getRoleToRevoke()));

        return changed;
    }

    private static Set<Role> computeFixpoint(final Role goalRole, final List<CanAssignRule> canAssignRules) {
        final Set<Role> previous = new HashSet<>();
        previous.add(goalRole);

        final Set<Role> newRoles = new HashSet<>();
        // Si−1 ∪ {Rp ∪ Rn ∪ {ra} | (ra, Rp, Rn,rt) ∈ CA ∧ rt ∈ Si−1}
        do {
            newRoles.clear();
            for (final Role r : previous) {
                for (final CanAssignRule rule : canAssignRules) {
                    if (rule.getRoleToAssign().equals(r)) {
                        newRoles.addAll(rule.getPreconditions());
                        newRoles.addAll(rule.getNegativePreconditions());
                        newRoles.add(rule.getAdministrativeRole());
                    }
                }
            }
        } while (previous.addAll(newRoles));

        return previous;
    }
}
