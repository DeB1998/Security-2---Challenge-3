package it.debsite.rr.test;

import it.debsite.rr.file.ArbacReader;
import it.debsite.rr.test.previous.OldArbacReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-12
 * @since version date
 */
public class ArbacReaderTest {

    @Test
    public void testReader() throws IOException {
        for (int i = 1; i <= 8; i++) {
            final OldArbacReader oldArbacReader = new OldArbacReader();
            final ArbacReader arbacReader = new ArbacReader();
            oldArbacReader.readFile("policies/policy" + i + ".arbac");
            arbacReader.readFile("policies/policy" + i + ".arbac");

            Utils.checkCollectionsEqual(oldArbacReader.getRoles(), arbacReader.getRoles(), "roles");
            Utils.checkCollectionsEqual(oldArbacReader.getUsers(), arbacReader.getUsers(), "users");
            Utils.checkCollectionsEqual(oldArbacReader.getUserToRoleAssignments(), arbacReader.getUserToRoleAssignments(), "user to role");
            Utils.checkCollectionsEqual(
                oldArbacReader.getCanAssignRules(),
                arbacReader.getCanAssignRules(),
                "can assign rules"
            );
            Utils.checkCollectionsEqual(oldArbacReader.getCanRevokeRules(), arbacReader.getCanRevokeRules(), "can revoke rules");
            Assertions.assertEquals(oldArbacReader.getGoalRole(), arbacReader.getGoalRole(),
                    "Target role mismatch");
        }
    }
}
