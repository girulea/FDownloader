package com.example.root.downloader.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.root.downloader.entities.Crawler;
import com.example.root.downloader.entities.Download;
import com.example.root.downloader.entities.DownloadType;
import com.example.root.downloader.entities.Downloader;
import com.example.root.downloader.entities.STATE;
import com.example.root.downloader.entities.Segment;
import com.example.root.downloader.entities.StateTaskListener;
import com.example.root.downloader.utils.CustomRunnable;
import com.example.root.downloader.utils.UTIL;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by root on 22/11/16.
 */

public class CRUD {

    private static CRUD crud;
    private DBManager manager;
    private static Context context;
    private static ChangePesistenceListener LISTENER;

    private CRUD(Context context)
    {
        this.context = context;
        manager = new DBManager( context, DBManager.DB_NAME, null, DBManager.version);
        manager.getWritableDatabase();
    }

    public static void setListener(ChangePesistenceListener listener)
    {
        CRUD.LISTENER = listener;
    }
    public static void changeHandler()
    {
        LISTENER.onPersistenceChange();
    }
    public static CRUD createCRUD(Context context)
    {
        if( crud == null)
        {
            crud = new CRUD(context);
            CRUD.context = context;
        }
        return crud;
    }
    public static CRUD getCRUD()
    {
        if( crud == null && context != null )
        {
            crud = new CRUD( context );
        }
        return  crud;
    }

    public static void disposeCRUD()
    {
        crud.close();
        crud = null;
    }

    private void close()
    {
        manager.close();
    }

