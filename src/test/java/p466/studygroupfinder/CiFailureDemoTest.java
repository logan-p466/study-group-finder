package p466.studygroupfinder;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CiFailureDemoTest {

    @Test
    void demonstrateCiFailure() {
        // Deliberately incorrect for CI demonstration.
        assertEquals(1, 2, "Intentional CI failure for Assignment 4");
    }
}
