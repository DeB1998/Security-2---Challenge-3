package it.debsite.rr.test;

import it.debsite.rr.arbac.ArbacInformation;
import it.debsite.rr.arbac.ArbacReader;
import it.debsite.rr.slicing.BackwardSlicer;
import it.debsite.rr.slicing.ForwardSlicer;
import it.debsite.rr.test.previous.OldArbacReader;
import it.debsite.rr.test.previous.OldBackwardSlicing;
import it.debsite.rr.test.previous.OldForwardSlicing;
import java.io.IOException;

import org.junit.jupiter.api.Test;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-12
 * @since version date
 */
public class SlicingTest {

    @Test
    void testSlicing1() throws IOException {
        SlicingTest.testSlicing(1);
    }

    @Test
    void testSlicing2() throws IOException {
        SlicingTest.testSlicing(2);
    }

    @Test
    void testSlicing3() throws IOException {
        SlicingTest.testSlicing(3);
    }

    @Test
    void testSlicing4() throws IOException {
        SlicingTest.testSlicing(4);
    }

    @Test
    void testSlicing5() throws IOException {
        SlicingTest.testSlicing(5);
    }

    @Test
    void testSlicing6() throws IOException {
        SlicingTest.testSlicing(6);
    }

    @Test
    void testSlicing7() throws IOException {
        SlicingTest.testSlicing(7);
    }

    @Test
    void testSlicing8() throws IOException {
        SlicingTest.testSlicing(8);
    }

    private static void testSlicing(final int i) throws IOException {
        
        final ArbacInformation information = ArbacReader.readAndParseFile(
            "policies/policy" + i + ".arbac"
        );
        
        boolean toContinue;
        

        final OldArbacReader oldArbacReader = new OldArbacReader();
        oldArbacReader.readFile("policies/policy" + i + ".arbac");
    
        System.out.println("eeee");
        long time = System.nanoTime();
        do {
            toContinue =
                OldForwardSlicing.applyForwardSlicing(
                    oldArbacReader.getUserToRoleAssignments(),
                    oldArbacReader.getCanAssignRules(),
                    oldArbacReader.getCanRevokeRules(),
                    oldArbacReader.getRoles()
                );
            toContinue |=
                OldBackwardSlicing.applyBackwardSlicing(
                    oldArbacReader.getGoalRole(),
                    oldArbacReader.getCanAssignRules(),
                    oldArbacReader.getCanRevokeRules(),
                    oldArbacReader.getRoles()
                );
        } while (toContinue);
        System.out.println("TIME: " + (System.nanoTime() - time));
        time = System.nanoTime();
        do {
            toContinue = ForwardSlicer.applyForwardSlicing(information);
            toContinue |= BackwardSlicer.applyBackwardSlicing(information);
            
        } while (toContinue);
    
        System.out.println("TIME: " + (System.nanoTime() - time));
        
        Utils.compareUsersSet(oldArbacReader.getUsers(), information.getUsers());
        Utils.compareRolesSet(oldArbacReader.getRoles(), information.getRoles());
        Utils.compareUserToRolesAssignments(
            oldArbacReader.getUserToRoleAssignments(),
            information.getUserToRoleAssignments()
        );
        Utils.compareCanAssignRulesList(
            oldArbacReader.getCanAssignRules(),
            information.getCanAssignRules()
        );
        Utils.compareCanRevokeRules(
            oldArbacReader.getCanRevokeRules(),
            information.getCanRevokeRules()
        );
        Utils.compareRoles(oldArbacReader.getGoalRole(), information.getGoalRole());
    }
}