    public boolean insertDownload( Download download )
    {
        boolean result = true;
        try {
            SQLiteDatabase database = manager.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(EntryContract.DownloadEntry.COLUMN_NAME_URL, download.getURI());
            contentValues.put(EntryContract.DownloadEntry.COLUMN_NAME_NAME, download.getFilename());
            contentValues.put(EntryContract.DownloadEntry.COLUMN_NAME_SIZE, download.getLength());
            int byteServingInt = download.isByteServing() ? 1 : 0;
            contentValues.put(EntryContract.DownloadEntry.COLUMN_NAME_IS_BYTESERVING, byteServingInt );
            contentValues.put( EntryContract.DownloadEntry.COLUMN_NAME_DOWNLOAD_TYPE, download.getDownloadType().getValue() );
            database.insert(EntryContract.DownloadEntry.TABLE_NAME, null, contentValues);
            changeHandler();
        }catch (Exception e )
        {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean saveFinishedDownload( Download download )
    {
        boolean result = true;
        try
        {
            SQLiteDatabase database = manager.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put( EntryContract.DownloadEntry.COLUMN_NAME_STATE, STATE.FINISHED.getValue() );
            contentValues.put( EntryContract.DownloadEntry.COLUMN_NAME_TOTAL_READED, download.getTotalReaded());
            contentValues.put( EntryContract.DownloadEntry.COLUMN_NAME_SEGMENTS, "");
            database.update(EntryContract.DownloadEntry.TABLE_NAME, contentValues, "" +EntryContract.DownloadEntry.COLUMN_NAME_URL+ "=" + "'" + download.getURI() + "'",null);
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean saveIstanceDownload( Download download )
    {
        boolean result = true;
        try
        {
            String uri = download.getURI();
            SQLiteDatabase database = manager.getWritableDatabase();
            ArrayList<Segment> segments = download.getSegments();
            JSONArray jsonArray;
            if( segments == null )
            {
                jsonArray = new JSONArray();
            }
            else
            {
                synchronized ( segments )
                {
                    jsonArray = UTIL.convertTypedArrayListToJSONArray( segments );
                }
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put( EntryContract.DownloadEntry.COLUMN_NAME_SEGMENTS, jsonArray.toString() );
            contentValues.put( EntryContract.DownloadEntry.COLUMN_NAME_TOTAL_READED, download.getTotalReaded() );
            contentValues.put( EntryContract.DownloadEntry.COLUMN_NAME_STATE, download.getState().getValue() );
            int byteServingInt = download.isByteServing() ? 1 : 0;
            contentValues.put(EntryContract.DownloadEntry.COLUMN_NAME_IS_BYTESERVING, byteServingInt );
            int isStreamableInt = download.isStreamable() ? 1 : 0;
            contentValues.put( EntryContract.DownloadEntry.COLUMN_NAME_IS_STREAMABLE, isStreamableInt );
            contentValues.put( EntryContract.DownloadEntry.COLUMN_NAME_EXTENSION, download.getExtension() );
            contentValues.put( EntryContract.DownloadEntry.COLUMN_NAME_DOWNLOAD_TYPE, download.getDownloadType().getValue() );
            int update = database.update(EntryContract.DownloadEntry.TABLE_NAME, contentValues, EntryContract.DownloadEntry.COLUMN_NAME_URL + "=" + "?" , new String[]{uri});
            if( update == 0 )
            {
                database.delete(EntryContract.DownloadEntry.TABLE_NAME, "" + EntryContract.DownloadEntry.COLUMN_NAME_URL + "=" + "'" + download.getURI() + "'", null);
                if( download.getGroup() != null )
                {
                    contentValues.put( EntryContract.DownloadEntry.COLUMN_NAME_GROUP, download.getGroup());
                }
                contentValues.put( EntryContract.DownloadEntry.COLUMN_NAME_NAME, download.getFilename() );
                contentValues.put( EntryContract.DownloadEntry.COLUMN_NAME_URL, download.getURI());
                contentValues.put( EntryContract.DownloadEntry.COLUMN_NAME_SIZE, download.getLength());
                contentValues.put( EntryContract.DownloadEntry.COLUMN_NAME_TIME, UTIL.getCurrentTimeMillis());
                database.insert( EntryContract.DownloadEntry.TABLE_NAME, null, contentValues);
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public Thread getCurrentDownloadsAsync(StateTaskListener l )
    {
        Thread thread = new Thread(new CustomRunnable(null, l) {
            @Override
            public void run()
            {
                ArrayList<Download> activeItems = getCurrentDownloads();
                listener.onTaskFinished(null, activeItems);
            }
        });
        thread.start();
        return thread;
    }

    public ArrayList<Download> getCurrentDownloads( )
    {
        ArrayList<Download> activeItems = new ArrayList<>();
        HashMap<String,Download> downloads = Download.getDOWNLOADS();
        activeItems.addAll( downloads.values() );
        return activeItems;
    }
    public Thread getSavedDownloadsAsync(StateTaskListener l )
    {
        Thread thread = new Thread(new CustomRunnable( null, l ) {
            @Override
            public void run()
            {
                ArrayList<Download> savedItems = getSavedDownloadsWithoutActive();
                listener.onTaskFinished( savedItems, null );
            }
        });
        thread.start();
        return thread;
    }

    public Download getDownload(String uri)
    {
        Download result = null;
        SQLiteDatabase database = manager.getReadableDatabase();
        Cursor resultSet = database.query( EntryContract.DownloadEntry.TABLE_NAME,null,EntryContract.DownloadEntry.COLUMN_NAME_URL + "=?", new String[]{uri} ,null,null,null);
        if( resultSet.getCount() > 0 )
        {
            try
            {
                resultSet.moveToFirst();
                result = createDownloadFromCursor( resultSet );
            }catch ( Exception e )
            {

            }
        }
        return result;
    }
    public Download getFirstDownloader()
    {
        Download result = null;
        SQLiteDatabase database = manager.getReadableDatabase();
        Cursor resultSet = database.query(EntryContract.DownloadEntry.TABLE_NAME,null,null,null,null,null,EntryContract.DownloadEntry.COLUMN_NAME_TIME);
        HashMap<String,Download> downloads = Download.getDOWNLOADS();
        if( resultSet.getCount() > 0 )
        {
            try {
                resultSet.moveToFirst();
                do {
                    String uri = resultSet.getString(resultSet.getColumnIndex(EntryContract.DownloadEntry.COLUMN_NAME_URL));
                    STATE state = STATE.getStateFromValue( resultSet.getInt(resultSet.getColumnIndex(EntryContract.DownloadEntry.COLUMN_NAME_STATE)) );

                    if ( !downloads.containsKey(uri) && state != STATE.FINISHED)
                    {
                        result = createDownloadFromCursor( resultSet );
                        if( result instanceof Crawler )
                        {
                            result = null;
                        }
                    }
                } while (resultSet.moveToNext() && result == null);
                resultSet.close();
            }catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        return result;
    }
    public ArrayList<Download> getSavedDownloadsWithoutActive()
    {
        ArrayList<Download> savedItems = new ArrayList<>();
        SQLiteDatabase database = manager.getReadableDatabase();
        Cursor resultSet = database.query(EntryContract.DownloadEntry.TABLE_NAME,null,null,null,null,null,EntryContract.DownloadEntry.COLUMN_NAME_TIME);
        HashMap<String,Download> downloads = Download.getDOWNLOADS();
        if( resultSet.getCount() > 0 )
        {
            try {
                resultSet.moveToFirst();
                do {
                    String uri = resultSet.getString(resultSet.getColumnIndex(EntryContract.DownloadEntry.COLUMN_NAME_URL));
                    if (!downloads.containsKey(uri))
                    {
                        Download download = createDownloadFromCursor( resultSet );
                        savedItems.add(download);
                    }
                } while (resultSet.moveToNext());
                resultSet.close();
            }catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        return savedItems;
    }
    public void deleteDownload(Download download) {
            try
            {
                SQLiteDatabase database = manager.getWritableDatabase();
                int delete = database.delete(EntryContract.DownloadEntry.TABLE_NAME, "" + EntryContract.DownloadEntry.COLUMN_NAME_URL + "=" + "'" + download.getURI() + "'", null);
                changeHandler();
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
    }

    public Thread getAllDownloads( StateTaskListener listener, ArrayList<Download> savedItems, ArrayList<Download> activeItems )
    {
        Thread thread = new Thread(new CustomRunnable( savedItems, activeItems, listener) {
            @Override
            public void run()
            {
                ArrayList<Download> savedItems = getSavedDownloadsWithoutActive();
                ArrayList<Download> activeItems = getCurrentDownloads();
                //UTIL.sortDownloadByTime( savedItems );
                listener.onTaskFinished( savedItems, activeItems);
            }
        });
        thread.start();
        return thread;
    }

    public Download createDownloadFromCursor( Cursor resultSet ) throws JSONException {
        Download result = null;
        String uri = resultSet.getString( resultSet.getColumnIndex(EntryContract.DownloadEntry.COLUMN_NAME_URL ));
        String filename = resultSet.getString(resultSet.getColumnIndex(EntryContract.DownloadEntry.COLUMN_NAME_NAME));
        long size = resultSet.getLong(resultSet.getColumnIndex(EntryContract.DownloadEntry.COLUMN_NAME_SIZE));
        int stateAsInt = resultSet.getInt(resultSet.getColumnIndex(EntryContract.DownloadEntry.COLUMN_NAME_STATE));
        long totalReaded = resultSet.getLong(resultSet.getColumnIndex(EntryContract.DownloadEntry.COLUMN_NAME_TOTAL_READED));
        boolean byteServing = resultSet.getInt(resultSet.getColumnIndex(EntryContract.DownloadEntry.COLUMN_NAME_IS_BYTESERVING)) == 1;
        boolean streamable = resultSet.getInt( resultSet.getColumnIndex(EntryContract.DownloadEntry.COLUMN_NAME_IS_STREAMABLE)) == 1;
        long time = resultSet.getLong( resultSet.getColumnIndex( EntryContract.DownloadEntry.COLUMN_NAME_TIME));
        String extension = resultSet.getString( resultSet.getColumnIndex(EntryContract.DownloadEntry.COLUMN_NAME_EXTENSION));
        String group = resultSet.getString( resultSet.getColumnIndex( EntryContract.DownloadEntry.COLUMN_NAME_GROUP));
        DownloadType downloadType = DownloadType.downloadTypeFromIntValue( resultSet.getInt( resultSet.getColumnIndex(EntryContract.DownloadEntry.COLUMN_NAME_DOWNLOAD_TYPE)));
        STATE state = STATE.getStateFromValue( stateAsInt );
        ArrayList<Segment> segments = null;
        if( totalReaded == 0 )
        {
            state = STATE.NOT_STARTED;
        }
        if( state == STATE.STARTED || state == STATE.PAUSED )
        {
            state = STATE.STOPPED;
        }
        if ( state != STATE.FINISHED )
        {
            JSONArray jsonArray;
            String jsonString = resultSet.getString(resultSet.getColumnIndex(EntryContract.DownloadEntry.COLUMN_NAME_SEGMENTS));
            if( jsonString != null )
            {
                //jsonArray = new JSONArray(jsonString);
                segments = UTIL.convertJSONStringToTypedArrayList(jsonString);
            }
        }
        result = Download.createDownloadFromParam(filename, group, extension, uri, time, size, totalReaded, byteServing, streamable, state, downloadType);
        result.setSegments(segments);
        return result;
    }
}
