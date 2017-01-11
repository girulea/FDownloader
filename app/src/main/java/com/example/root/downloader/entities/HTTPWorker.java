package com.example.root.downloader.entities;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 08/12/16.
 */

public class HTTPWorker extends Worker<HttpURLConnection>
{
    private Thread thread;
    private boolean paused = false;
    HTTPWorker(String uri, long start, long end)
    {
        super(uri, start, end);
    }

    HTTPWorker(HttpURLConnection connection)
    {
        super(connection);
    }

    HTTPWorker( String uri )
    {
        super(uri);
    }

    @Override
    public void start()
    {
        thread = new Thread( this );
        thread.start();
    }

    @Override
    public void pause()
    {
        paused = true;
    }

    @Override
    public void resume()
    {
        paused = false;
    }

    @Override
    public void stop()
    {
        thread.interrupt();
    }

    @Override
    public void destroy()
    {
        listener = null;
        thread.interrupt();
    }

    @Override
    public HashMap<String, Object> loadDetailsDownload( HashMap<String, Object> detail ) throws IOException
    {
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        Map<String,List<String>> map = connection.getHeaderFields();
        boolean byteServing = false;
        if( !detail.containsKey("filename") )
        {
            detail.put( "filename", getFileNameFromHeaders(map));
        }
        //createWriter();
        if( !detail.containsKey("length") )
        {
            detail.put( "length",getLengthFromHeaders(map));
        }
        if( !detail.containsKey("byteserving") )
        {
            byteServing = isByteServing(map);
            detail.put( "byteserving", byteServing );
        }
        if( !byteServing )
        {
            detail.put("connection", connection);
        }
        detail.put( "extension", getExtensionFromHeaders(map) );
        detail.put( "contentType", getContentTypeFromHeaders(map) );
        return detail;
    }



    @Override
    public void run()
    {
        try
        {
            try
            {
                if (connection == null)
                {
                    createConnection();
                }
                if (listener != null)
                {
                    listener.onStartWorker(this);
                }
                InputStream inputStream = connection.getInputStream();
                download(inputStream);
                inputStream.close();
                connection.disconnect();
                if (listener != null)
                {
                    listener.onFinishWorker(this);
                }
            }
            catch (MalformedURLException e)
            {
                messageError = e.getMessage();
                flush();
                errorHandler();
            }
            catch (InterruptedException e)
            {
                flush();
                interruptedHandler();
            }
        }
        catch (IOException e)
        {
            flush();
            interruptedHandler();
        }
        NUMBER_OF_ISTANCES--;
    }


    protected void createConnection() throws IOException
    {
        URL url = new URL(uri);
        connection = (HttpURLConnection) url.openConnection();
        String range = "bytes=" + firstByte + "-";
        if( lastByte != 0 )
        {
            range += lastByte;
        }
        connection.setRequestProperty("Range", range);
    }

    protected void download(InputStream inputStream) throws IOException, InterruptedException {
        byte[] buffer = new byte[ BUFFER_SIZE ];
        int readed;
        while(  ( readed = inputStream.read(buffer) ) != -1  )
        {
            addBytes( buffer, readed );
            if( paused )
            {
                flush();
                while (paused) {
                    Thread.sleep(200);
                }
            }
        }
        flush();
    }
    private boolean isByteServing(Map<String,List<String>> headers)
    {
        boolean result = false;
        if( headers.containsKey("Accept-Ranges") )
        {
            result = true;
        }
        return result;
    }
    private long getLengthFromHeaders(Map<String,List<String>> headers)
    {
        long result = -1;
        if( headers.containsKey("Content-Length") )
        {
            try
            {
                result = Long.parseLong(headers.get("Content-Length").get(0));
            }
            catch (NumberFormatException e)
            {
                Log.d(TAG_CLASS,"errore di parsing");
            }
        }
        return result;
    }

    private DownloadType getContentTypeFromHeaders( Map<String,List<String>> headers )
    {
        DownloadType result = null;
        if( headers.containsKey("Content-Type") )
        {
            try
            {
                String contentTypeString = headers.get("Content-Type").get(0).toLowerCase();
                if( contentTypeString.contains("text/html") )
                {
                    result = DownloadType.CRAWLER;
                }else
                {
                   result = DownloadType.DOWNLOADER;
                }
            }catch (Exception e )
            {
                e.printStackTrace();
            }
        }
        return result;
    }
    private String getFileNameFromHeaders(Map<String,List<String>> headers)
    {
        String result = null;
        if( headers.containsKey("Content-Disposition"))
        {
            List<String> list = headers.get("Content-Disposition");
            for( String s : list )
            {
                int index;
                if(( index = s.indexOf("filename=") ) != -1 )
                {
                    result = s.substring( "filename=".length() + index ).replaceAll("\"","");
                }
            }
        }
        else
        {
            String tmp = uri + "";
            if( uri.endsWith("/") )
            {
                tmp = tmp.substring( 0, uri.length()-1 );
            }
            tmp = tmp.substring(tmp.lastIndexOf("/") + 1 );
            result = tmp;
        }
        return result;
    }
    private String getExtensionFromHeaders(Map<String, List<String>> headers)
    {
        String result = null;
        if( headers.containsKey("Content-Disposition"))
        {
            List<String> list = headers.get("Content-Disposition");
            for( String s : list )
            {
                int index;
                if(( index = s.lastIndexOf(".") ) != -1 )
                {
                    result = s.substring( index ).replaceAll("\"","");
                }
            }
        }
        else
        {
            String tmp = uri.substring(uri.lastIndexOf(".") );
            result = tmp;
        }
        return result;
    }
}
