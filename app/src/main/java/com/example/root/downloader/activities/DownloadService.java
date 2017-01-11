package com.example.root.downloader.activities;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.root.downloader.database.CRUD;
import com.example.root.downloader.entities.Download;
import com.example.root.downloader.entities.Downloader;
import com.example.root.downloader.entities.DownloadUpdateListener;
import com.example.root.downloader.entities.Player;
import com.example.root.downloader.entities.URLMatcherListener;
import com.example.root.downloader.utils.UTIL;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by root on 18/11/16.
 */


public class DownloadService extends IntentService implements DownloadUpdateListener, URLMatcherListener
{

    private static DownloadService actualListener;
    private boolean automaticDownload = true;
    public DownloadService()
    {
        super("DownloadService");
    }

    public DownloadService(String name)
    {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent workIntent)
    {
        if( CRUD.getCRUD() == null )
        {
            CRUD.createCRUD( getApplicationContext() );
        }
        if( workIntent.hasExtra("CREATE") )
        {
            if( automaticDownload )
            {
                CRUD crud = CRUD.getCRUD();
                Download download = crud.getFirstDownloader();
                if( download != null )
                {
                    download.addListenerUpdateDetails(this);
                    download.start();
                }
            }
        }
        else
        {
            String uri = workIntent.getStringExtra("URL");
            Download download;
            if (workIntent.hasExtra("streaming"))
            {
                download = new Player(uri);
            }
            else
            {
                download = new Downloader(uri, null);
            }
            download.addListenerUpdateDetails(this);
            if (workIntent.hasExtra("threads")) {
                download.setNumOfThreads(workIntent.getIntExtra("threads", 8));
            }
            if (workIntent.hasExtra("filename")) {
                download.setFilename(workIntent.getStringExtra("filename"));
            }
            //CRUD.getCRUD().insertDownload( download );
        }

    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        actualListener = this;
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //CRUD.disposeCRUD();
    }

    @Override
    public void onRestartDownload(Download download)
    {
        CRUD crud = CRUD.getCRUD( );
        boolean result =  crud.saveIstanceDownload( download );
        if( !result )
        {
            Toast.makeText(getApplicationContext()," Errore nell'inserimento del nuovo download",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStartDownload(Download download)
    {

    }

    @Override
    public void onErrorDownload(Download download)
    {
        CRUD crud = CRUD.getCRUD(  );
        crud.saveIstanceDownload( download );
    }

    @Override
    public void onUpdateDetail(Download download)
    {
        CRUD crud = CRUD.getCRUD(  );
        boolean result =  crud.saveIstanceDownload( download );

    }
    @Override
    public void onProgressDownload(Download download)
    {

    }

    @Override
    public void onFinishedDownload(Download download)
    {
        CRUD crud = CRUD.getCRUD(  );
        crud.saveFinishedDownload( download );
        MediaScannerConnection.scanFile(DownloadService.this,
                new String[] { UTIL.getDownloadFile(download.getFilename(),download.getGroup()).getAbsolutePath() }, null, (path, uri) -> { });
        if( automaticDownload )
        {
            Download d = crud.getFirstDownloader();
            if( d != null )
            {
                d.addListenerUpdateDetails(this);
                d.start();
            }
        }
    }

    @Override
    public void onDeleteDownload(Download download)
    {
        CRUD crud = CRUD.getCRUD();
        crud.deleteDownload( download );
        File file =  UTIL.getDownloadFile( download.getFilename(), download.getGroup());
        if( file.delete())
        {
            Toast.makeText(getApplicationContext(),"Contenuto eliminato correttamente",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Contenuto non trovato",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInterruptDownload(Download download )
    {
        CRUD crud = CRUD.getCRUD(  );
        crud.saveIstanceDownload( download );
    }

    public static DownloadService getActualListener()
    {
        return actualListener;
    }


    @Override
    public void onFinishedMatches(ArrayList<String> URIs, @Nullable String group) {

    }

    @Override
    public void onMatchedURI(String uri, @Nullable String group)
    {
        if( CRUD.getCRUD() == null )
        {
            CRUD.createCRUD( getApplicationContext() );
        }
        Download download = new Downloader(uri, group);
        download.addListenerUpdateDetails(this);
        //CRUD.getCRUD().insertDownload( download );
    }
}
