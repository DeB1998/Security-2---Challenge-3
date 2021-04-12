package it.debsite.rr;

import it.debsite.rr.file.ArbacReader;
import it.debsite.rr.slicing.BackwardSlicing;
import it.debsite.rr.slicing.ForwardSlicing;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
            arbacReader.readFile("policies/policy" + i + ".arbac");

            final Set<Role> roles = arbacReader.getRoles();
            final Set<User> users = arbacReader.getUsers();
            final List<UserToRoleAssignment> userToRoleAssignments = arbacReader.getUserToRoleAssignments();
            final List<CanAssignRule> canAssignRules = arbacReader.getCanAssignRules();
            final List<CanRevokeRule> canRevokeRules = arbacReader.getCanRevokeRules();
            
           // print(arbacReader);
            
            boolean toContinue;
            do {
                toContinue =
                    Main.applyForwardSlicing(
                        userToRoleAssignments,
                        canAssignRules,
                        canRevokeRules,
                        roles
                    );
                toContinue |=
                    Main.applyBackwardSlicing(
                        arbacReader.getGoalRole(),
                        canAssignRules,
                        canRevokeRules,
                        roles
                    );
            } while (toContinue);

            GraphNode initialNode = new GraphNode(userToRoleAssignments);
    
            System.out.println("REAHING: "+ reaching(
                    initialNode,
                    new HashSet<>(),
                    canAssignRules,
                    canRevokeRules,
                    arbacReader.getGoalRole()
            ));
            tot += (System.nanoTime() - nano);
        //}
        System.out.println("TOT: " + tot);
        System.out.println("sdsdsdsd");
    }

    private static boolean reaching(
        final GraphNode nodeToExplore,
        final Set<GraphNode> exploredNodes,
        final List<CanAssignRule> canAssignRules,
        final List<CanRevokeRule> canRevokeRules,
        final Role goalRole
    ) {
        if (exploredNodes.contains(nodeToExplore)) {
            return false;
        }
        for (UserToRoleAssignment assignment: nodeToExplore.getAssignments()) {
            if (assignment.getRoles().contains(goalRole)) {
                return true;
            }
        }
        exploredNodes.add(nodeToExplore);

        // USER -> {r1, r2, r3}
        for (final UserToRoleAssignment assignment : nodeToExplore.getAssignments()) {
            for (final CanAssignRule rule : canAssignRules) {
                if (assignment.getRoles().contains(rule.getAdministrativeRole())) {
                    int i = 0;
                    for (final UserToRoleAssignment otherAssignments : nodeToExplore.getAssignments()) {
                        if (
                            otherAssignments.getRoles().containsAll(rule.getPreconditions()) &&
                            Collections.disjoint(
                                otherAssignments.getRoles(),
                                rule.getNegativePreconditions()
                            )
                        ) {
                            final UserToRoleAssignment newAssignment = new UserToRoleAssignment(
                                otherAssignments
                            );
                            newAssignment.getRoles().add(rule.getRoleToAssign());
                            final List<UserToRoleAssignment> newAssignments = new ArrayList<>(
                                nodeToExplore.getAssignments()
                            );
                            newAssignments.set(i, newAssignment);
                            final GraphNode newNode = new GraphNode(newAssignments);
                            if (Main.reaching(
                                newNode,
                                exploredNodes,
                                canAssignRules,
                                canRevokeRules,
                                goalRole
                            )) {
                                return true;
                            }
                        }
                        i++;
                    }
                }
            }
            for (final CanRevokeRule rule: canRevokeRules) {
                if (assignment.getRoles().contains(rule.getAdministrativeRole())) {
                    int i = 0;
                    for (final UserToRoleAssignment otherAssignments : nodeToExplore.getAssignments()) {
                        if (otherAssignments.getRoles().contains(rule.getRoleToRevoke())) {
                            final UserToRoleAssignment newAssignment = new UserToRoleAssignment(
                                    otherAssignments
                            );
                            newAssignment.getRoles().remove(rule.getRoleToRevoke());
                            final List<UserToRoleAssignment> newAssignments = new ArrayList<>(
                                    nodeToExplore.getAssignments()
                            );
                            newAssignments.set(i, newAssignment);
                            final GraphNode newNode = new GraphNode(newAssignments);
                            if (Main.reaching(
                                    newNode,
                                    exploredNodes,
                                    canAssignRules,
                                    canRevokeRules,
                                    goalRole
                            )) {
                                return true;
                            }
                        }
                        i++;
                    }
                }
            }
        }

        return false;
    }

    private static boolean applyForwardSlicing(
        final List<? extends UserToRoleAssignment> userToRoleAssignments,
        final List<CanAssignRule> canAssignRules,
        final List<? extends CanRevokeRule> canRevokeRules,
        final Set<Role> roles
    ) {
        final Set<Role> fixpoint = ForwardSlicing.compute(
            userToRoleAssignments,
            canAssignRules,
            roles
        );

        // delete the roles R \ S
        final Set<Role> rMinusS = new HashSet<>(roles);
        rMinusS.removeAll(fixpoint);
        boolean changed = roles.retainAll(fixpoint); // R- (R-S)

        // remove from CA:
        // - all the rules that include any role in R \ S* in:
        //     - the positive preconditions
        //     - in the target
        final Iterator<CanAssignRule> iterator = canAssignRules.iterator();
        while (iterator.hasNext()) {
            final CanAssignRule rule = iterator.next();
            boolean toRemove = false;
            for (final Role role : rule.getPreconditions()) {
                if (rMinusS.contains(role)) {
                    toRemove = true;
                    break;
                }
            }
            toRemove = toRemove || rMinusS.contains(rule.getRoleToAssign());
            if (toRemove) {
                iterator.remove();
                changed = true;
            } else {
                // remove the roles R \ S* from the negative preconditions of all rules
                changed |= rule.getNegativePreconditions().removeIf(rMinusS::contains);
            }
        }

        // remove from CR all the rules that mention any role in R \ S*
        changed |= canRevokeRules.removeIf(role -> rMinusS.contains(role.getRoleToRevoke()));

        return changed;
    }

    private static boolean applyBackwardSlicing(
        final Role goalRole,
        final List<CanAssignRule> canAssignRules,
        final List<? extends CanRevokeRule> canRevokeRules,
        final Set<Role> roles
    ) {
        final Set<Role> fixpoint = BackwardSlicing.compute(goalRole, canAssignRules);

        // delete the roles R \ S
        final Set<Role> rMinusS = new HashSet<>(roles);
        rMinusS.removeAll(fixpoint);
        boolean changed = roles.retainAll(fixpoint);

        // remove from CA all the rules that assign a role in R \ S
        changed |= canAssignRules.removeIf(value -> rMinusS.contains(value.getRoleToAssign()));
        changed |= canRevokeRules.removeIf(value -> rMinusS.contains(value.getRoleToRevoke()));

        return changed;
    }
    
    
}
