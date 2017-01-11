package com.example.root.downloader.entities;

import android.os.Build;

import com.example.root.downloader.activities.DownloadService;
import com.example.root.downloader.utils.UTIL;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by root on 23/11/16.
 */

public abstract class Download implements WorkerListener, Runnable
{
    ArrayList<Worker> workers;
    final static String[] STREAMABLE_FILE_EXTENSIONS = new String[]{".mp4",".mp3",".avi",".flv",".mkv"};

    final static int SIZE_OF_THE_SEGMENTS = 1024 * 1024 * 10; //10 MB
    final static ArrayList<Download> DOWNLOADS = new ArrayList<>();
    ArrayList<DownloadUpdateListener> listenersUpdateDetails;

    static int NUM_OF_MAX_THREADS = 36;
    int num_of_threads = 8;
    RandomAccessFile accessFile;
    Thread thread;
    Object connection;

    String uri;
    String filename;
    long length;
    long totalReaded;
    long time;
    STATE state = STATE.NOT_STARTED;
    ArrayList<Segment> segments;
    boolean byteServing;
    boolean streamable;
    STREAMABLE_TYPE streamableType;
    File destinationFile;
    String filePath;
    String extension;
    DownloadType downloadType;
    String group;

    abstract void prepareDownload() throws IOException;

    public abstract void start();

    public abstract void pause();

    public abstract void resume();

    public abstract void stop();

    public abstract void restart();

    public abstract void delete();

    public abstract String getAbsoluteFilePath();

    void addToActiveDownloads( Download download )
    {
        synchronized ( Download.DOWNLOADS )
        {
            if( !Download.DOWNLOADS.contains( download ) )
            {
                Download.DOWNLOADS.add( download );
            }
        }
    }
    public static HashMap<String,Download> getDOWNLOADS()
    {
        HashMap<String,Download> result = new HashMap<>();
        synchronized ( DOWNLOADS )
        {
            for( Download download : DOWNLOADS )
            {
                result.put( download.getURI(), download );
            }
        }
        return result;
    }

    public void addListenerUpdateDetails(DownloadUpdateListener downloadUpdateListener)
    {
        if( listenersUpdateDetails == null )
        {
            listenersUpdateDetails = new ArrayList<>();
        }
        synchronized( listenersUpdateDetails )
        {
            if (!listenersUpdateDetails.contains(downloadUpdateListener))
            {
                listenersUpdateDetails.add(downloadUpdateListener);
            }
        }
    }
    public void removeListenerUpdateDetails( DownloadUpdateListener downloadUpdateListener)
    {
        synchronized( listenersUpdateDetails )
        {
            if ( listenersUpdateDetails.contains(downloadUpdateListener) )
            {
                listenersUpdateDetails.remove(downloadUpdateListener);
            }
        }
    }

    void checkIfIsStreamableFile()
    {
        for( String s : STREAMABLE_FILE_EXTENSIONS )
        {
            if( s.compareToIgnoreCase( extension ) == 0 )
            {
                streamable = true;
            }
        }
    }

    ArrayList<Segment> getFreeSegments(int n)
    {
        int number;
        ArrayList<Segment> result = new ArrayList<>();
        synchronized ( segments )
        {
            if (n < segments.size()) {
                number = n;
            } else {
                number = segments.size();
            }
            for (int i = 0; i < number; i++) {
                Segment segment = segments.get(i);
                result.add(segment);
            }
            for (Segment s : result) {
                segments.remove(s);
            }
        }
        return  result;
    }

    void scheduleDownload()
    {
        int numOfSegments = (int) length / SIZE_OF_THE_SEGMENTS;
        synchronized ( segments )
        {
            if (numOfSegments >= num_of_threads) {
                segments = splitInNParts(numOfSegments);
            } else {
                segments = splitInNParts(num_of_threads);
            }
        }
    }

    void scheduleDownload( int sizeOfSegments )
    {
        int numOfSegments = (int) length / sizeOfSegments;
        if( numOfSegments >= num_of_threads)
        {
            segments = splitInNParts( numOfSegments );
        }
        else
        {
            segments = splitInNParts(num_of_threads);
        }
    }

    private ArrayList<Segment> splitInNParts(int n)
    {
        ArrayList<Segment> result = new ArrayList<>();
        long size_of_the_segments = length / n;
        long start = 0;
        long end = 0;
        for( int i = 0; i < n-1; i++)
        {
            start = i * size_of_the_segments;
            end = start + size_of_the_segments - 1;
            Segment segment = new Segment(start,end);
            result.add(segment);
        }
        Segment segment = new Segment(end+1,length-1);
        result.add(segment);
        return  result;
    }
    void startDownloadHandler()
    {
        removeNullListener();
        addToActiveDownloads(this);
        if( DownloadService.getActualListener() != null && !listenersUpdateDetails.contains(DownloadService.getActualListener()))
        {
            listenersUpdateDetails.add( DownloadService.getActualListener());
        }
        if( listenersUpdateDetails.size() > 0 )
        {
            for( DownloadUpdateListener listener : listenersUpdateDetails )
            {
                listener.onStartDownload(this);
            }
        }
    }
    void updateDetailsHandler()
    {
        removeNullListener();
        if( DownloadService.getActualListener() != null && !listenersUpdateDetails.contains(DownloadService.getActualListener()))
        {
            listenersUpdateDetails.add( DownloadService.getActualListener());
        }
        if( listenersUpdateDetails.size() > 0 )
        {
            for( DownloadUpdateListener listener : listenersUpdateDetails )
            {
                listener.onUpdateDetail(this);
            }
        }
    }
    void progressDownloadHanlder()
    {
        removeNullListener();
        if( DownloadService.getActualListener() != null && !listenersUpdateDetails.contains(DownloadService.getActualListener()))
        {
            listenersUpdateDetails.add( DownloadService.getActualListener());
        }
        if( listenersUpdateDetails.size() > 0 )
        {
            for( int i = 0; i < listenersUpdateDetails.size();i++ )
            {
                DownloadUpdateListener listener = listenersUpdateDetails.get( i );
                listener.onProgressDownload(this);
            }
        }
    }

