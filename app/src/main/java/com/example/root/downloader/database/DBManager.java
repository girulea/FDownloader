package com.example.root.downloader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by root on 21/11/16.
 */

public class DBManager extends SQLiteOpenHelper {

    protected final static int version = 4;
    protected final static String DB_NAME = "mydb";
    private SQLiteDatabase writableDB;
    private SQLiteDatabase readableDB;
    private Context context;

    public DBManager(Context context, String dbName, SQLiteDatabase.CursorFactory factory, int dbVersion)
    {
        super(context, dbName, null, version);
        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase database)
    {
        String createTableDownload = "CREATE TABLE " + EntryContract.DownloadEntry.TABLE_NAME +
                "( " + EntryContract.DownloadEntry.COLUMN_NAME_URL + " TEXT PRIMARY KEY," +
                EntryContract.DownloadEntry.COLUMN_NAME_NAME + " TEXT," +
                EntryContract.DownloadEntry.COLUMN_NAME_EXTENSION + " TEXT," +
                EntryContract.DownloadEntry.COLUMN_NAME_SIZE + " NUMERIC," +
                EntryContract.DownloadEntry.COLUMN_NAME_STATE + " NUMERIC DEFAULT -1,"+
                EntryContract.DownloadEntry.COLUMN_NAME_IS_BYTESERVING + " NUMERIC DEFAULT 0,"+
                EntryContract.DownloadEntry.COLUMN_NAME_DOWNLOAD_TYPE + " NUMERIC DEFAULT 1,"+
                EntryContract.DownloadEntry.COLUMN_NAME_IS_STREAMABLE + " NUMERIC,"+
                EntryContract.DownloadEntry.COLUMN_NAME_TIME + " NUMERIC,"+
                EntryContract.DownloadEntry.COLUMN_NAME_GROUP + " TEXT,"+
                EntryContract.DownloadEntry.COLUMN_NAME_TOTAL_READED + " NUMERIC DEFAULT 0,"+
                EntryContract.DownloadEntry.COLUMN_NAME_SEGMENTS + " TEXT DEFAULT NULL )";
        database.execSQL(createTableDownload);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {

    }

    @Override
    public SQLiteDatabase getWritableDatabase()
    {
        if( writableDB == null )
        {
            writableDB = super.getWritableDatabase();
        }
        return  writableDB;
    }
    @Override
    public SQLiteDatabase getReadableDatabase()
    {
        if( readableDB == null )
        {
            readableDB = super.getReadableDatabase();
        }
        return  readableDB;
    }
    @Override
    public void onOpen(SQLiteDatabase database)
    {

    }
}
