package in.devexis.vitalsdiary.core;

/**
 * A single dated observation within a HealthCase. Immutable, no Android dependencies
 * so it can be unit tested directly on the JVM.
 */
public final class SymptomEntry {
    public final long id;
    public final long timestampMillis;
    /** 1 (mild) to 10 (severe). */
    public final int severity;
    public final String note;
    public final boolean hasPhoto;
    public final boolean hasVoiceNote;

    public SymptomEntry(long id, long timestampMillis, int severity, String note,
                         boolean hasPhoto, boolean hasVoiceNote) {
        if (severity < 1 || severity > 10) {
            throw new IllegalArgumentException("Severity must be between 1 and 10");
        }
        this.id = id;
        this.timestampMillis = timestampMillis;
        this.severity = severity;
        this.note = note == null ? "" : note;
        this.hasPhoto = hasPhoto;
        this.hasVoiceNote = hasVoiceNote;
    }
}
