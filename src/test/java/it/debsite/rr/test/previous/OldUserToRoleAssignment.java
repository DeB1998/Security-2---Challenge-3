package it.debsite.rr.test.previous;

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
public class OldUserToRoleAssignment {
    
    private final OldUser user;
    
    private final Set<OldRole> roles;
    
    public OldUserToRoleAssignment(OldUserToRoleAssignment otherAssignment) {
        
        this.user = otherAssignment.user;
        this.roles = new HashSet<>(otherAssignment.roles);
    }
    
    @Override
    public String toString() {
        
        return "(" + this.user + ", " + this.roles + ')';
    }
}
