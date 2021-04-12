package it.debsite.rr;

import lombok.AllArgsConstructor;
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
public class CanRevokeRule {

    private final Role administrativeRole;
    
    private final Role roleToRevoke;
    
    @Override
    public String toString() {
        
        return "(" + this.administrativeRole + ", " + this.roleToRevoke + ')';
    }
}
