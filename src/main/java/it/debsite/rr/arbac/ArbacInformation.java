package it.debsite.rr.arbac;

import it.debsite.rr.info.CanAssignRule;
import it.debsite.rr.info.CanRevokeRule;
import it.debsite.rr.info.Role;
import it.debsite.rr.info.User;
import it.debsite.rr.info.UserToRolesAssignment;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Class that contains all the information specified inside a {@code .arbac} file.
 *
 * @author Alessio De Biasi
 * @version 1.0 2021-04-12
 * @since 1.1 2021-04-12
 */
@AllArgsConstructor
@Getter
public class ArbacInformation {

    /**
     * Set of all the roles.
     */
    private final Set<Role> roles;

    /**
     * Set of all the users.
     */
    private final Set<User> users;

    /**
     * Set in which each element specifies the roles assigned to a user.
     */
    private final List<UserToRolesAssignment> userToRoleAssignments;

    /**
     * List of <i>can-assign</i> rules.
     */
    private final List<CanAssignRule> canAssignRules;

    /**
     * List of <i>can-revoke</i> rules;
     */
    private final List<CanRevokeRule> canRevokeRules;

    /**
     * Role to verify to be reachable.
     */
    private final Role goalRole;
}
