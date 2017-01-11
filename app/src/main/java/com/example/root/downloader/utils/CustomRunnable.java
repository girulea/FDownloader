package com.example.root.downloader.utils;

import com.example.root.downloader.entities.Download;
import com.example.root.downloader.entities.StateTaskListener;

import java.util.ArrayList;

/**
 * Created by root on 23/11/16.
 */

public class CustomRunnable implements Runnable {
    protected Object object;
    protected StateTaskListener listener;
    protected long millis;
    protected IStopwatch stopwatch;
    protected ArrayList<Download> savedItems;
    protected ArrayList<Download> activeItems;
    public CustomRunnable(Object object, StateTaskListener listener)
    {
        this.object = object;
        this.listener = listener;
    }
    public CustomRunnable( ArrayList<Download> savedItems, ArrayList<Download> activeItems, StateTaskListener listener )
    {
        this.activeItems = activeItems;
        this.savedItems = savedItems;
        this.listener = listener;
    }

    public CustomRunnable(IStopwatch stopwatch, long millis )
    {
        this.stopwatch = stopwatch;
        this.millis = millis;
    }
    @Override
    public void run() {

    }
}
