package dbox.praseem.com.testapp.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by shanks on 3/9/2015.
 */
public class DatabaseAdapter {

    VivzHelper vivzHelper;
    Context context;
    String meaning = "";


    public DatabaseAdapter(Context context) {
        vivzHelper = new VivzHelper(context);
    }

    public long insert(String word, String mening) {
        SQLiteDatabase sqLiteDatabase = vivzHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(VivzHelper.NAME, word);
        contentValues.put(VivzHelper.MEANING, mening);
        long id = sqLiteDatabase.insert(VivzHelper.TABLE_NAME, null, contentValues);

        return id;

    }

    public String getallData() {
        SQLiteDatabase sqLiteDatabase = vivzHelper.getWritableDatabase();
        String[] coloumn = {VivzHelper.UID, VivzHelper.NAME, VivzHelper.MEANING};
        Cursor cursor = sqLiteDatabase.query(VivzHelper.TABLE_NAME, coloumn, null, null, null, null, null);
        StringBuffer stringBuffer = new StringBuffer();
        while (cursor.moveToNext()) {
            int index1 = cursor.getColumnIndex(VivzHelper.UID);
            int index2 = cursor.getColumnIndex(VivzHelper.NAME);
            int index3 = cursor.getColumnIndex(VivzHelper.MEANING);
            int index = cursor.getInt(index1);
            String word = cursor.getString(index2);
            String meaning = cursor.getString(index3);
            stringBuffer.append(index + "  " + word + "   " + meaning + "\n");

        }
        return stringBuffer.toString();
    }

    public String getLocations(String fileName) {
        SQLiteDatabase sqLiteDatabase = vivzHelper.getWritableDatabase();
        String[] coloumn = {VivzHelper.MEANING};
        Cursor cursor = sqLiteDatabase.query(VivzHelper.TABLE_NAME, coloumn, VivzHelper.NAME + " =  '" + fileName + "'", null, null, null, null);
        StringBuffer stringBuffer = new StringBuffer();
        while (cursor.moveToNext()) {

            int index3 = cursor.getColumnIndex(VivzHelper.MEANING);

            meaning = cursor.getString(index3);

        }
        return meaning;
    }

    public class VivzHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "vivzdatabase";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_NAME = "VIVZTABLE";
        private static final String UID = "_ID";
        private static final String NAME = "NAME";
        private static final String MEANING = "Meaning";
        private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME + " VARCHAR(255), " + MEANING + " VARCHAR(255));";
        private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;


        public VivzHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_TABLE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

            sqLiteDatabase.execSQL(DROP_TABLE);
            onCreate(sqLiteDatabase);
        }
    }
}
