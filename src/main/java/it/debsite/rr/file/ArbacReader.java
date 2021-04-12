package it.debsite.rr.file;

import it.debsite.rr.CanAssignRule;
import it.debsite.rr.CanRevokeRule;
import it.debsite.rr.Role;
import it.debsite.rr.User;
import it.debsite.rr.UserToRoleAssignment;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

/**
 * Class that allows for reading and parsing a {@code .arbac} file.
 *
 * @author Alessio De Biasi
 * @version 1.0 2021-04-11
 * @since 1.0 2021-04-11
 */
public class ArbacReader {
    
    /**
     * Pattern of the content of {@code .arbac} files.
     */
    private static final Pattern INFORMATION_PATTERN = Pattern.compile(
            "^Roles (?<roles>[^;]+);Users (?<users>[^;]+);UA (?<ua>[^;]+);CR (?<cr>[^;]+);CA " +
                    "(?<ca>[^;]+);Goal (?<goal>[^;]+);.*"
    );
    
    /**
     * Reads and parses a {@code .arbac} file. The file charset must be UTF-8. It also assumes
     * that:
     * <ul>
     *     <li>The roles are separated by one space;</li>
     *     <li>The users are separated by one space.</li>
     * </ul>
     * <p>
     * User-to-role assignments, can-revoke and can-assign-rules may be not separated by one
     * space and may be placed on the same line or different lines.
     *
     * @param fileName Relative path of the {@code .arbac} file to read and parse. This path
     *         is relative to the root folder of the project.
     * @throws IOException If some I/O errors occur.
     */
    public ArbacInformation readAndParseFile(final String fileName) throws IOException {
        // Open the file
        try (final Scanner reader = new Scanner(new File(fileName), StandardCharsets.UTF_8)) {
            // Clear the read content
            final StringBuilder builder = new StringBuilder();
            // Read all the lines of the file into one string
            while (reader.hasNextLine()) {
                builder.append(reader.nextLine().trim());
            }
            // Parse the file
            return this.parseFile(builder.toString().trim());
        }
    }
    
    private ArbacInformation parseFile(final String fileContent) {
        
        // Extract the macro information from the file content
        final Matcher matcher = ArbacReader.INFORMATION_PATTERN.matcher(fileContent);
        
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
                        for (final UserToRoleAssignment assignment : this.userToRoleAssignments) {
                            if (assignment.getUser().equals(user)) {
                                a = assignment;
                                break;
                            }
                        }
                        if (a != null) {
                            a.getRoles().add(role);
                        } else {
                            final Set<Role> newRoles = new HashSet<>();
                            newRoles.add(role);
                            this.userToRoleAssignments.add(
                                    new UserToRoleAssignment(user, newRoles)
                            );
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
                    final Role admin = new Role(usMatcher.group("role1").trim());
                    final Role assign = new Role(usMatcher.group("role2").trim());
                    final Set<Role> preconditions = new HashSet<>();
                    final Set<Role> negPrec = new HashSet<>();
                    final String conditions = usMatcher.group("cond").trim();
                    if (!conditions.equals("TRUE")) {
                        for (final String splitCondition : conditions.split("&")) {
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
