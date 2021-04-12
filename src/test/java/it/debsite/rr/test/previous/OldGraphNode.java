package it.debsite.rr.test.previous;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

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
public class OldGraphNode {
    
    private final List<OldUserToRoleAssignment> assignments;
}
