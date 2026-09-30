package in.devexis.vitalsdiary.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class CaseLimitPolicyTest {

    @Test
    public void freeUserCanCreateFirstCase() {
        assertEquals(true, CaseLimitPolicy.canCreateNewCase(0, false));
    }

    @Test
    public void freeUserBlockedAtLimit() {
        assertEquals(false, CaseLimitPolicy.canCreateNewCase(1, false));
    }

    @Test
    public void freeUserBlockedWellBeyondLimit() {
        assertEquals(false, CaseLimitPolicy.canCreateNewCase(5, false));
    }

    @Test
    public void premiumUserNeverBlocked() {
        assertEquals(true, CaseLimitPolicy.canCreateNewCase(0, true));
        assertEquals(true, CaseLimitPolicy.canCreateNewCase(1, true));
        assertEquals(true, CaseLimitPolicy.canCreateNewCase(100, true));
    }

    @Test
    public void negativeCountRejected() {
        assertThrows(IllegalArgumentException.class, () -> CaseLimitPolicy.canCreateNewCase(-1, false));
    }
}
