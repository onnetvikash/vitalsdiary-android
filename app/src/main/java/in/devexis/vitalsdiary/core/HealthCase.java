package in.devexis.vitalsdiary.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A named health concern the user is tracking (e.g. "Persistent cough"), holding
 * its timeline of SymptomEntry objects, always kept sorted oldest-first.
 */
public final class HealthCase {
    public final long id;
    public final String title;
    public final long createdAtMillis;
    private final List<SymptomEntry> entries;

    public HealthCase(long id, String title, long createdAtMillis, List<SymptomEntry> entries) {
        this.id = id;
        this.title = title == null ? "" : title;
        this.createdAtMillis = createdAtMillis;
        List<SymptomEntry> sorted = new ArrayList<>(entries == null ? Collections.emptyList() : entries);
        sorted.sort(Comparator.comparingLong(e -> e.timestampMillis));
        this.entries = Collections.unmodifiableList(sorted);
    }

    public List<SymptomEntry> entries() {
        return entries;
    }
}
