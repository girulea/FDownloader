package com.example.root.downloader.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.MediaController;

import com.example.root.downloader.R;
import com.example.root.downloader.database.CRUD;
import com.example.root.downloader.database.ChangePesistenceListener;
import com.example.root.downloader.entities.StreamRequestListener;
import com.example.root.downloader.utils.IStopwatch;
import com.example.root.downloader.utils.UTIL;
import com.example.root.downloader.entities.Download;
import com.example.root.downloader.entities.StateTaskListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, StateTaskListener, IStopwatch, StreamRequestListener, ChangePesistenceListener {

    private RecyclerView recyclerView;
    private DownloadRecyclerViewAdapter mAdapter;
    private ArrayList<Download> savedItems;
    private ArrayList<Download> activeItems;
    private Thread timerThread;
    MediaController mediaController;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButtonAddDownload);
        fab.setOnClickListener(this);
        LinearLayoutManager linearLayout = new LinearLayoutManager(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        recyclerView = (RecyclerView) findViewById(R.id.list_view);
        recyclerView.setLayoutManager(linearLayout);
        savedItems = new ArrayList<>();
        activeItems = new ArrayList<>();
        mediaController = new MediaController( MainActivity.this );
        mAdapter = new DownloadRecyclerViewAdapter(savedItems, activeItems,this);
        CRUD.setListener(this);
        CRUD.createCRUD( getApplicationContext() ).getAllDownloads( this, savedItems, activeItems);
        recyclerView.setAdapter(mAdapter);
        createService();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        timerThread.interrupt();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        final Activity activity = this;
        new Thread(() -> {
            UTIL.verifyStoragePermissions(activity);
        }).start();
        if( timerThread != null)
        {
            timerThread.interrupt();
        }
        timerThread = UTIL.createStopwatch(this,5000);
    }

    private static final int START_DOWNLOAD_REQUEST = 1;
    @Override
    public void onClick(View view)
    {
        Intent intent = new Intent(this,DownloadActivity.class);
        startActivityForResult(intent, START_DOWNLOAD_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == START_DOWNLOAD_REQUEST ) {

            if ( resultCode == RESULT_OK )
            {
                savedItems.clear();
                CRUD.getCRUD().getSavedDownloadsAsync( this );
            }
        }
    }


    @Override
    public void onTaskFinished( ArrayList<Download> savedItems, ArrayList<Download> activeItems )
    {
        if( savedItems != null )
        {
            mAdapter.setSavedItems( savedItems);
            this.savedItems = savedItems;
        }
        if( activeItems != null )
        {
            mAdapter.setActiveItems(activeItems);
            this.activeItems = activeItems;
            //runOnUiThread(() -> mAdapter.notifyDataSetChanged());
        }
        runOnUiThread(() -> mAdapter.notifyDataSetChanged());
    }

    @Override
    public void onCompletedLap()
    {
        CRUD.getCRUD().getAllDownloads(this, savedItems, activeItems);
    }


    @Override
    public void onRequestPlayVideo( Download download )
    {
        Intent intent = new Intent(this, VideoViewActivity.class );
        intent.putExtra( "URI",download.getURI());
        startActivity( intent );
    }

    @Override
    public void onRequestPlayMusicTrack(Download download)
    {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = UTIL.getDownloadFile( download.getFilename(), download.getGroup() );
        intent.setDataAndType(Uri.fromFile(file), "audio/*");
        startActivity(intent);
    }

    @Override
    public void onRequestCrawling(Download download)
    {
        Intent intent = new Intent(this, WebViewActivity.class );
        intent.putExtra( "URI",download.getURI());
        intent.putExtra( "GROUP", download.getFilename() );
        startActivity( intent );
    }

    private void createService()
    {
        Intent intent = new Intent(this,DownloadService.class);
        intent.putExtra("CREATE","");
        startService(intent);
    }

    @Override
    public void onPersistenceChange()
    {
        CRUD.getCRUD().getSavedDownloadsAsync(this);
    }
}
