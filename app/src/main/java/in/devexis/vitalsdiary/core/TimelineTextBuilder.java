package in.devexis.vitalsdiary.core;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Turns a HealthCase into a plain-text timeline summary suitable for a doctor visit
 * (and for rendering into the exported PDF). Pure text formatting, no Android dependency.
 */
public final class TimelineTextBuilder {

    public static final String DISCLAIMER =
            "This summary is for sharing with a licensed healthcare professional. " +
            "It is not a diagnosis and VitalsDiary does not assess or predict any medical condition.";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);

    private TimelineTextBuilder() {}

    public static String buildSummary(HealthCase healthCase, ZoneId zone) {
        StringBuilder sb = new StringBuilder();
        sb.append("VitalsDiary summary\n");
        sb.append("Case: ").append(healthCase.title).append('\n');
        sb.append("Started: ").append(formatDate(healthCase.createdAtMillis, zone)).append('\n');

        TrendAnalyzer.Trend trend = TrendAnalyzer.analyze(healthCase.entries());
        sb.append("Trend: ").append(describeTrend(trend)).append('\n');
        sb.append('\n');

        List<SymptomEntry> entries = healthCase.entries();
        if (entries.isEmpty()) {
            sb.append("No entries recorded yet.\n");
        } else {
            for (SymptomEntry e : entries) {
                sb.append("- ").append(formatDate(e.timestampMillis, zone))
                  .append(" | severity ").append(e.severity).append("/10");
                if (e.hasPhoto) {
                    sb.append(" | photo attached");
                }
                if (e.hasVoiceNote) {
                    sb.append(" | voice note attached");
                }
                sb.append('\n');
                if (!e.note.isEmpty()) {
                    sb.append("    ").append(e.note).append('\n');
                }
            }
        }

        sb.append('\n').append(DISCLAIMER);
        return sb.toString();
    }

    private static String describeTrend(TrendAnalyzer.Trend trend) {
        switch (trend) {
            case IMPROVING:
                return "Symptoms appear to be improving";
            case WORSENING:
                return "Symptoms appear to be worsening";
            case STABLE:
                return "Symptoms appear stable";
            default:
                return "Not enough entries yet to show a trend";
        }
    }

    private static String formatDate(long millis, ZoneId zone) {
        return DATE_FORMAT.format(Instant.ofEpochMilli(millis).atZone(zone));
    }
}