    void downloadFinishedHandler()
    {
        state = STATE.FINISHED;
        removeFromActiveDownloads();
        removeNullListener();
        if( DownloadService.getActualListener() != null && !listenersUpdateDetails.contains(DownloadService.getActualListener()))
        {
            listenersUpdateDetails.add( DownloadService.getActualListener());
        }
        if( listenersUpdateDetails != null )
        {
            for( DownloadUpdateListener listener : listenersUpdateDetails )
            {
                listener.onFinishedDownload(this);
            }
            listenersUpdateDetails.clear();
        }
    }

    private void removeFromActiveDownloads()
    {
        synchronized ( Download.DOWNLOADS )
        {
            if( Download.DOWNLOADS.contains( this ))
            {
                Download.DOWNLOADS.remove( this );
            }
        }
    }

    void interruptedDownloadHandler()
    {
        state = STATE.STOPPED;
        removeFromActiveDownloads();
        removeNullListener();
        if( DownloadService.getActualListener() != null && !listenersUpdateDetails.contains(DownloadService.getActualListener()))
        {
            listenersUpdateDetails.add( DownloadService.getActualListener());
        }
        if( listenersUpdateDetails != null )
        {
            for( DownloadUpdateListener listener : listenersUpdateDetails  )
            {
                listener.onInterruptDownload(this);
            }
            listenersUpdateDetails.clear();
        }
    }
    void errorDownloadHandler()
    {
        state = STATE.STOPPED;
        removeFromActiveDownloads();
        removeNullListener();
        if( DownloadService.getActualListener() != null && !listenersUpdateDetails.contains(DownloadService.getActualListener()))
        {
            listenersUpdateDetails.add( DownloadService.getActualListener());
        }
        if( listenersUpdateDetails != null )
        {
            for( DownloadUpdateListener listener : listenersUpdateDetails  )
            {
                listener.onErrorDownload(this);
            }
            listenersUpdateDetails.clear();
        }
    }
    void deleteDownloadHandler()
    {
        removeNullListener();
        if( DownloadService.getActualListener() != null && !listenersUpdateDetails.contains(DownloadService.getActualListener()))
        {
            listenersUpdateDetails.add( DownloadService.getActualListener());
        }
        if( listenersUpdateDetails != null )
        {
            for( DownloadUpdateListener listener : listenersUpdateDetails  )
            {
                listener.onDeleteDownload(this);
            }
            listenersUpdateDetails.clear();
        }
    }
    void restartDownloadHandler()
    {
        state = STATE.STARTED;
        addToActiveDownloads( this );
        removeNullListener();
        if( DownloadService.getActualListener() != null && !listenersUpdateDetails.contains(DownloadService.getActualListener()))
        {
            listenersUpdateDetails.add( DownloadService.getActualListener());
        }
        if( listenersUpdateDetails != null )
        {
            for( DownloadUpdateListener listener : listenersUpdateDetails  )
            {
                listener.onRestartDownload(this);
            }
        }
    }
    private void removeNullListener()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            listenersUpdateDetails.removeIf(Objects::isNull);
        }
        else
        {
            listenersUpdateDetails.removeAll(Collections.singleton(null));
        }

    }

    void startWorker() throws MalformedURLException
    {
        Segment segment = getFreeSegments(1).get(0);
        Worker worker = Worker.createWorkerByProtocol( uri, segment.getStart(), segment.getEnd() );
        worker.setDownloadListener(this);
        synchronized ( workers )
        {
            workers.add(worker);
        }
        worker.start();
    }

    public ArrayList<Segment> getSegments()
    {
        return segments;
    }

    public void setSegments(ArrayList<Segment> segments)
    {
        this.segments = segments;
    }

    public STATE getState()
    {
        return  state;
    }

    public long getTotalReaded()
    {
        return totalReaded;
    }

    public String getURI()
    {
        return uri;
    }

    public boolean isByteServing()
    {
        return byteServing;
    }

    public String getFilename()
    {
        return filename;
    }

    public String getExtension()
    {
        return extension;
    }

    public boolean isStreamable()
    {
        return streamable;
    }

    public String getGroup()
    {
        return group;
    }

    public STREAMABLE_TYPE getStreamableType()
    {
        return streamableType;
    }
    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public void setNumOfThreads( int numOfThreads )
    {
        this.num_of_threads = numOfThreads;
    }

    public long getLength()
    {
        return  length;
    }

    public long getTime()
    {
        return time;
    }

    public static Download getDownload(String uri)
    {
        Download result = null;
        for( Download d : DOWNLOADS )
        {
            if( d.getURI().compareTo(uri) == 0 )
            {
                result = d;
            }
        }
        return result;
    }

    public DownloadType getDownloadType() {
        return downloadType;
    }

    public static Download createDownloadFromParam(String filename, String group, String extension, String uri, long time, long length, long totalReaded, boolean byteServing, boolean streamable, STATE state, DownloadType downloadType )
    {
        Download result = null;
        if( downloadType == DownloadType.DOWNLOADER )
        {
            result = Downloader.createView( filename, group, extension, uri, time, length, totalReaded, byteServing,streamable, state);
        }
        else if( downloadType == DownloadType.CRAWLER )
        {
            result = Crawler.createView( uri, filename, time );
        }
        return result;
    }
}
