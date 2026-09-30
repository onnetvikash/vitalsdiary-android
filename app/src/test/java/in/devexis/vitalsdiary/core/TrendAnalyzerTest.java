package in.devexis.vitalsdiary.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TrendAnalyzerTest {

    private static SymptomEntry entry(long day, int severity) {
        return new SymptomEntry(day, day * 86_400_000L, severity, "", false, false);
    }

    @Test
    public void emptyListIsInsufficient() {
        assertEquals(TrendAnalyzer.Trend.INSUFFICIENT_DATA, TrendAnalyzer.analyze(new ArrayList<>()));
    }

    @Test
    public void singleEntryIsInsufficient() {
        List<SymptomEntry> entries = List.of(entry(1, 5));
        assertEquals(TrendAnalyzer.Trend.INSUFFICIENT_DATA, TrendAnalyzer.analyze(entries));
    }

    @Test
    public void nullIsInsufficient() {
        assertEquals(TrendAnalyzer.Trend.INSUFFICIENT_DATA, TrendAnalyzer.analyze(null));
    }

    @Test
    public void clearlyDecreasingSeverityIsImproving() {
        List<SymptomEntry> entries = List.of(entry(1, 8), entry(2, 6), entry(3, 4), entry(4, 2));
        assertEquals(TrendAnalyzer.Trend.IMPROVING, TrendAnalyzer.analyze(entries));
    }

    @Test
    public void clearlyIncreasingSeverityIsWorsening() {
        List<SymptomEntry> entries = List.of(entry(1, 2), entry(2, 4), entry(3, 6), entry(4, 8));
        assertEquals(TrendAnalyzer.Trend.WORSENING, TrendAnalyzer.analyze(entries));
    }

    @Test
    public void flatSeverityIsStable() {
        List<SymptomEntry> entries = List.of(entry(1, 5), entry(2, 5), entry(3, 5), entry(4, 5));
        assertEquals(TrendAnalyzer.Trend.STABLE, TrendAnalyzer.analyze(entries));
    }

    @Test
    public void smallNoiseIsStableNotWorsening() {
        List<SymptomEntry> entries = List.of(entry(1, 5), entry(2, 5), entry(3, 6), entry(4, 5));
        assertEquals(TrendAnalyzer.Trend.STABLE, TrendAnalyzer.analyze(entries));
    }

    @Test
    public void twoEntriesDecreasingIsImproving() {
        List<SymptomEntry> entries = List.of(entry(1, 7), entry(2, 3));
        assertEquals(TrendAnalyzer.Trend.IMPROVING, TrendAnalyzer.analyze(entries));
    }
}
