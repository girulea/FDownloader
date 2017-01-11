package com.example.root.downloader.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.root.downloader.utils.UTIL;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amerigo on 13/12/16.
 */

public class Player extends Download {

    private RandomAccessFile accessFile;
    private static int sizeOfSegments = 1024 * 1024;
    public Player( String uri)
    {
        this.uri = uri;
        totalReaded = 0;
        try {
            prepareDownload();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run()
    {
        try
        {
            createWriter();
            if( byteServing )
            {
                if( segments == null )
                {
                    scheduleDownload( sizeOfSegments );
                }
                ArrayList<Segment> freeSegment = getFreeSegments( num_of_threads );
                for( Segment segment : freeSegment )
                {
                    Worker worker = Worker.createWorkerByProtocol( uri, segment.getStart(), segment.getEnd() );
                    worker.setDownloadListener(this);
                    synchronized ( workers )
                    {
                        workers.add(worker);
                    }
                    worker.start();
                }
            }
            else
            {
                Worker worker = Worker.createWorkerByProtocol( connection );
                worker.setDownloadListener(this);
                synchronized ( workers )
                {
                    workers.add(worker);
                }
                worker.start();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    void prepareDownload() throws IOException {

    }

    @Override
    public void start()
    {
        synchronized ( Download.DOWNLOADS )
        {
            Download.DOWNLOADS.add(this);
        }
        createWriter();
        workers = new ArrayList<>();
        state = STATE.STARTED;
        thread = new Thread( this );
        thread.start();
    }


    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void stop()
    {
    }

    @Override
    public void restart() {

    }

    @Override
    public void delete() {

    }


    @Override
    public void onStartWorker(Worker w) throws IOException {

    }

    @Override
    public void onPauseWorker(Worker w) throws IOException {

    }

    @Override
    public void onResumeWorker(Worker w) {

    }

    @Override
    public synchronized void onProgressWorker(List<Byte> data, long start) throws IOException
    {
        accessFile.seek(start);
        byte[] d = UTIL.toByteArray(data);
        accessFile.write(d);
        totalReaded += data.size();
        progressDownloadHanlder();
        data.clear();
    }

    @Override
    public void onFinishWorker(Worker worker) throws IOException
    {
        synchronized ( workers )
        {
            if( workers.contains( worker ))
            {
                workers.remove( worker );
            }
        }
        worker.stop();
        if( segments.size() > 0 )
        {
            startWorker();
        }
    }

    @Override
    public void onInterruptedWorker(Worker w)
    {

    }

    @Override
    public void onErrorWorker(Worker worker, String errorMessage) {

    }

    public String getAbsoluteFilePath()
    {
        return destinationFile.getAbsolutePath();
    }

    private void createWriter()
    {
        try
        {
            destinationFile = File.createTempFile( filename, "mp4");
            destinationFile.createNewFile();
            accessFile = new RandomAccessFile(destinationFile, "rw");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


}
