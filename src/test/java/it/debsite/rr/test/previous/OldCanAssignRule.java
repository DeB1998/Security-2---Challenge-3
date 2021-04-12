package it.debsite.rr.test.previous;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;
import java.util.Set;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-11
 * @since version date
 */
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class OldCanAssignRule {

    private final OldRole administrativeRole;
    
    private final Set<OldRole> preconditions;
    
    private final Set<OldRole> negativePreconditions;
    
    private final OldRole roleToAssign;
    
    @Override
    public String toString() {
        
        return "(" + this.administrativeRole + ", " + this.preconditions + ", " + this.negativePreconditions + ", " + this.roleToAssign + ')';
    }
}
