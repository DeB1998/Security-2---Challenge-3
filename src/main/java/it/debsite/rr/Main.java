package it.debsite.rr;

import it.debsite.rr.file.ArbacInformation;
import it.debsite.rr.file.ArbacReader;
import it.debsite.rr.info.CanAssignRule;
import it.debsite.rr.info.CanRevokeRule;
import it.debsite.rr.info.Role;
import it.debsite.rr.info.User;
import it.debsite.rr.info.UserToRolesAssignment;
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

        long tot = 0;
        //for (int i = 1; i <= 8; i++) {
        int i = 8;
        final long nano = System.nanoTime();
        final ArbacReader arbacReader = new ArbacReader();
        final ArbacInformation information = arbacReader.readAndParseFile(
            "policies/policy" + i + ".arbac"
        );

        final Set<Role> roles = information.getRoles();
        final Set<User> users = information.getUsers();
        final List<UserToRolesAssignment> userToRoleAssignments = information.getUserToRoleAssignments();
        final List<CanAssignRule> canAssignRules = information.getCanAssignRules();
        final List<CanRevokeRule> canRevokeRules = information.getCanRevokeRules();

        // print(arbacReader);

        boolean toContinue;
        do {
            toContinue = ForwardSlicer.applyForwardSlicing(information);
            toContinue |= BackwardSlicer.applyBackwardSlicing(information);
        } while (toContinue);
    
        System.out.println("Reaching: ");
        tot += (System.nanoTime() - nano);
        //}
        System.out.println("TOT: " + tot);
        System.out.println("sdsdsdsd");
    }

    
}
