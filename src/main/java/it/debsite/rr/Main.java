package it.debsite.rr;

import it.debsite.rr.arbac.ArbacInformation;
import it.debsite.rr.arbac.ArbacReader;
import it.debsite.rr.info.CanAssignRule;
import it.debsite.rr.info.CanRevokeRule;
import it.debsite.rr.info.Role;
import it.debsite.rr.info.User;
import it.debsite.rr.info.UserToRolesAssignment;
import it.debsite.rr.resolver.RoleReachabilityResolver;
import it.debsite.rr.slicing.BackwardSlicer;
import it.debsite.rr.slicing.ForwardSlicer;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-11
 * @since version date
 */
public class Main {

    public static void main(final String[] args) throws IOException {
        long tot = 0;
        // Loop over all the policies
        for (int i = 1; i <= 8; i++) {
            final long nano = System.nanoTime();
            // Read an parse the file
            final ArbacInformation information = ArbacReader.readAndParseFile(
                "policies/policy" + i + ".arbac"
            );
            
            // Apply pruning until no more information can be removed
            boolean toContinue;
            do {
                toContinue = ForwardSlicer.applyForwardSlicing(information);
                toContinue |= BackwardSlicer.applyBackwardSlicing(information);
            } while (toContinue);

            // Resolve the role reachability problem
            System.out.println(
                "Reachable: " + RoleReachabilityResolver.solveRoleReachabilityProblem(information)
            );
            tot += (System.nanoTime() - nano);
        }
        System.out.println("Total time: " + tot);
    }
}
