package org.berendeev.roma.offchat.data.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;


public class DatabaseOpenHelper extends SQLiteOpenHelper implements BaseColumns{

    private static final String DATABASE_NAME = "offchat.db";
    private static final int DATABASE_VERSION = 1;

    //_ID | TIME | OWNER | TEXT | IMAGE
    public static final String CHAT_TABLE = "feeds";
    public static final String TIME = "time";
    public static final String OWNER = "description";
    public static final String TEXT = "title";
    public static final String IMAGE = "author";

    public DatabaseOpenHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        String script = "create table " + CHAT_TABLE + " (" +
                _ID + " integer primary key, " +
                TIME + " integer not null, " +
                TEXT + " text not null, " +
                OWNER + " text not null, " +
                IMAGE + " text not null);";
        db.execSQL(script);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CHAT_TABLE);
        onCreate(db);
    }

    @Override public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + CHAT_TABLE);
        onCreate(db);
    }
}
