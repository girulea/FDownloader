package com.example.root.downloader.entities;

/**
 * Created by root on 21/11/16.
 */

public interface DownloadUpdateListener
{
    void onRestartDownload( Download download );
    void onStartDownload( Download download );
    void onErrorDownload( Download download );
    void onUpdateDetail( Download download );
    void onProgressDownload( Download download );
    void onFinishedDownload( Download download );
    void onDeleteDownload( Download download );
    void onInterruptDownload(Download download );
}
