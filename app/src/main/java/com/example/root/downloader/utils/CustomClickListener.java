package com.example.root.downloader.utils;

import android.content.DialogInterface;
import android.view.View;

import com.example.root.downloader.entities.Download;

/**
 * Created by root on 02/12/16.
 */

public class CustomClickListener implements View.OnClickListener {


    protected Download download;
    protected Object object;
    public CustomClickListener( Download download )
    {
        this.download = download;
    }

    public CustomClickListener( Download download, Object object)
    {
        this.download = download;
        this.object = object;
    }
    @Override
    public void onClick(View view)
    {

    }
}
