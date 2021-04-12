package it.debsite.rr.resolver;

import it.debsite.rr.info.UserToRolesAssignment;
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
public class ReachingState {
    
    private final List<UserToRolesAssignment> assignments;
}
