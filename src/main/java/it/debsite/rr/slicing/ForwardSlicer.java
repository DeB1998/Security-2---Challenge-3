package it.debsite.rr.slicing;

import it.debsite.rr.file.ArbacInformation;
import it.debsite.rr.info.CanAssignRule;
import it.debsite.rr.info.CanRevokeRule;
import it.debsite.rr.info.Role;
import it.debsite.rr.info.UserToRolesAssignment;
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
public class ForwardSlicer {

    /**
     * Applies the forward slicing algorithm to reduce the state space.
     * @param information Information to appy the forward slicing algorithm to.
     * @return
     */
    public static boolean applyForwardSlicing(final ArbacInformation information) {
        final List<UserToRolesAssignment> userToRoleAssignments = information.getUserToRoleAssignments();
        final List<CanAssignRule> canAssignRules = information.getCanAssignRules();
        final List<CanRevokeRule> canRevokeRules = information.getCanRevokeRules();
        final Set<Role> roles = information.getRoles();

        // Compute the fixpoint
        final Set<Role> fixpoint = ForwardSlicer.computeFixpoint(
            userToRoleAssignments,
            canAssignRules,
            roles
        );

        // Compute R \ S
        final Set<Role> rMinusS = new HashSet<>(roles);
        rMinusS.removeAll(fixpoint);
        // Delete the R \ S roles, i.e., compute R \ (R \ S)
        boolean changed = roles.retainAll(fixpoint);

        // remove from CA:
        // - all the rules that include any role in R \ S* in:
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
                // remove the roles R \ S* from the negative preconditions of all rules
                changed |= rule.getNegativePreconditions().removeIf(rMinusS::contains);
            }
        }

        // remove from CR all the rules that mention any role in R \ S*
        changed |= canRevokeRules.removeIf(role -> rMinusS.contains(role.getRoleToRevoke()));

        return changed;
    }

    private static Set<Role> computeFixpoint(
        final List<? extends UserToRolesAssignment> userToRoleAssignments,
        final List<CanAssignRule> canAssignRules,
        final Set<Role> roles
    ) {
        final Set<Role> previous = new HashSet<>();
        for (final UserToRolesAssignment u : userToRoleAssignments) {
            previous.addAll(u.getRoles());
        }
        final Set<Role> newRoles = new HashSet<>();
        
        do {
            newRoles.clear();
    
            for (final Role role : roles) {
                // {rt ∈ R | (ra, Rp, Rn,rt) ∈ CA ∧ Rp ∪ {ra} ⊆ Si−1}
                for (final CanAssignRule value : canAssignRules) {
                    if (value.getRoleToAssign().equals(role) &&
                            previous.containsAll(value.getPreconditions()) &&
                            previous.contains(value.getAdministrativeRole())) {
                        newRoles.add(role);
                        break;
                    }
                }
            }
    
        }while (previous.addAll(newRoles));
        
        return previous;
    }
}
