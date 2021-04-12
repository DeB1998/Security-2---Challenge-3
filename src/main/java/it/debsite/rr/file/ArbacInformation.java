package it.debsite.rr.file;

import it.debsite.rr.CanAssignRule;
import it.debsite.rr.CanRevokeRule;
import it.debsite.rr.Role;
import it.debsite.rr.User;
import it.debsite.rr.UserToRoleAssignment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Set;

/**
 * Class that contains all the information specifed inside a {@code .arbac} file.
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
     * 
     */
    private final List<UserToRoleAssignment> userToRoleAssignments;
    
    private final List<CanRevokeRule> canRevokeRules;
    
    private final List<CanAssignRule> canAssignRules;
    
    private final Role goalRole;
}
