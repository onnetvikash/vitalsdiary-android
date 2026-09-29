package in.devexis.vitalsdiary.core;

/** The app's freemium rule: how many cases a non-premium user may create. */
public final class CaseLimitPolicy {

    public static final int FREE_CASE_LIMIT = 1;

    private CaseLimitPolicy() {}

    public static boolean canCreateNewCase(int currentCaseCount, boolean isPremium) {
        if (currentCaseCount < 0) {
            throw new IllegalArgumentException("Case count cannot be negative");
        }
        return isPremium || currentCaseCount < FREE_CASE_LIMIT;
    }
}
