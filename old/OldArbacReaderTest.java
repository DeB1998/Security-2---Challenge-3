package it.debsite.rr.test;

import it.debsite.rr.CanAssignRule;
import it.debsite.rr.CanRevokeRule;
import it.debsite.rr.Role;
import it.debsite.rr.User;
import it.debsite.rr.UserToRoleAssignment;
import it.debsite.rr.file.ArbacReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NonNls;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-12
 * @since version date
 */
class OldArbacReaderTest {

    private static final Pattern USER_TO_ROLE_PATTERN = Pattern.compile(
        "[(](?<user>[^,]*), \\[(?<roles>[^]]*)][)]?"
    );

    // (Admin, [PrimaryDoctor, Manager], [], target)
    private static final Pattern CAN_ASSIGN_RULE_PATTERN = Pattern.compile(
        "[(](?<admin>[^,]*), \\[(?<preconditions>[^]]*)], \\[(?<negative>[^]]*)], (?<target>[^,)]*)[)]?"
    );
    
    private static final Pattern CAN_REVOKE_RULE_PATTERN = Pattern.compile(
            "[(](?<admin>[^,]*), (?<target>[^)]*)[)]?"
    );

    @Test
    void testArbacReaderPolicy1() throws IOException {
        OldArbacReaderTest.testPolicy(
            1,
            """
            [Agent, PrimaryDoctor, Manager, Admin, MedicalTeam, Patient, Receptionist, ReferredDoctor,
             Doctor, target, Employee, MedicalManager, Nurse, ThirdParty, PatientWithTPC]
             """,
            "[user6, user7, user4, user5, user2, user3, user0, user1, user8, user9]",
            """
                [(user0, [Admin]), (user1, [Doctor]), (user2, [Doctor]), (user3, [Nurse]),
                (user4, [Nurse]), (user5, [PrimaryDoctor, Doctor]), (user6, [Manager]),
                (user7, [Patient]), (user8, [Patient]), (user9, [Employee, Receptionist])]
                """,
            """
                [(Admin, [PrimaryDoctor, Manager], [], target), (Doctor, [], [], ThirdParty),
                (Manager, [], [], Employee), (Manager, [], [], MedicalManager),
                (Patient, [], [], Agent), (Doctor, [Doctor], [], ReferredDoctor),
                (MedicalManager, [Doctor], [], MedicalTeam),
                (MedicalManager, [Nurse], [], MedicalTeam), (Manager, [], [Doctor], Receptionist),
                (Manager, [], [Receptionist], Doctor),
                (Patient, [Doctor], [Patient], PrimaryDoctor),
                (Receptionist, [], [PrimaryDoctor], Patient),
                (ThirdParty, [Patient], [], PatientWithTPC)]
                """,
            """
                [(Doctor, ThirdParty), (Doctor, ReferredDoctor), (MedicalManager, MedicalTeam),
                (Manager, Employee), (Manager, MedicalManager)]
                """,
            "target"
        );
        final ArbacReader arbacReader = new ArbacReader();
        arbacReader.readFile("policies/policy1.arbac");
        //final Iterable<CanAssignRule> canAssignRules = new ArrayList<>();
        //Assertions.assertIterableEquals(canAssignRules, arbacReader.getCanAssignRules());
        // ROL [Agent, PrimaryDoctor, Manager, Admin, MedicalTeam, Patient, Receptionist, ReferredDoctor, Doctor, target, Employee, MedicalManager, Nurse, ThirdParty, PatientWithTPC]
        // US: [user6, user7, user4, user5, user2, user3, user0, user1, user8, user9]
        // UA: [(user0, [Admin]), (user1, [Doctor]), (user2, [Doctor]), (user3, [Nurse]), (user4, [Nurse]), (user5, [PrimaryDoctor, Doctor]), (user6, [Manager]), (user7, [Patient]), (user8, [Patient]), (user9, [Employee, Receptionist])]
        // CA: [(Admin, [PrimaryDoctor, Manager], [], target), (Doctor, [], [], ThirdParty), (Manager, [], [], Employee), (Manager, [], [], MedicalManager), (Patient, [], [], Agent), (Doctor, [Doctor], [], ReferredDoctor), (MedicalManager, [Doctor], [], MedicalTeam), (MedicalManager, [Nurse], [], MedicalTeam), (Manager, [], [Doctor], Receptionist), (Manager, [], [Receptionist], Doctor), (Patient, [Doctor], [Patient], PrimaryDoctor), (Receptionist, [], [PrimaryDoctor], Patient), (ThirdParty, [Patient], [], PatientWithTPC)]

        // CR: [(Doctor, ThirdParty), (Doctor, ReferredDoctor), (MedicalManager, MedicalTeam), (Manager, Employee), (Manager, MedicalManager)]
        
        // target
    }

