package it.debsite.rr.test;

import it.debsite.rr.file.ArbacInformation;
import it.debsite.rr.file.ArbacReader;
import it.debsite.rr.test.previous.OldArbacReader;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-12
 * @since version date
 */
@SuppressWarnings("CallToSuspiciousStringMethod")
public class ArbacReaderTest {

    @Test
    public void testReader() throws IOException {
        for (int i = 1; i <= 8; i++) {
            final OldArbacReader oldArbacReader = new OldArbacReader();
            final ArbacReader arbacReader = new ArbacReader();
            oldArbacReader.readFile("policies/policy" + i + ".arbac");
            final ArbacInformation information = arbacReader.readAndParseFile(
                "policies/policy" + i + ".arbac"
            );

            System.out.println("Checking policy " + i);

            // Test roles
            Utils.compareRolesSet(oldArbacReader.getRoles(), information.getRoles());
            // Test users
            Utils.compareUsersSet(oldArbacReader.getUsers(), information.getUsers());
            // Test user-to-roles assignments
            Utils.compareUserToRolesAssignments(
                oldArbacReader.getUserToRoleAssignments(),
                information.getUserToRoleAssignments()
            );
            Utils.compareCanAssignRulesList(
                oldArbacReader.getCanAssignRules(),
                information.getCanAssignRules()
            );
            
            Assertions.assertTrue(
                Utils.compareRoles(
                    oldArbacReader.getGoalRole(),
                    information.getGoalRole()
                ),
                "Target role mismatch"
            );
        }
    }

    
}
