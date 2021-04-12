package it.debsite.rr;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
public class CanAssignRule {

    private final Role administrativeRole;
    
    private final Set<Role> preconditions;
    
    private final Set<Role> negativePreconditions;
    
    private final Role roleToAssign;
    
    @Override
    public String toString() {
        
        return "(" + this.administrativeRole + ", " + this.preconditions + ", " + this.negativePreconditions + ", " + this.roleToAssign + ')';
    }
}
