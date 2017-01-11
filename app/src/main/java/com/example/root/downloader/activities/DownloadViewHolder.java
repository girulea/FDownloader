package com.example.root.downloader.activities;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.root.downloader.entities.Download;
import com.example.root.downloader.entities.Downloader;


public class DownloadViewHolder extends RecyclerView.ViewHolder
{

    private Download download;
    View v;
    public DownloadViewHolder(View v, Download download)
    {
        super(v);
        this.v = v;
        this.download = download;
    }

    public void setDownloader( Download download )
    {
        this.download = download;
    }
}