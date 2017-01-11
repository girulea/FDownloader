package com.example.root.downloader.entities;

import android.widget.VideoView;

import com.example.root.downloader.entities.Download;

/**
 * Created by Amerigo on 20/12/16.
 */

public interface StreamRequestListener
{
    void onRequestPlayVideo( Download download );
    void onRequestPlayMusicTrack( Download download );

    void onRequestCrawling(Download download);
}
