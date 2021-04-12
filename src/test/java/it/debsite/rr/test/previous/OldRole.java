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
public class OldRole {
    
    private String name;
    
    @Override
    public String toString() {
        
        return this.name;
    }
}
