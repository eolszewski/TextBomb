package com.ericolszewski.smsbomb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ericolszewski on 5/30/15.
 */
public class DatabaseAdapter {

    private static final String TAG = "DatabaseAdapter";

    //region Database Columns
    public static final String KEY_ROWID = "_id";
    public static final String KEY_TEXT  = "text";
    public static final String KEY_DATE = "date";
    public static final String KEY_RECIPIENTS = "recipients";
    public static final String KEY_FREQUENCY = "frequency";

    public static final int COL_ROWID = 0;
    public static final int COL_TEXT = 1;
    public static final int COL_DATE = 2;
    public static final int COL_RECIPIENTS = 3;
    public static final int COL_FREQUENCY = 4;

    public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_TEXT, KEY_DATE, KEY_RECIPIENTS, KEY_FREQUENCY};
    //endregion

    //region Database Information
    public static final String DATABASE_NAME = "textBomb";
    public static final String DATABASE_TABLE = "messagesTable";
    public static final int DATABASE_VERSION = 1;
    //endregion

    //region Database Stuff
    private static final String DATABASE_CREATE_SQL =
            "create table " + DATABASE_TABLE
                    + " (" + KEY_ROWID + " integer primary key autoincrement, "
                    + KEY_TEXT + " text not null, "
                    + KEY_DATE + " text not null, "
                    + KEY_RECIPIENTS + " text not null, "
                    + KEY_FREQUENCY + " text not null"
                    + ");";

    private Context context;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    //endregion

    //region Public Methods
    public DatabaseAdapter(Context ctx) {
        this.context = ctx;
        databaseHelper = new DatabaseHelper(context);
    }

    // Open the database connection.
    public DatabaseAdapter open() {
        db = databaseHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void close() {
        databaseHelper.close();
    }

    // Add a new set of values to the database.
    public long insertRow(String text, String date, String recipients, String frequency) {
        // Create row's data:
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TEXT, text);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_RECIPIENTS, recipients);
        initialValues.put(KEY_FREQUENCY, frequency);

        // Insert it into the database.
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    // Delete a row from the database, by rowId (primary key)
    public boolean deleteRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        return db.delete(DATABASE_TABLE, where, null) != 0;
    }

    public void deleteAll() {
        Cursor c = getAllRows();
        long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
        if (c.moveToFirst()) {
            do {
                deleteRow(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }

    // Return all data in the database.
    public Cursor getAllRows() {
        String where = null;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Get a specific row (by rowId)
    public Cursor getRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Change an existing row to be equal to new data.
    public boolean updateRow(long rowId, String text, String date, String recipients, String frequency) {
        String where = KEY_ROWID + "=" + rowId;
        // Create row's data:
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_TEXT, text);
        newValues.put(KEY_DATE, date);
        newValues.put(KEY_RECIPIENTS, recipients);
        newValues.put(KEY_FREQUENCY, frequency);

        // Insert it into the database.
        return db.update(DATABASE_TABLE, newValues, where, null) != 0;
    }
    //endregion

    /**
     * Private class which handles database creation and upgrading.
     * Used to handle low-level database access.
     */
    //region DatabaseHelper Class
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DATABASE_CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");

            // Destroy old database
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);

            // Recreate new database
            onCreate(_db);
        }
    }
    //endregion
}
