package it.debsite.rr.info;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Represents all the roles assigned to a user.<br> To simplify and optimize the code, the
 * user-to-role assignments that mention the same user are condensed in only one {@link
 * UserToRolesAssignment} object that holds the user and all the roles assigned to him/her.
 *
 * @author Alessio De Biasi
 * @version 1.0 2021-04-11
 * @since 1.0 2021-04-11
 */
@AllArgsConstructor
@Getter(onMethod_ = { @NotNull })
@EqualsAndHashCode
public class UserToRolesAssignment {
    
    /**
     * User that is assigned the roles.
     */
    private final User user;
    
    /**
     * Roles that are assigned to the user.
     */
    private final Set<Role> roles;
    
    /**
     * Creates a deep copy of the specified {@code UserToRolesAssignment}.
     *
     * @param otherAssignment {@code UserToRolesAssignment} to copy.
     */
    public UserToRolesAssignment(final @NotNull UserToRolesAssignment otherAssignment) {
        
        // Share the user since it is immutable
        this.user = otherAssignment.user;
        // Copy the roles
        this.roles = new HashSet<>(otherAssignment.roles);
    }
    
    /**
     * Creates a string representation of the object.
     *
     * @return A string representation of the object.
     */
    @Override
    @NotNull
    public String toString() {
        
        return "(" + this.user + ", " + this.roles + ')';
    }
}
