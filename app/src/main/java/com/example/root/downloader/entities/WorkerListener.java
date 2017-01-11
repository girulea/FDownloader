package com.example.root.downloader.entities;

import com.example.root.downloader.entities.Worker;

import java.io.IOException;
import java.util.List;

/**
 * Created by root on 19/11/16.
 */

public interface WorkerListener
{
    void onStartWorker( Worker w ) throws IOException;
    void onPauseWorker( Worker w ) throws IOException;
    void onResumeWorker( Worker w );
    void onProgressWorker( List<Byte> data, long start ) throws IOException;
    void onFinishWorker(Worker w ) throws IOException;
    void onInterruptedWorker( Worker w );
    void onErrorWorker( Worker worker , String errorMessage);
}