    private static void testPolicy(
        final int policyIndex,
        final String rolesString,
        final String usersString,
        final String userToRoleString,
        final String canAssignRulesString,
        final String canRevokeRulesString,
        final String targetRoleString
    ) throws IOException {
        @NonNls
        final ArbacReader arbacReader = new ArbacReader();
        arbacReader.readFile("policies/policy" + policyIndex + ".arbac");

        final Collection<Role> expectedRoles = OldArbacReaderTest.extractInformation(
            rolesString,
            ",",
            Role::new
        );
        OldArbacReaderTest.checkCollectionsEqual(expectedRoles, arbacReader.getRoles(), "roles");

        final Collection<User> expectedUsers = OldArbacReaderTest.extractInformation(
            usersString,
            ",",
            User::new
        );

        OldArbacReaderTest.checkCollectionsEqual(expectedUsers, arbacReader.getUsers(), "users");

        final Collection<UserToRoleAssignment> expectedUserToRoles = OldArbacReaderTest.extractInformation(
            userToRoleString,
            "\\),",
            OldArbacReaderTest::extractUserToRoleAssignment
        );

        OldArbacReaderTest.checkCollectionsEqual(
            expectedUserToRoles,
            arbacReader.getUserToRoleAssignments(),
            "user to roles"
        );

        final Collection<CanAssignRule> expectedCanAssignRules = OldArbacReaderTest.extractInformation(
            canAssignRulesString,
            "\\),",
            OldArbacReaderTest::extractCanAssignRule
        );

        OldArbacReaderTest.checkCollectionsEqual(
            expectedCanAssignRules,
            arbacReader.getCanAssignRules(),
            "can assign rules"
        );
    
        final Collection<CanRevokeRule> expectedCanRevokeRules = OldArbacReaderTest.extractInformation(
                canRevokeRulesString,
                "\\),",
                OldArbacReaderTest::extractCanRevoke
        );
    
        OldArbacReaderTest.checkCollectionsEqual(
                expectedCanRevokeRules,
                arbacReader.getCanRevokeRules(),
                "can assign rules"
        );

        Assertions.assertTrue(
            new Role(targetRoleString).equals(arbacReader.getGoalRole()),
            "Target role mismatch"
        );
        //final Iterable<CanAssignRule> canAssignRules = new ArrayList<>();
        //Assertions.assertIterableEquals(canAssignRules, arbacReader.getCanAssignRules());
    }

    private static <T> Collection<T> extractInformation(
        final String toStringResult,
        final String split,
        final Function<? super String, ? extends T> creator
    ) {
        final Collection<T> result = new HashSet<>(20);
        final String[] information = toStringResult
            .trim()
            .substring(1, toStringResult.trim().length() - 1)
            .split(split);
        for (final String singleInformation : information) {
            result.add(creator.apply(singleInformation.trim()));
        }

        return result;
    }

    private static UserToRoleAssignment extractUserToRoleAssignment(
            final String userToRoleAssignmentString
    ) {
        // (user5, [PrimaryDoctor, Doctor])
        final Matcher matcher = OldArbacReaderTest.USER_TO_ROLE_PATTERN.matcher(userToRoleAssignmentString);
        Assertions.assertTrue(
            matcher.matches(),
            () -> "Malformed input for " + userToRoleAssignmentString
        );
        if (matcher.matches()) {
            final String userName = matcher.group("user").trim();
            final String rolesString = matcher.group("roles").trim();
            final String[] rolesName = rolesString.split(",");
            final Set<Role> roles = new HashSet<>();
            for (final String role : rolesName) {
                roles.add(new Role(role.trim()));
            }
            return new UserToRoleAssignment(new User(userName), roles);
        }
        throw new IllegalStateException("This cannot happen");
    }

    private static CanAssignRule extractCanAssignRule(final String canAssignRuleString) {
        //(Admin, [PrimaryDoctor, Manager], [], target)
        // "[(](?<admin>[^,]*), \\[(?<preconditions>[^]]*)], \\[(?<negative>[^]]*)], (?<target>[^,]*)[)]?"
        final Matcher matcher = OldArbacReaderTest.CAN_ASSIGN_RULE_PATTERN.matcher(canAssignRuleString);
        Assertions.assertTrue(
            matcher.matches(),
            () -> "Malformed input for " + canAssignRuleString
        );
        if (matcher.matches()) {
            final String admin = matcher.group("admin").trim();
            final String target = matcher.group("target").trim();
            final String preconditionsString = matcher.group("preconditions").trim();
            final String negativeString = matcher.group("negative").trim();

            final Set<Role> preconditions = new HashSet<>();
            final Set<Role> negativePeconditions = new HashSet<>();

            if (!preconditionsString.isEmpty()) {
                final String[] preconditionsSplit = preconditionsString.split(",");
                for (final String singlePreconditionsSplit : preconditionsSplit) {
                    preconditions.add(new Role(singlePreconditionsSplit.trim()));
                }
            }

            if (!negativeString.isEmpty()) {
                final String[] negativeSplit = negativeString.split(",");
                for (final String singleNegativeSplit : negativeSplit) {
                    negativePeconditions.add(new Role(singleNegativeSplit.trim()));
                }
            }

            return new CanAssignRule(
                new Role(admin),
                preconditions,
                negativePeconditions,
                new Role(target)
            );
        }
        throw new IllegalStateException("This cannot happen");
    }

    private static CanRevokeRule extractCanRevoke(final String canRevokeString) {
        
        // (Doctor, ThirdParty)
        final Matcher matcher = OldArbacReaderTest.CAN_REVOKE_RULE_PATTERN.matcher(canRevokeString);
        Assertions.assertTrue(
                matcher.matches(),
                () -> "Malformed input for " + canRevokeString
        );
        if (matcher.matches()) {
            final String admin = matcher.group("admin").trim();
            final String target = matcher.group("target").trim();
            
            return new CanRevokeRule(new Role(admin), new Role(target));
        }
        
        throw new IllegalStateException("This cannot happen");
    }
    
    private static <T> void checkCollectionsEqual(
        final Collection<? extends T> firstCollection,
        final Collection<T> secondCollection,
        final @NonNls String message
    ) {
        Assertions.assertEquals(
            firstCollection.size(),
            secondCollection.size(),
            () -> "Error in checking " + message + ": The two collections differs in size."
        );

        for (final T firstCollectionElement : firstCollection) {
            Assertions.assertTrue(
                secondCollection.contains(firstCollectionElement),
                () ->
                    "Error in checking " +
                    message +
                    ": Element '" +
                    firstCollectionElement +
                    "' is not contained in second collection"
            );
        }
    }
}
