package it.debsite.rr.file;

import it.debsite.rr.CanAssignRule;
import it.debsite.rr.CanRevokeRule;
import it.debsite.rr.Role;
import it.debsite.rr.User;
import it.debsite.rr.UserToRoleAssignment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-11
 * @since version date
 */
@Getter
public class ArbacReader {

    /*
     * Roles Teacher Student TA ;
     * Users stefano alice bob ;
     * UA <stefano,Teacher> <alice,TA> ;
     * CR <Teacher,Student> <Teacher,TA> ;
     * CA <Teacher,-Teacher&-TA,Student>
     * <Teacher,-Student,TA>
     * <Teacher,TA&-Student,Teacher> ;
     * Goal Student ;
     */

    private final Set<Role> roles;

    private final Set<User> users;

    private final List<UserToRoleAssignment> userToRoleAssignments;

    private final List<CanRevokeRule> canRevokeRules;

    private final List<CanAssignRule> canAssignRules;

    private Role goalRole;

    public ArbacReader() {
        this.roles = new HashSet<>();
        this.users = new HashSet<>();
        this.userToRoleAssignments = new ArrayList<>();
        this.canRevokeRules = new ArrayList<>();
        this.canAssignRules = new ArrayList<>();
        this.goalRole = new Role("");
    }

    public void readFile(final String fileName) throws IOException {
        try (final Scanner reader = new Scanner(new File(fileName), StandardCharsets.UTF_8)) {
            final StringBuilder builder = new StringBuilder();
            while (reader.hasNextLine()) {
                builder.append(reader.nextLine().trim());
            }
            this.extractParts(builder.toString().trim());
        }
    }

    private void extractParts(final String fileContent) {
        // CONT Roles Teacher Student TA ;Users stefano alice bob ;UA <stefano,Teacher> <alice,
        // TA> ;CR <Teacher,Student> <Teacher,TA> ;CA <Teacher,-Teacher&-TA,Student><Teacher,
        // -Student,TA><Teacher,TA&-Student,Teacher> ;Goal Student ;

        final Pattern pattern = Pattern.compile(
            "^Roles (?<roles>[^;]+);Users (?<users>[^;]+);UA (?<ua>[^;]+);CR (?<cr>[^;]+);CA " +
            "(?<ca>[^;]+);Goal (?<goal>[^;]+);.*"
        );
        final Matcher matcher = pattern.matcher(fileContent);

        if (matcher.matches()) {
            for (final String roleName : matcher.group("roles").split(" ")) {
                this.roles.add(new Role(roleName));
            }
            for (final String userName : matcher.group("users").split(" ")) {
                this.users.add(new User(userName));
            }

            final Pattern usPattern = Pattern.compile("<(?<user>[^,]+),(?<role>[^>]+)");
            for (final String assignments : matcher.group("ua").split(">")) {
                if (!assignments.trim().isEmpty()) {
                    final Matcher usMatcher = usPattern.matcher(assignments.trim());
                    if (usMatcher.matches()) {
                        final User user = new User(usMatcher.group("user"));
                        final Role role = new Role(usMatcher.group("role"));
                        @Nullable
                        UserToRoleAssignment a = null;
                        for (UserToRoleAssignment assignment : this.userToRoleAssignments) {
                            if (assignment.getUser().equals(user)) {
                                a = assignment;
                                break;
                            }
                        }
                        if (a != null) {
                            a.getRoles().add(role);
                        } else {
                            Set<Role> newRoles = new HashSet<>();
                            newRoles.add(role);
                            this.userToRoleAssignments.add(new UserToRoleAssignment(user, newRoles));
                        }
                    } else {
                        throw new IllegalArgumentException("File is not well-formed");
                    }
                }
            }

            final String canRevokeRulesA = matcher.group("cr");
            final Pattern crPattern = Pattern.compile("<(?<role1>[^,]+),(?<role2>[^>]+)");
            for (final String canRevoke : canRevokeRulesA.split(">")) {
                final Matcher usMatcher = crPattern.matcher(canRevoke.trim());
                if (usMatcher.matches()) {
                    this.canRevokeRules.add(
                            new CanRevokeRule(
                                new Role(usMatcher.group("role1")),
                                new Role(usMatcher.group("role2"))
                            )
                        );
                }
            }

            final String canAssignString = matcher.group("ca");
            final Pattern caPattern = Pattern.compile(
                "<(?<role1>[^,]+),(?<cond>[^>]+)," + "(?<role2>[^>]+)"
            );
            for (final String canAssign : canAssignString.split(">")) {
                final Matcher usMatcher = caPattern.matcher(canAssign.trim());
                if (usMatcher.matches()) {
                    Role admin = new Role(usMatcher.group("role1").trim());
                    Role assign = new Role(usMatcher.group("role2").trim());
                    Set<Role> preconditions = new HashSet<>();
                    Set<Role> negPrec = new HashSet<>();
                    String conditions = usMatcher.group("cond").trim();
                    if (!conditions.equals("TRUE")) {
                        for (String splitCondition : conditions.split("&")) {
                            if (splitCondition.trim().startsWith("-")) {
                                negPrec.add(new Role(splitCondition.trim().substring(1)));
                            } else {
                                preconditions.add(new Role(splitCondition.trim()));
                            }
                        }
                    }
                    this.canAssignRules.add(
                            new CanAssignRule(admin, preconditions, negPrec, assign)
                        );
                    /*this.canRevokeRules.add(
                            new CanRevokeRule(new Role(usMatcher.group("role1")),
                                    new Role(usMatcher.group("role2"))
                            )
                    );*/
                }
            }

            this.goalRole = new Role(matcher.group("goal").trim());
        } else {
            throw new IllegalArgumentException("File is not well-formed");
        }
    }
}
