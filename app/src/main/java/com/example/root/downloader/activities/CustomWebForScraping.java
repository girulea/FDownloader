package com.example.root.downloader.activities;

import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.example.root.downloader.entities.URLMatcher;
import com.example.root.downloader.entities.URLMatcherListener;

import java.util.Map;

/**
 * Created by Amerigo on 27/12/16.
 */

public class CustomWebForScraping extends WebViewClient
{
    private String[] interest_extensions;

    private String uri;
    private boolean started;
    private String HTML;
    private URLMatcherListener listener;
    private String group;
    public CustomWebForScraping(String uri, URLMatcherListener listener )
    {
        super();
        this.uri = uri;
        started = false;
        this.listener = listener;

    }
    @Override
    public WebResourceResponse shouldInterceptRequest (WebView view, WebResourceRequest request)
    {
        Map<String, String> requestHeaders = request.getRequestHeaders();
        if( requestHeaders.containsKey("Content-Type"))
        {
            String s = requestHeaders.get("Content-Type");
            Log.d("Crawler", s);
        }
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public void onPageFinished(WebView view, String uri)
    {
        view.loadUrl("javascript:window.DOMInterfaces.processHTML('<HTML>'+document.getElementsByTagName('HTML')[0].innerHTML+'</HTML>');");
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public class DOMInterfaces
    {
        @JavascriptInterface
        public void processHTML(String html)
        {
            HTML = html;
            //Log.d("DOMInterfaces","avviato nuovo url matcher");
            //new URLMatcher(uri, listener).execute(HTML);
        }
    }


    public void addCustomInterestExtensions( String[] interest_extensions)
    {
        this.interest_extensions = interest_extensions;
    }

    public boolean startScraping( @Nullable ProgressBar progressBar )
    {
        boolean result = false;
        if( !started && HTML != null )
        {
            URLMatcher urlMatcher = new URLMatcher( uri, group, listener);
            if( interest_extensions != null )
            {
                urlMatcher.setInterestExtensionsForInstance(interest_extensions);
            }
            if( progressBar != null ) { urlMatcher.setProgressBar( progressBar ); }
            urlMatcher.execute(HTML);
            result = true;
        }
        return result;
    }
}
