package com.example.root.downloader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.root.downloader.R;
import com.example.root.downloader.database.CRUD;
import com.example.root.downloader.entities.Download;
import com.example.root.downloader.entities.STATE;
import com.example.root.downloader.utils.UTIL;

/**
 * Created by Amerigo on 20/12/16.
 */

public class VideoViewActivity extends AppCompatActivity
{

    private static final String C_P_K = "CURRENT_POSITION";
    private VideoView videoView;
    private MediaController mediaController;
    private Download download;
    private String uri;
    private int currentPosition = 0;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView( R.layout.activity_videoview);
        Intent intent = getIntent();
        uri = intent.getStringExtra("URI");
        videoView =(VideoView)findViewById(R.id.videoView);
        mediaController = new MediaController( videoView.getContext() );
        mediaController.setAnchorView(videoView);
        mediaController.setMediaPlayer(videoView);
        videoView.setMediaController(mediaController);
        videoView.setKeepScreenOn( true );;
        videoView.requestFocus();
        loadStreamFromDownload();
    }

    private void loadStreamFromDownload() {
        Thread thread = new Thread(() -> {
            download = Download.getDownload( uri );
            if( download == null )
            {
                download = CRUD.createCRUD( getApplicationContext() ).getDownload( uri );
            }
            loadDetailsOnView();
            loadVideo();
        });
        thread.start();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        loadVideo();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        currentPosition = videoView.getCurrentPosition();
    }
    private void loadVideo()
    {
        if( download != null )
        {
            runOnUiThread(() -> {
                String videoPath = UTIL.getDownloadFile(download.getFilename(), download.getGroup()).getAbsolutePath();
                videoView.setVideoPath(videoPath);
                videoView.seekTo( currentPosition );
                videoView.start();
            });
        }
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        currentPosition = savedInstanceState.getInt( C_P_K );
        super.onRestoreInstanceState(savedInstanceState);
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle out)
    {
        out.putInt( C_P_K, currentPosition);
        super.onSaveInstanceState(out);
    }
    private void videoViewToFullScreen()
    {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DisplayMetrics metrics = new DisplayMetrics(); getWindowManager().getDefaultDisplay().getMetrics(metrics);
        android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) videoView.getLayoutParams();
        params.width =  metrics.widthPixels;
        params.height = metrics.heightPixels;
        params.leftMargin = 0;
        videoView.setLayoutParams(params);
    }
    private void videoViewToNormalScreen()
    {
        DisplayMetrics metrics = new DisplayMetrics(); getWindowManager().getDefaultDisplay().getMetrics(metrics);
        android.widget.LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) videoView.getLayoutParams();
        params.width =  (int) (300*metrics.density);
        params.height = (int) (250*metrics.density);
        params.leftMargin = 30;
        videoView.setLayoutParams(params);
    }
    private void loadDetailsOnView()
    {
        TextView textViewName = (TextView) findViewById(R.id.textViewName);
        textViewName.setText( download.getFilename() );
        TextView textViewURI = (TextView) findViewById(R.id.textViewURI);
        textViewURI.setText( download.getURI() );
        TextView textViewLength = (TextView) findViewById(R.id.textViewLength);
        textViewLength.setText( UTIL.convertLengthToString( download.getLength() ) );
        TextView textViewDownloaded = (TextView) findViewById(R.id.textViewDownloaded);
        TextView textViewPercent = (TextView) findViewById(R.id.textViewPercent);
        ImageButton playButton = (ImageButton) findViewById(R.id.playButton );
        ImageButton deleteButton = ( ImageButton ) findViewById(R.id.deleteButton);
        if( download.getState() == STATE.FINISHED )
        {
            textViewPercent.setText( "100%");
            TextView textViewSeparator = ( TextView ) findViewById(R.id.textViewSeparator );
            textViewSeparator.setVisibility( View.INVISIBLE );
            textViewDownloaded.setVisibility( View.INVISIBLE );
            playButton.setVisibility( View.INVISIBLE );
            deleteButton.setVisibility( View.INVISIBLE );

        }
        else
        {
            textViewPercent.setText(((100 *  download.getTotalReaded() ) /  download.getLength() ) + "%");
            textViewDownloaded.setText(UTIL.convertLengthToString(download.getTotalReaded()));
        }
    }
}
