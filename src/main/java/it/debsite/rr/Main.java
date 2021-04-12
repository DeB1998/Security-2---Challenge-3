package it.debsite.rr;

import it.debsite.rr.arbac.ArbacInformation;
import it.debsite.rr.arbac.ArbacReader;
import it.debsite.rr.resolver.RoleReachabilityResolver;
import it.debsite.rr.slicing.BackwardSlicer;
import it.debsite.rr.slicing.ForwardSlicer;
import java.io.IOException;

/**
 * Main class that applies pruning and solves the role reachability problem for all the policies.
 *
 * @author Alessio De Biasi
 * @version 1.1 2021-04-12
 * @since 1.0 2021-04-11
 */
public final class Main {

    /**
     * Constructor that prevents this class from being instantiated.
     */
    private Main() {}

    /**
     * Launches the program.
     *
     * @param args List of command line arguments.
     * @throws IOException If some I/O errors occur.
     */
    public static void main(final String[] args) throws IOException {
        // Initialize the total time spent to solve all the problems
        long totalTime = 0;
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
            final boolean isReachable = RoleReachabilityResolver.solveRoleReachabilityProblem(
                information
            );
            System.out.println("Reachable: " + isReachable);
            totalTime += (System.nanoTime() - nano);
        }
        System.out.println("Total time: " + totalTime);
    }
}
