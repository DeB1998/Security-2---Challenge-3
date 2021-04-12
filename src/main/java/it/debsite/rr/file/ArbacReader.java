package it.debsite.rr.file;

import it.debsite.rr.info.CanAssignRule;
import it.debsite.rr.info.CanRevokeRule;
import it.debsite.rr.info.Role;
import it.debsite.rr.info.User;
import it.debsite.rr.info.UserToRolesAssignment;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
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
    @SuppressWarnings("ConstantExpression")
    private static final Pattern FILE_CONTENT_PATTERN = Pattern.compile(
        "^Roles (?<roles>[^;]+);Users (?<users>[^;]+);UA (?<ua>[^;]+);CR (?<cr>[^;]+);CA " +
        "(?<ca>[^;]+);Goal (?<goal>[^;]+);.*"
    );

    /**
     * Pattern of each <i>user-to-role</i>assignment.
     */
    private static final Pattern UA_ASSIGNMENT_PATTERN = Pattern.compile(
        "<(?<user>[^,]+),(?<role>[^>]+)"
    );

    /**
     * Pattern of each <i>can-assign</i> rule.
     */
    private static final Pattern CAN_ASSIGN_RULE_PATTERN = Pattern.compile(
        "<(?<admin>[^,]+),(?<conditions>[^>]+),(?<target>[^>]+)"
    );

    /**
     * Pattern of each <i>can-revoke</i> rule.
     */
    private static final Pattern CAN_REVOKE_RULE_PATTERN = Pattern.compile(
        "<(?<admin>[^,]+),(?<target>[^>]+)"
    );

    /**
     * Callback used to create a new role. It is a constant for performance reasons.
     */
    private static final Function<String, Role> ROLE_CREATOR = Role::new;

    /**
     * Callback used to create a new user. It is a constant for performance reasons.
     */
    private static final Function<String, User> USER_CREATOR = User::new;

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
     * @param filePath Relative path of the {@code .arbac} file to read and parse. This path
     *         is relative to the root folder of the project.
     * @return The information read and parsed from the specified file.
     * @throws IOException If some I/O errors occur.
     */
    @NotNull
    public ArbacInformation readAndParseFile(@NotNull final String filePath) throws IOException {
        // Open the file
        try (final Scanner reader = new Scanner(new File(filePath), StandardCharsets.UTF_8)) {
            // Clear the read content
            final StringBuilder fileContentBuilder = new StringBuilder();
            // Read all the lines of the file into one string
            while (reader.hasNextLine()) {
                fileContentBuilder.append(reader.nextLine().trim());
            }
            // Parse the file
            return ArbacReader.parseFileContent(filePath, fileContentBuilder.toString().trim());
        }
    }

    /**
     * Parses the content of the file and extracts the information.
     *
     * @param filePath Path of the read file.
     * @param fileContent String that holds the content of the file as a single-row string.
     * @return The information parsed from the specified file content.
     */
    @NotNull
    private static ArbacInformation parseFileContent(
        @NotNull final String filePath,
        @NotNull final CharSequence fileContent
    ) {
        // Extract the macro information from the file content
        final Matcher matcher = ArbacReader.FILE_CONTENT_PATTERN.matcher(fileContent);

        // CHeck if the file content is well-formed
        if (matcher.matches()) {
            // Extract the roles
            final Set<Role> roles = ArbacReader.extractSpaceSeparatedInformation(
                matcher.group("roles"),
                ArbacReader.ROLE_CREATOR
            );
            // Extract the users
            final Set<User> users = ArbacReader.extractSpaceSeparatedInformation(
                matcher.group("users"),
                ArbacReader.USER_CREATOR
            );

            // Extract the user-to-roles assignments
            final List<UserToRolesAssignment> userToRolesAssignments = ArbacReader.extractUserToRolesAssignments(
                filePath,
                matcher.group("ua")
            );

            // Extract the can-assign rules
            final List<CanAssignRule> canAssignRules = ArbacReader.extractCanAssignRules(
                filePath,
                matcher.group("ca")
            );

            // Extract the can-revoke rules
            final List<CanRevokeRule> canRevokeRules = ArbacReader.extractCanRevokeRules(
                filePath,
                matcher.group("cr")
            );

            // Extract the goal role
            final Role goalRole = new Role(matcher.group("goal").trim());

            // Return the gathered information
            return new ArbacInformation(
                roles,
                users,
                userToRolesAssignments,
                canAssignRules,
                canRevokeRules,
                goalRole
            );
        }
        // The file is not well-formed
        throw new IllegalArgumentException("File " + filePath + " is not well-formed.");
    }

    /**
     * Parses and extracts the information from a space-separated sequence of elements. Each of
     * those elements are extracted an passed to {@code creator} in order to extract the
     * information.
     *
     * @param wholeString String to parse.
     * @param creator Function that extracts the information from a single element inside
     *         the space-separated string.
     * @param <T> Type of the information returned.
     * @return The set of extracted information.
     */
    @NotNull
    private static <T> Set<T> extractSpaceSeparatedInformation(
        final @NotNull String wholeString,
        final @NotNull Function<? super String, ? extends T> creator
    ) {
        // Clear the result
        final Set<T> result = new HashSet<>();

        // Loop of the elements in the space-separated sequence
        for (final String information : wholeString.split(" ")) {
            // Extract the information from each element
            result.add(creator.apply(information));
        }

        return result;
    }
    
    /**
     *
     * @param filePath
     * @param uaAssignmentsString
     * @return
     */
    @NotNull
    private static List<UserToRolesAssignment> extractUserToRolesAssignments(
        @NotNull final String filePath,
        @NotNull final String uaAssignmentsString
    ) {
        // Clear the result
        final List<UserToRolesAssignment> result = new ArrayList<>();

        // Loop over all the user-to-role assignments
        for (final String uaAssignmentString : uaAssignmentsString.split(">")) {
            // Skip empty strings
            if (!uaAssignmentString.trim().isEmpty()) {
                // Extract the user and the assigned role
                final Matcher matcher = ArbacReader.UA_ASSIGNMENT_PATTERN.matcher(
                    uaAssignmentString.trim()
                );
                // Check if the rules are well-formed
                if (matcher.matches()) {
                    // Create the information
                    final User user = new User(matcher.group("user"));
                    final Role role = new Role(matcher.group("role"));

                    // Add the role to the user or create a new user-to-roles assignment
                    @Nullable
                    UserToRolesAssignment userAssignment = null;
                    // Loop over all user-to-roles assignments
                    for (final UserToRolesAssignment assignment : result) {
                        // Check if the user is already present in the result
                        if (assignment.getUser().equals(user)) {
                            userAssignment = assignment;
                            break;
                        }
                    }
                    // Check if an already-present assignment has been found
                    if (userAssignment != null) {
                        // Add the role
                        userAssignment.getRoles().add(role);
                    } else {
                        // Create and add a new user-to-roles assignment
                        final Set<Role> newRoles = new HashSet<>();
                        newRoles.add(role);
                        result.add(new UserToRolesAssignment(user, newRoles));
                    }
                } else {
                    // The can-revoke rules are not well-formed
                    throw new IllegalArgumentException(
                        "User-to-role assignments inside file " +
                        filePath +
                        " are not " +
                        "well-formed."
                    );
                }
            }
        }

        return result;
    }

    @NotNull
    private static List<CanAssignRule> extractCanAssignRules(
        @NotNull final String filePath,
        @NotNull final String canAssignRulesString
    ) {
        // Clear the result
        final List<CanAssignRule> result = new ArrayList<>();

        for (final String canAssign : canAssignRulesString.split(">")) {
            final String ruleString = canAssign.trim();
            if (!ruleString.isEmpty()) {
                final Matcher matcher = ArbacReader.CAN_ASSIGN_RULE_PATTERN.matcher(ruleString);
                if (matcher.matches()) {
                    // Create the basic information
                    final Role admin = new Role(matcher.group("admin").trim());
                    final Role assign = new Role(matcher.group("target").trim());
                    final Set<Role> preconditions = new HashSet<>();
                    final Set<Role> negativePreconditions = new HashSet<>();

                    // Extract the conditions
                    @NonNls
                    final String conditions = matcher.group("conditions").trim();
                    // Skip TRUE conditions
                    if (!conditions.equals("TRUE")) {
                        // Loop over all conditions
                        for (final String splitCondition : conditions.split("&")) {
                            // Trim the string
                            final String trimmedCondition = splitCondition.trim();
                            // CHeck if the condition is a precondition or a negative one
                            if (trimmedCondition.startsWith("-")) {
                                negativePreconditions.add(new Role(trimmedCondition.substring(1)));
                            } else {
                                preconditions.add(new Role(trimmedCondition));
                            }
                        }
                    }
                    // Add the new rule
                    result.add(
                        new CanAssignRule(admin, preconditions, negativePreconditions, assign)
                    );
                } else {
                    // The can-revoke rules are not well-formed
                    throw new IllegalArgumentException(
                        "Can-assign rules inside file " + filePath + " are not well-formed."
                    );
                }
            }
        }

        return result;
    }

    @NotNull
    private static List<CanRevokeRule> extractCanRevokeRules(
        @NotNull final String filePath,
        @NotNull final String canRevokeRulesString
    ) {
        // Clear the result
        final List<CanRevokeRule> result = new ArrayList<>();

        // Divide each can-revoke rule
        for (final String canRevokeRuleString : canRevokeRulesString.split(">")) {
            final String ruleString = canRevokeRuleString.trim();
            if (!ruleString.isEmpty()) {
                // Extract the administrative and target roles
                final Matcher matcher = ArbacReader.CAN_REVOKE_RULE_PATTERN.matcher(ruleString);
                // Check if the rules are well-formed
                if (matcher.matches()) {
                    // Create the new rule
                    result.add(
                        new CanRevokeRule(
                            new Role(matcher.group("admin")),
                            new Role(matcher.group("target"))
                        )
                    );
                } else {
                    // The can-revoke rules are not well-formed
                    throw new IllegalArgumentException(
                        "Can-revoke rules inside file " + filePath + " are not well-formed."
                    );
                }
            }
        }

        return result;
    }
}
