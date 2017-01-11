package com.example.root.downloader.entities;

/**
 * Created by root on 02/12/16.
 */

public enum STATE
{
    NOT_STARTED(-1), FINISHED(0), STARTED(1), PAUSED(2), STOPPED(3);

    private final int value;
    STATE(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    public static STATE getStateFromValue( int value )
    {
        STATE result = null;
        switch ( value )
        {
            case -1: result = NOT_STARTED;
                break;
            case 0: result = FINISHED;
                break;
            case 1: result = STARTED;
                break;
            case 2: result = PAUSED;
                break;
            case 3: result = STOPPED;
                break;
        }
        return result;
    }
}
