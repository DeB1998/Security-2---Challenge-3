package it.debsite.rr.info;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Description.
 *
 * @author Alessio De Biasi
 * @version 1.0 2021-04-11
 * @since 1.0 2021-04-11
 */
@AllArgsConstructor
@Getter(onMethod_ = { @NotNull})
@EqualsAndHashCode
public class User {
    
    /**
     * Name of the user.
     */
    private final String name;
    
    /**
     * Creates a string representation of the object.
     *
     * @return A string representation of the object.
     */
    @Override
    @NotNull
    public String toString() {
        
        return this.name;
    }
}
