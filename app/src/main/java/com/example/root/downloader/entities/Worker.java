package com.example.root.downloader.entities;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by Amerigo on 18/11/16.
 */

abstract class Worker<C> implements Runnable
{
    protected static final String TAG_CLASS = "WORKER" ;
    static int  NUMBER_OF_ISTANCES;
    final static int BUFFER_SIZE = 10480;
    private final static int SIZE_OF_THE_FIRST_BUFFER = 1572864;

    String uri;
    long firstByte;
    long lastByte;
    C connection;
    private int totalReaded;
    WorkerListener listener;
    ArrayList<Byte> first_buffer;
    String messageError;

    Worker(String uri, long firstByte, long lastByte)
    {
        this.uri = uri;
        this.firstByte = firstByte;
        this.lastByte = lastByte;
        first_buffer = new ArrayList<>();
        NUMBER_OF_ISTANCES++;

    }

    Worker( String uri )
    {
        this.uri = uri;
        first_buffer = new ArrayList<>();
        NUMBER_OF_ISTANCES++;
    }

    Worker( C connection )
    {
        this.connection = connection;
        first_buffer = new ArrayList<>();
        NUMBER_OF_ISTANCES++;
    }

    public abstract void start();

    public abstract void pause();

    public abstract void resume();

    public abstract void stop();

    public abstract void destroy();

    public abstract HashMap<String, Object> loadDetailsDownload( HashMap<String, Object> detail ) throws IOException;

    long getFirstByte() {
        return firstByte;
    }

    long getLastByte() {
        return lastByte;
    }

    public ArrayList<Byte> getData() {
        return first_buffer;
    }

    long getTotalReaded()
    {
        return totalReaded;
    }

    void setDownloadListener(WorkerListener listener)
    {
        this.listener = listener;
    }

    protected abstract void createConnection() throws IOException;

    protected abstract void download(InputStream inputStream) throws IOException, InterruptedException;

    void errorHandler()
    {
        if(listener != null )
        {
            listener.onErrorWorker(this, messageError);
        }
    }
    void addBytes(byte[] array, int readed) throws IOException {
        for( int i = 0; i < readed; i++ )
        {
            byte b = array[i];
            first_buffer.add( b );
        }
        totalReaded += readed;
        if( first_buffer.size() >= SIZE_OF_THE_FIRST_BUFFER )
        {
            writeOnDisk();
        }
    }

    void interruptedHandler()
    {
        if(listener != null )
        {
            listener.onInterruptedWorker(this);
        }
    }

    void flush()
    {
        if( first_buffer != null && first_buffer.size() > 0 )
        {
            try
            {
                writeOnDisk();
            } catch (IOException e)
            {
            }
        }
    }
    void writeOnDisk() throws IOException {
        if( listener != null )
        {
            long first = firstByte + totalReaded - first_buffer.size();
            listener.onProgressWorker( first_buffer, first);
        }
    }

    public static Worker createWorkerByProtocol( String uri ) throws MalformedURLException
    {
        Worker worker = null;
        URL url = new URL(uri);
        String protocol = url.getProtocol();
        protocol = protocol.toLowerCase();
        switch ( protocol )
        {
            case "http":
            case "https":worker = new HTTPWorker( uri );
                break;
        }
        return worker;
    }
    public static Worker createWorkerByProtocol( String uri, long firstByte, long lastByte ) throws MalformedURLException
    {
        Worker worker = null;
        URL url = new URL(uri);
        String protocol = url.getProtocol();
        protocol = protocol.toLowerCase();
        switch ( protocol )
        {
            case "http":
            case "https":worker = new HTTPWorker( uri, firstByte, lastByte );
                break;
        }
        return worker;
    }

    public static Worker createWorkerByProtocol( Object connection ) throws MalformedURLException
    {
        Worker worker = null;
        if( connection instanceof HttpURLConnection)
        {
            worker = new HTTPWorker( (HttpURLConnection) connection );
        }
        return worker;
    }
}
