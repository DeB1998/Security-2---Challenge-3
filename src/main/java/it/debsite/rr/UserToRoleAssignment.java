package it.debsite.rr;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-11
 * @since version date
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class UserToRoleAssignment {
    
    private final User user;
    
    private final Set<Role> roles;
    
    public UserToRoleAssignment(UserToRoleAssignment otherAssignment) {
        
        this.user = otherAssignment.user;
        this.roles = new HashSet<>(otherAssignment.roles);
    }
    
    @Override
    public String toString() {
        
        return "UA (" + this.user + ", " + this.roles + ')';
    }
}
