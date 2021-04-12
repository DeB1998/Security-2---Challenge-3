package it.debsite.rr.test;

import it.debsite.rr.info.CanAssignRule;
import it.debsite.rr.info.CanRevokeRule;
import it.debsite.rr.info.Role;
import it.debsite.rr.info.User;
import it.debsite.rr.info.UserToRolesAssignment;
import it.debsite.rr.test.previous.OldCanAssignRule;
import it.debsite.rr.test.previous.OldCanRevokeRule;
import it.debsite.rr.test.previous.OldRole;
import it.debsite.rr.test.previous.OldUser;
import it.debsite.rr.test.previous.OldUserToRoleAssignment;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import org.jetbrains.annotations.NonNls;
import org.junit.jupiter.api.Assertions;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-12
 * @since version date
 */
public class Utils {

    static <T, U> boolean checkCollectionsEqual(
        final Collection<? extends T> firstCollection,
        final Collection<U> secondCollection,
        final BiFunction<T, U, Boolean> areElementsEqual
    ) {
        if (firstCollection.size() != secondCollection.size()) {
            return false;
        }
        for (final T firstCollectionElement : firstCollection) {
            boolean found = false;
            for (U secondColectionElement : secondCollection) {
                if (areElementsEqual.apply(firstCollectionElement, secondColectionElement)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }

    static void compareRolesSet(Set<OldRole> oldRoles, Set<Role> newRoles) {
        // Test roles
        Assertions.assertTrue(
            Utils.checkCollectionsEqual(oldRoles, newRoles, Utils::compareRoles),
            "Error in checking roles"
        );
    }

    static void compareUsersSet(Set<OldUser> oldUsers, Set<User> newUsers) {
        // Test roles
        Assertions.assertTrue(
            Utils.checkCollectionsEqual(oldUsers, newUsers, Utils::compareUsers),
            "Error in checking users"
        );
    }

    static void compareUserToRolesAssignments(
        List<OldUserToRoleAssignment> oldAssignments,
        List<UserToRolesAssignment> newAssignments
    ) {
        // Test roles
        Assertions.assertTrue(
            Utils.checkCollectionsEqual(oldAssignments, newAssignments, Utils::compareUserToRoles),
            "Error in checking user to role"
        );
    }

    private static boolean compareUserToRoles(
        final OldUserToRoleAssignment oldUserToRoleAssignment,
        final UserToRolesAssignment newUserToRolesAssignment
    ) {
        return (
            Utils.checkCollectionsEqual(
                oldUserToRoleAssignment.getRoles(),
                newUserToRolesAssignment.getRoles(),
                Utils::compareRoles
            ) &&
            Utils.compareUsers(
                oldUserToRoleAssignment.getUser(),
                newUserToRolesAssignment.getUser()
            )
        );
    }

    static void compareCanAssignRulesList(
        List<OldCanAssignRule> oldCanAssignRules,
        List<CanAssignRule> newCanAssignRules
    ) {
        // Test roles
        Assertions.assertTrue(
            Utils.checkCollectionsEqual(
                oldCanAssignRules,
                newCanAssignRules,
                Utils::compareCanAssignRules
            ),
            "Error in checking user to role"
        );
    }

    static void compareCanRevokeRules(
        List<OldCanRevokeRule> oldCanRevokeRules,
        List<CanRevokeRule> newCanRevokeRules
    ) {
        Assertions.assertTrue(
            Utils.checkCollectionsEqual(
                oldCanRevokeRules,
                newCanRevokeRules,
                Utils::compareCanRevokeRules
            ),
            "Error in checking can-revoke rules"
        );
    }

    static boolean compareRoles(final OldRole oldRole, final Role newRole) {
        return oldRole.getName().equals(newRole.getName());
    }

    private static boolean compareUsers(final OldUser oldUser, final User newUser) {
        return oldUser.getName().equals(newUser.getName());
    }

    private static boolean compareCanAssignRules(
        final OldCanAssignRule oldCanAssignRule,
        final CanAssignRule newCanAssignRule
    ) {
        return (
            Utils.checkCollectionsEqual(
                oldCanAssignRule.getPreconditions(),
                newCanAssignRule.getPreconditions(),
                Utils::compareRoles
            ) &&
            Utils.checkCollectionsEqual(
                oldCanAssignRule.getNegativePreconditions(),
                newCanAssignRule.getNegativePreconditions(),
                Utils::compareRoles
            ) &&
            (
                Utils.compareRoles(
                    oldCanAssignRule.getAdministrativeRole(),
                    newCanAssignRule.getAdministrativeRole()
                ) &&
                Utils.compareRoles(
                    oldCanAssignRule.getRoleToAssign(),
                    newCanAssignRule.getRoleToAssign()
                )
            )
        );
    }

    private static boolean compareCanRevokeRules(
        final OldCanRevokeRule oldCanRevokeRule,
        final CanRevokeRule newCanRevokeRule
    ) {
        return (
            Utils.compareRoles(
                oldCanRevokeRule.getAdministrativeRole(),
                newCanRevokeRule.getAdministrativeRole()
            ) &&
            Utils.compareRoles(
                oldCanRevokeRule.getRoleToRevoke(),
                newCanRevokeRule.getRoleToRevoke()
            )
        );
    }
}
