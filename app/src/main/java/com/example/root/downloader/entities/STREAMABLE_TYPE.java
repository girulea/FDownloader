package com.example.root.downloader.entities;

/**
 * Created by Amerigo on 24/12/16.
 */

public enum STREAMABLE_TYPE
{
    VIDEO(0), MUSIC(1);

    private int value;
    STREAMABLE_TYPE(int value )
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    public static STREAMABLE_TYPE getStreamableTypeFromInt(int value )
    {
        STREAMABLE_TYPE result = null;
        switch ( value )
        {
            case -1: result = null;
                break;
            case 0: result = VIDEO;
                break;
            case 1: result = MUSIC;
                break;
        }
        return result;
    }
}
