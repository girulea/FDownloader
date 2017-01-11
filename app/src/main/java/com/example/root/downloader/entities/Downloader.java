package com.example.root.downloader.entities;

import android.os.Environment;

import com.example.root.downloader.utils.UTIL;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by root on 19/11/16.
 */

public class Downloader extends Download {
    private static final String TAG_CLASS = "DOWNLOADER";
    private static final long MIN_LENGTH_FOR_SEGMENTATION = 1024 * 1024 ;

    public Downloader(String uri, String group)
    {
        this.uri = uri;
        totalReaded = 0;
        this.group = group;
        try {
            state = STATE.NOT_STARTED;
            listenersUpdateDetails = new ArrayList<>();
            prepareDownload();
            updateDetailsHandler();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Downloader(String filename, String group, String extension, String uri, long time, long length, long totalReaded, boolean byteServing, boolean streamable,STATE state)
    {
        this.filename = filename;
        this.extension = extension;
        this.uri = uri;
        this.length = length;
        this.totalReaded = totalReaded;
        this.byteServing = byteServing;
        this.streamable = streamable;
        if( streamable )
        {
            this.streamableType = UTIL.getStreamableTypeFromExtension(extension);
        }
        this.group = group;
        this.time = time;
        this.state = state;
        this.downloadType = DownloadType.DOWNLOADER;
    }


    void prepareDownload() throws IOException
    {
        Worker worker = Worker.createWorkerByProtocol( uri );
        HashMap<String, Object> details = new HashMap<>();
        if( filename != null )
        {
            details.put( "filename", filename );
        }
        if( length != 0 )
        {
            details.put("length", length);
        }
        details = worker.loadDetailsDownload( details );
        filename = ( String ) details.get("filename");
        length = ( Long ) details.get("length");
        byteServing = ( Boolean ) details.get("byteserving");
        if( !byteServing )
        {
            connection = details.get("connection");
        }
        extension = (String) details.get("extension");
        checkIfIsStreamableFile();
        if( streamable )
        {
            streamableType = UTIL.getStreamableTypeFromExtension( extension );
        }
        downloadType = ( DownloadType ) details.get("contentType");
    }

    @Override
    public void start()
    {
        workers = new ArrayList<>();
        state = STATE.STARTED;
        thread = new Thread( this );
        startDownloadHandler();
        thread.start();
    }

    @Override
    public void pause()
    {
        synchronized ( workers )
        {
            for( Worker worker : workers )
            {
                worker.pause();
            }
        }
        state = STATE.PAUSED;
    }

    @Override
    public void resume()
    {
        synchronized ( workers )
        {
            for( Worker worker : workers )
            {
                worker.resume();
            }
        }
        state = STATE.STARTED;
    }
    @Override
    public void stop()
    {
        synchronized ( workers )
        {
            for (Worker worker : workers) {
                worker.stop();
            }
        }
        interruptedDownloadHandler();
    }

    @Override
    public void restart()
    {
        if( workers != null )
        {
            synchronized (workers) {
                for (Worker worker : workers) {
                    worker.destroy();
                }
            }
        }
        workers = new ArrayList<>();
        segments = new ArrayList<>();
        totalReaded = 0;
        if( accessFile != null )
        {
            try
            {
                accessFile.close();
                accessFile = null;
                deleteFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if( thread != null && !thread.isInterrupted() )
        {
            thread.interrupt();
        }
        restartDownloadHandler();
        thread = new Thread( this );
        thread.start();
    }


    @Override
    public void delete()
    {
        if( state == STATE.STARTED || state == STATE.PAUSED )
        {
            if( workers != null )
            {
                synchronized (workers) {
                    for (Worker worker : workers) {
                        worker.destroy();
                    }
                }
            }
        }
        deleteDownloadHandler();
        if( thread != null && !thread.isInterrupted() )
        {
            thread.interrupt();
        }
    }



    @Override
    public String getAbsoluteFilePath()
    {
        return filePath;
    }

    @Override
    public void run()
    {
        try
        {
            createWriter();
            if( byteServing && length >= MIN_LENGTH_FOR_SEGMENTATION )
            {
                if( segments == null || segments.size() == 0 )
                {
                    scheduleDownload();
                }
                ArrayList<Segment> freeSegment = getFreeSegments(num_of_threads);
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
                Worker worker = Worker.createWorkerByProtocol( uri );
                worker.setDownloadListener(this);
                segments = new ArrayList<>();
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


    private void createWriter() throws IOException
    {
        destinationFile = UTIL.getDownloadFile(filename,group);
        if( totalReaded <= 0 )
        {
            if (destinationFile.exists()) {
                destinationFile.delete();
            }
            destinationFile.createNewFile();
        }
        accessFile = new RandomAccessFile(destinationFile, "rw");
    }

    private void deleteFile()
    {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), filename);
        if( file.exists() )
        {
            file.delete();
        }
    }




    @Override
    public void onStartWorker( Worker worker ) throws IOException {
    }

    @Override
    public void onPauseWorker(Worker w) throws IOException
    {

    }

    @Override
    public void onResumeWorker(Worker w)
    {

    }


    @Override
     public void onProgressWorker(List<Byte> data, long start) throws IOException
    {
        byte[] d = UTIL.toByteArray(data);
        synchronized ( accessFile )
        {
            accessFile.seek(start);
            accessFile.write(d);
        }
        totalReaded += data.size();
        progressDownloadHanlder();
        data.clear();
    }

    @Override
    public void onFinishWorker( Worker worker ) throws IOException
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
        else if ( segments.size() == 0 && workers.size() == 0)
        {
            accessFile.close();
            downloadFinishedHandler();
        }
    }


    @Override
    public void onInterruptedWorker(Worker worker)
    {
        synchronized ( workers )
        {
            if( workers.contains( worker ))
            {
                workers.remove( worker );
            }
        }
        synchronized ( segments )
        {
            addSegmentFromInterruptedDownload( worker );
        }
        if( workers.size() == 0 )
        {
            disposeActiveDownload();
            interruptedDownloadHandler();
        }
    }

    private void disposeActiveDownload()
    {
        synchronized ( Download.DOWNLOADS )
        {
            if( Download.DOWNLOADS.contains( this ))
            {
                Download.DOWNLOADS.remove( this );
            }
        }
        try
        {
            synchronized ( accessFile )
            {
                accessFile.close();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    private void addSegmentFromInterruptedDownload(Worker w)
    {
        long start = w.getFirstByte() + w.getTotalReaded();
        long end = w.getLastByte();
        Segment segment = new Segment( start, end );
        segments.add( segment );
    }

    @Override
    public void onErrorWorker( Worker worker, String error )
    {
        synchronized ( workers )
        {
            if( workers.contains( worker ))
            {
                workers.remove( worker );
            }
        }
        synchronized ( segments )
        {
            addSegmentFromInterruptedDownload( worker );
        }
        if( workers.size() == 0 )
        {
            disposeActiveDownload();
            errorDownloadHandler();
        }
    }


    public static Download createView(String filename, String group, String extension, String uri, long time, long length, long totalReaded, boolean byteServing, boolean streamable, STATE state )
    {
        return new Downloader(filename, group, extension, uri, time, length, totalReaded, byteServing, streamable,state );
    }

}
