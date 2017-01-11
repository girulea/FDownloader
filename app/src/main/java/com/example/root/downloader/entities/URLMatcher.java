package com.example.root.downloader.entities;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import com.example.root.downloader.activities.DownloadService;
import com.example.root.downloader.utils.UTIL;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Amerigo on 30/12/16.
 */

public class URLMatcher extends AsyncTask<String, Integer, ArrayList<String>>
{

    //String urlPattern = "http\\://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(/\\S*)?\n";
    String hrefPattern = "[href|src]=\"([^#\"]+?)\"";
    public static String[] INTEREST_EXTENSIONS = new String[]{"mp4","mp3","pdf","zip","rar","tar","flac"};
    private static String[] interestExtensionsForInstance;
    private ProgressBar progressBar;
    private String uri;
    private int numOfURIs;
    private URLMatcherListener listener;
    private String group;
    public URLMatcher(String uri, String group, URLMatcherListener listener)
    {
        this.uri = uri;
        this.listener = listener;
        this.group = group;
        interestExtensionsForInstance = INTEREST_EXTENSIONS;
    }

    public void setProgressBar( ProgressBar progressBar )
    {
        this.progressBar = progressBar;
    }

    public void setInterestExtensionsForInstance( String[] extensions )
    {
        interestExtensionsForInstance = extensions;
    }

    public static void setInterestExtensionsForClassifier( String[] extensions )
    {
        INTEREST_EXTENSIONS = extensions;
    }

    protected ArrayList<String> doInBackground(String... html)
    {
        ArrayList<String> result = new ArrayList<>();
        result.addAll( firstPhase( html[0] ) );
        //if( progressBar != null ) progressBar.setMax( result.size() );
        numOfURIs = result.size();
        result = secondPhase( result );
        //result.addAll( firstPhase( hrefPattern, html[0]));
        return result;
    }


    public ArrayList<String> firstPhase( String text )
    {
        ArrayList<String> result = new ArrayList<>();
        //Pattern pattern = Patterns.WEB_URL;
        Pattern pattern = Pattern.compile(hrefPattern);
        Matcher matcher = pattern.matcher(text);
        while( matcher.find() )
        {
            //Log.d("URLMatcher",matcher.group(1));
            result.add(matcher.group(1));
            matcher.end();
        }
        return result;
    }

    private ArrayList<String> secondPhase( ArrayList<String> URIs )
    {
        ArrayList<String> result = new ArrayList<>();
        for( String s : URIs )
        {
            if( UTIL.checkIfIsAvailableAndInteresting( s, interestExtensionsForInstance ) )
            {
                result.add( s );
                listener.onMatchedURI( s, group );
            }
            else
            {
                String tmp = uri + "";
                if( !s.startsWith("/") && !uri.endsWith("/") )
                {
                    tmp+="/";
                }
                tmp += s;
                if( UTIL.checkIfIsAvailableAndInteresting( tmp, interestExtensionsForInstance ) )
                {
                    result.add( tmp );
                    listener.onMatchedURI( tmp, group );
                }
            }
            publishProgress(1);
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        if( progressBar != null )
        {
            progressBar.setMax(numOfURIs);
            progressBar.setProgress( values[0] + progressBar.getProgress() );
        }
    }
    @Override
    protected void onPostExecute( ArrayList<String> result )
    {
        //listener.onFinishedMatches( result );
    }
}
