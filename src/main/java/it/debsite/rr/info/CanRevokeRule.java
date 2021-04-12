package it.debsite.rr.info;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Class that represents a <i>can-revoke</i> rule.
 *
 * @author Alessio De Biasi
 * @version 1.0 2021-04-11
 * @since 1.0 2021-04-11
 */
@AllArgsConstructor
@Getter(onMethod_ = { @NotNull })
@EqualsAndHashCode
public class CanRevokeRule {

    /**
     * Administrative role needed for revoking the {@code roleToRevoke} role.
     */
    private final Role administrativeRole;

    /**
     * Role to revoke.
     */
    private final Role roleToRevoke;

    /**
     * Creates a string representation of the object.
     *
     * @return A string representation of the object.
     */
    @Override
    @NotNull
    public String toString() {
        return "(" + this.administrativeRole + ", " + this.roleToRevoke + ')';
    }
}
