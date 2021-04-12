package it.debsite.rr.test;

import it.debsite.rr.arbac.ArbacInformation;
import it.debsite.rr.arbac.ArbacReader;
import it.debsite.rr.resolver.RoleReachabilityResolver;
import it.debsite.rr.slicing.BackwardSlicer;
import it.debsite.rr.slicing.ForwardSlicer;
import it.debsite.rr.test.previous.OldArbacReader;
import it.debsite.rr.test.previous.OldBackwardSlicing;
import it.debsite.rr.test.previous.OldForwardSlicing;
import it.debsite.rr.test.previous.OldGraphNode;
import it.debsite.rr.test.previous.OldReaching;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.HashSet;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-12
 * @since version date
 */
public class ReachingTest {
    
    public static void main(String[] args) throws IOException {
        
        ReachingTest.checkReachability(1);
        ReachingTest.checkReachability(2);
        ReachingTest.checkReachability(3);
        ReachingTest.checkReachability(4);
        ReachingTest.checkReachability(5);
        ReachingTest.checkReachability(6);
        ReachingTest.checkReachability(7);
        ReachingTest.checkReachability(8);
    }
    
    private static void checkReachability(final int i) throws IOException {
    
       
        final ArbacInformation information = ArbacReader.readAndParseFile(
                "policies/policy" + i + ".arbac"
        );
        boolean toContinue;
        
        long time = System.nanoTime();
        
        final OldArbacReader oldArbacReader = new OldArbacReader();
        oldArbacReader.readFile("policies/policy" + i + ".arbac");
        
        /*OLD*/
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
        final OldGraphNode initialNode = new OldGraphNode(oldArbacReader.getUserToRoleAssignments());
    
        final boolean second = OldReaching.reaching(
                initialNode,
                new HashSet<>(),
                oldArbacReader.getCanAssignRules(),
                oldArbacReader.getCanRevokeRules(),
                oldArbacReader.getGoalRole()
        );
        System.out.println("OLD: " + second + " ELAPS: " + (System.nanoTime() - time));
    
        time = System.nanoTime();
        
        do {
            toContinue = ForwardSlicer.applyForwardSlicing(information);
            toContinue |= BackwardSlicer.applyBackwardSlicing(information);
        
        } while (toContinue);
    
        final boolean first = RoleReachabilityResolver.solveRoleReachabilityProblem(information);
        System.out.println("NEW: " + first + " ELAPS: " + (System.nanoTime() - time));
        System.out.println();
        Assertions.assertEquals(first, second, "Reaching different");
    }
}
