package it.debsite.rr.test.previous;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-11
 * @since version date
 */
public class OldArbacReader {

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

    private final Set<OldRole> roles;

    private final Set<OldUser> users;

    private final List<OldUserToRoleAssignment> userToRoleAssignments;

    private final List<OldCanRevokeRule> canRevokeRules;

    private final List<OldCanAssignRule> canAssignRules;

    private OldRole goalRole;

    public  OldArbacReader() {
        this.roles = new HashSet<>();
        this.users = new HashSet<>();
        this.userToRoleAssignments = new ArrayList<>();
        this.canRevokeRules = new ArrayList<>();
        this.canAssignRules = new ArrayList<>();
        this.goalRole = new OldRole("");
    }
    
    public Set<OldRole> getRoles() {
        
        return this.roles;
    }
    
    public Set<OldUser> getUsers() {
        
        return this.users;
    }
    
    public List<OldUserToRoleAssignment> getUserToRoleAssignments() {
        
        return this.userToRoleAssignments;
    }
    
    public List<OldCanRevokeRule> getCanRevokeRules() {
        
        return this.canRevokeRules;
    }
    
    public List<OldCanAssignRule> getCanAssignRules() {
        
        return this.canAssignRules;
    }
    
    public OldRole getGoalRole() {
        
        return this.goalRole;
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
                this.roles.add(new OldRole(roleName));
            }
            for (final String userName : matcher.group("users").split(" ")) {
                this.users.add(new OldUser(userName));
            }

            final Pattern usPattern = Pattern.compile("<(?<user>[^,]+),(?<role>[^>]+)");
            for (final String assignments : matcher.group("ua").split(">")) {
                if (!assignments.trim().isEmpty()) {
                    final Matcher usMatcher = usPattern.matcher(assignments.trim());
                    if (usMatcher.matches()) {
                        final OldUser user = new OldUser(usMatcher.group("user"));
                        final OldRole role = new OldRole(usMatcher.group("role"));
                        @Nullable
                        OldUserToRoleAssignment a = null;
                        for (OldUserToRoleAssignment assignment : this.userToRoleAssignments) {
                            if (assignment.getUser().equals(user)) {
                                a = assignment;
                                break;
                            }
                        }
                        if (a != null) {
                            a.getRoles().add(role);
                        } else {
                            Set<OldRole> newRoles = new HashSet<>();
                            newRoles.add(role);
                            this.userToRoleAssignments.add(new OldUserToRoleAssignment(user, newRoles));
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
                            new OldCanRevokeRule(
                                new OldRole(usMatcher.group("role1")),
                                new OldRole(usMatcher.group("role2"))
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
                    OldRole admin = new OldRole(usMatcher.group("role1").trim());
                    OldRole assign = new OldRole(usMatcher.group("role2").trim());
                    Set<OldRole> preconditions = new HashSet<>();
                    Set<OldRole> negPrec = new HashSet<>();
                    String conditions = usMatcher.group("cond").trim();
                    if (!conditions.equals("TRUE")) {
                        for (String splitCondition : conditions.split("&")) {
                            if (splitCondition.trim().startsWith("-")) {
                                negPrec.add(new OldRole(splitCondition.trim().substring(1)));
                            } else {
                                preconditions.add(new OldRole(splitCondition.trim()));
                            }
                        }
                    }
                    this.canAssignRules.add(
                            new OldCanAssignRule(admin, preconditions, negPrec, assign)
                        );
                    /*this.canRevokeRules.add(
                            new CanRevokeRule(new Role(usMatcher.group("role1")),
                                    new Role(usMatcher.group("role2"))
                            )
                    );*/
                }
            }

            this.goalRole = new OldRole(matcher.group("goal").trim());
        } else {
            throw new IllegalArgumentException("File is not well-formed");
        }
    }
}
