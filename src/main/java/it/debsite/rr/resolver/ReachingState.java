package it.debsite.rr.resolver;

import it.debsite.rr.info.UserToRolesAssignment;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

/**
 * Class that models a reachable state in the role reachability problem..
 *
 * @author DeB
 * @version 1.0 2021-04-11
 * @since 1.0 2021-04-11
 */
@AllArgsConstructor
@Getter
@EqualsAndHashCode
class ReachingState {
    
    /**
     * List of the <i>user-to-roles</i> assignment inside the state.
     */
    private final List<UserToRolesAssignment> assignments;
}
