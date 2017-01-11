package com.example.root.downloader.entities;

import java.util.ArrayList;

/**
 * Created by root on 23/11/16.
 */

public interface StateTaskListener
{
    void onTaskFinished(ArrayList<Download> savedItems, ArrayList<Download> activeItems );
}
