package it.debsite.rr.test;

import org.jetbrains.annotations.NonNls;
import org.junit.jupiter.api.Assertions;

import java.util.Collection;

/**
 * Description.
 *
 * @author DeB
 * @version 1.0 2021-04-12
 * @since version date
 */
public class Utils {
    
    public static <T> void checkCollectionsEqual(
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
