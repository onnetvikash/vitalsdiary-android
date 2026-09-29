package in.devexis.vitalsdiary.core;

import java.util.List;

/**
 * Estimates whether a symptom timeline is trending better, worse, or holding steady,
 * using the slope of a simple least-squares regression of severity over entry order.
 * This is a data-organization aid, not a medical assessment.
 */
public final class TrendAnalyzer {

    public enum Trend { INSUFFICIENT_DATA, IMPROVING, WORSENING, STABLE }

    /** Slopes smaller than this (severity points per entry) are treated as noise, not a trend. */
    private static final double STABLE_THRESHOLD = 0.15;

    private TrendAnalyzer() {}

    public static Trend analyze(List<SymptomEntry> entriesSortedByTime) {
        if (entriesSortedByTime == null || entriesSortedByTime.size() < 2) {
            return Trend.INSUFFICIENT_DATA;
        }
        int n = entriesSortedByTime.size();
        double meanX = (n - 1) / 2.0;
        double meanY = 0;
        for (SymptomEntry e : entriesSortedByTime) {
            meanY += e.severity;
        }
        meanY /= n;

        double num = 0;
        double den = 0;
        for (int i = 0; i < n; i++) {
            double dx = i - meanX;
            double dy = entriesSortedByTime.get(i).severity - meanY;
            num += dx * dy;
            den += dx * dx;
        }
        double slope = den == 0 ? 0 : num / den;

        if (Math.abs(slope) < STABLE_THRESHOLD) {
            return Trend.STABLE;
        }
        return slope < 0 ? Trend.IMPROVING : Trend.WORSENING;
    }
}
