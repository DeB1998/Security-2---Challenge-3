package it.debsite.rr.info;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Class that represents a <i>can-assign</i> rule.
 *
 * @author Alessio De Biasi
 * @version 1.0 2021-04-11
 * @since 1.0 2021-04-11
 */
@AllArgsConstructor
@Getter(onMethod_ = { @NotNull })
@EqualsAndHashCode
public class CanAssignRule {

    /**
     * Administrative role needed for granting the {@code roleToAssign} role.
     */
    private final Role administrativeRole;

    /**
     * Roles the user the {@code roleToAssign} role will be assigned needs to have.
     */
    private final Set<Role> preconditions;

    /**
     * Roles the user the {@code roleToAssign} role will be assigned needs not to have.
     */
    private final Set<Role> negativePreconditions;

    /**
     * Role to assign.
     */
    private final Role roleToAssign;

    /**
     * Creates a string representation of the object.
     *
     * @return A string representation of the object.
     */
    @Override
    @NotNull
    public String toString() {
        return (
            "(" +
            this.administrativeRole +
            ", " +
            this.preconditions +
            ", " +
            this.negativePreconditions +
            ", " +
            this.roleToAssign +
            ')'
        );
    }
}
