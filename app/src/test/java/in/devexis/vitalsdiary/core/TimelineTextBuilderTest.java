package in.devexis.vitalsdiary.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public class TimelineTextBuilderTest {

    private static long millis(String iso) {
        return Instant.parse(iso).toEpochMilli();
    }

    @Test
    public void emptyCaseShowsNoEntriesAndDisclaimer() {
        HealthCase c = new HealthCase(1, "Test case", millis("2024-01-05T00:00:00Z"), List.of());
        String summary = TimelineTextBuilder.buildSummary(c, ZoneOffset.UTC);

        assertTrue(summary.contains("Case: Test case"));
        assertTrue(summary.contains("Started: Jan 5, 2024"));
        assertTrue(summary.contains("Not enough entries yet to show a trend"));
        assertTrue(summary.contains("No entries recorded yet."));
        assertTrue(summary.contains(TimelineTextBuilder.DISCLAIMER));
    }

    @Test
    public void entriesAreListedOldestFirstWithAttachmentTags() {
        SymptomEntry e1 = new SymptomEntry(1, millis("2024-01-06T00:00:00Z"), 7, "Started after rain", false, true);
        SymptomEntry e2 = new SymptomEntry(2, millis("2024-01-05T00:00:00Z"), 4, "", true, false);
        HealthCase c = new HealthCase(1, "Cough", millis("2024-01-05T00:00:00Z"), List.of(e1, e2));

        String summary = TimelineTextBuilder.buildSummary(c, ZoneOffset.UTC);

        int idxJan5 = summary.indexOf("Jan 5, 2024 | severity 4/10");
        int idxJan6 = summary.indexOf("Jan 6, 2024 | severity 7/10");
        assertTrue("entries should be sorted oldest first", idxJan5 >= 0 && idxJan6 > idxJan5);
        assertTrue(summary.contains("photo attached"));
        assertTrue(summary.contains("voice note attached"));
        assertTrue(summary.contains("Started after rain"));
    }

    @Test
    public void worseningTrendIsDescribed() {
        SymptomEntry e1 = new SymptomEntry(1, millis("2024-01-01T00:00:00Z"), 2, "", false, false);
        SymptomEntry e2 = new SymptomEntry(2, millis("2024-01-02T00:00:00Z"), 5, "", false, false);
        SymptomEntry e3 = new SymptomEntry(3, millis("2024-01-03T00:00:00Z"), 8, "", false, false);
        HealthCase c = new HealthCase(1, "Rash", millis("2024-01-01T00:00:00Z"), List.of(e1, e2, e3));

        String summary = TimelineTextBuilder.buildSummary(c, ZoneOffset.UTC);
        assertEquals(true, summary.contains("Symptoms appear to be worsening"));
    }
}
