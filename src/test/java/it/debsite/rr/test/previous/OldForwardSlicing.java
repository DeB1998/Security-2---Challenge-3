package it.debsite.rr.test.previous;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-11
 * @since version date
 */
public class OldForwardSlicing {
    
    public static boolean applyForwardSlicing(
            final List<? extends OldUserToRoleAssignment> userToRoleAssignments,
            final List<OldCanAssignRule> canAssignRules,
            final List<? extends OldCanRevokeRule> canRevokeRules,
            final Set<OldRole> roles
    ) {
        final Set<OldRole> fixpoint = OldForwardSlicing.compute(
                userToRoleAssignments,
                canAssignRules,
                roles
        );
        
        // delete the roles R \ S
        final Set<OldRole> rMinusS = new HashSet<>(roles);
        rMinusS.removeAll(fixpoint);
        boolean changed = roles.retainAll(fixpoint); // R- (R-S)
        
        // remove from CA:
        // - all the rules that include any role in R \ S* in:
        //     - the positive preconditions
        //     - in the target
        final Iterator<OldCanAssignRule> iterator = canAssignRules.iterator();
        while (iterator.hasNext()) {
            final OldCanAssignRule rule = iterator.next();
            boolean toRemove = false;
            for (final OldRole role : rule.getPreconditions()) {
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
                // remove the roles R \ S* from the negative preconditions of all rules
                changed |= rule.getNegativePreconditions().removeIf(rMinusS::contains);
            }
        }
        
        // remove from CR all the rules that mention any role in R \ S*
        changed |= canRevokeRules.removeIf(role -> rMinusS.contains(role.getRoleToRevoke()));
        
        return changed;
    }
    
    private static Set<OldRole> compute(
        final List<? extends OldUserToRoleAssignment> userToRoleAssignments,
        final List<OldCanAssignRule> canAssignRules,
        final Set<OldRole> roles
    ) {
        final Set<OldRole> initialSet = new HashSet<>();
        for (final OldUserToRoleAssignment u : userToRoleAssignments) {
            initialSet.addAll(u.getRoles());
        }

        return OldForwardSlicing.fixpoint(initialSet, canAssignRules, roles);
    }

    private static Set<OldRole> fixpoint(
        final Set<OldRole> previous,
        final List<OldCanAssignRule> canAssignRules,
        final Set<OldRole> roles
    ) {
        final Set<OldRole> newRoles = new HashSet<>();

        for (final OldRole role : roles) {
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

        return OldForwardSlicing.fixpoint(previous, canAssignRules, roles);
    }
}
