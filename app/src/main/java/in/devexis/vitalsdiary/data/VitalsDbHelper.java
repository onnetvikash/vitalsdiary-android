package in.devexis.vitalsdiary.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/** Local-only SQLite storage. Nothing here is ever sent off the device. */
public class VitalsDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "vitals_diary.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_CASES = "health_case";
    public static final String TABLE_ENTRIES = "symptom_entry";

    public VitalsDbHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CASES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "created_at INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_ENTRIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "case_id INTEGER NOT NULL," +
                "timestamp INTEGER NOT NULL," +
                "severity INTEGER NOT NULL," +
                "note TEXT," +
                "photo_path TEXT," +
                "audio_path TEXT," +
                "FOREIGN KEY(case_id) REFERENCES " + TABLE_CASES + "(id) ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CASES);
        onCreate(db);
    }
}
