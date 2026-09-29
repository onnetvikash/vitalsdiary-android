package in.devexis.vitalsdiary.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import in.devexis.vitalsdiary.core.HealthCase;
import in.devexis.vitalsdiary.core.SymptomEntry;

/** Reads and writes HealthCase / SymptomEntry data to the local SQLite database. */
public class VitalsRepository {
    private final VitalsDbHelper dbHelper;

    public VitalsRepository(Context context) {
        this.dbHelper = new VitalsDbHelper(context);
    }

    public long createCase(String title) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("created_at", System.currentTimeMillis());
        return db.insert(VitalsDbHelper.TABLE_CASES, null, values);
    }

    public long addEntry(long caseId, int severity, String note, String photoPath, String audioPath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("case_id", caseId);
        values.put("timestamp", System.currentTimeMillis());
        values.put("severity", severity);
        values.put("note", note);
        values.put("photo_path", photoPath);
        values.put("audio_path", audioPath);
        return db.insert(VitalsDbHelper.TABLE_ENTRIES, null, values);
    }

    public int countCases() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + VitalsDbHelper.TABLE_CASES, null)) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    public List<HealthCase> getAllCases() {
        List<HealthCase> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id, title, created_at FROM " + VitalsDbHelper.TABLE_CASES + " ORDER BY created_at DESC",
                null)) {
            while (c.moveToNext()) {
                long id = c.getLong(0);
                String title = c.getString(1);
                long createdAt = c.getLong(2);
                result.add(new HealthCase(id, title, createdAt, getEntries(id)));
            }
        }
        return result;
    }

    public HealthCase getCase(long caseId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id, title, created_at FROM " + VitalsDbHelper.TABLE_CASES + " WHERE id = ?",
                new String[]{String.valueOf(caseId)})) {
            if (c.moveToFirst()) {
                return new HealthCase(c.getLong(0), c.getString(1), c.getLong(2), getEntries(caseId));
            }
        }
        return null;
    }

    private List<SymptomEntry> getEntries(long caseId) {
        List<SymptomEntry> entries = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT id, timestamp, severity, note, photo_path, audio_path FROM " + VitalsDbHelper.TABLE_ENTRIES +
                        " WHERE case_id = ? ORDER BY timestamp ASC",
                new String[]{String.valueOf(caseId)})) {
            while (c.moveToNext()) {
                entries.add(new SymptomEntry(
                        c.getLong(0),
                        c.getLong(1),
                        c.getInt(2),
                        c.getString(3),
                        c.getString(4) != null,
                        c.getString(5) != null));
            }
        }
        return entries;
    }

    public void deleteCase(long caseId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(VitalsDbHelper.TABLE_ENTRIES, "case_id = ?", new String[]{String.valueOf(caseId)});
        db.delete(VitalsDbHelper.TABLE_CASES, "id = ?", new String[]{String.valueOf(caseId)});
    }
}
