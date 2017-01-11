package com.example.root.downloader.entities;

/**
 * Created by root on 19/11/16.
 */

public class Monitor {
    private long downloaded;
    private long downloaded_now;

    public Monitor() {}

    public long getDownloaded()
    {
        return downloaded;
    }
    public  long getDownloaded_now()
    {
        return  downloaded_now;
    }
}
