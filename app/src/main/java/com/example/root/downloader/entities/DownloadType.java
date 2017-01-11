package com.example.root.downloader.entities;

/**
 * Created by Amerigo on 26/12/16.
 */

public enum DownloadType
{
    DOWNLOADER(0),PLAYER(1),CRAWLER(2);

    private int value;
    DownloadType( int value )
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    public static DownloadType downloadTypeFromIntValue( int value )
    {
        DownloadType result = null;
        switch ( value )
        {
            case 0: result = DOWNLOADER;
                break;
            case 1: result = PLAYER;
                break;
            case 2: result = CRAWLER;
                break;
        }
        return result;
    }
}
