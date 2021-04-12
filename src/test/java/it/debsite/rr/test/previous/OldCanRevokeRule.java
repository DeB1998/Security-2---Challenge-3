package it.debsite.rr.test.previous;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

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
public class OldCanRevokeRule {

    private final OldRole administrativeRole;
    
    private final OldRole roleToRevoke;
    
    @Override
    public String toString() {
        
        return "(" + this.administrativeRole + ", " + this.roleToRevoke + ')';
    }
}
