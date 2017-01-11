package com.example.root.downloader.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.root.downloader.R;
import com.example.root.downloader.utils.UTIL;

/**
 * Created by Amerigo on 26/12/16.
 */

public class WebViewActivity extends AppCompatActivity implements DownloadListener, View.OnClickListener, TextWatcher
{
    private String uri;
    private WebView webView;
    private CustomWebForScraping webClient;
    private Button button;
    private String group;
    private EditText editTextExtension;
    @Override
    public void onCreate( Bundle bundle )
    {
        super.onCreate(bundle);
        setContentView( R.layout.activity_webview );
        Intent intent = getIntent();
        uri = intent.getStringExtra("URI");
        group = intent.getStringExtra("GROUP");
        webView = ( WebView ) findViewById( R.id.webview );

        webView.setDownloadListener(this);
        webView.setWebChromeClient( new WebChromeClient() );
        webClient = new CustomWebForScraping( uri, DownloadService.getActualListener() );
        webClient.setGroup( group );
        webView.setWebViewClient( webClient );
        webView.addJavascriptInterface( webClient.new DOMInterfaces(), "DOMInterfaces");
        webView.getSettings().setJavaScriptEnabled( true );
        //webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.loadUrl( uri );

        editTextExtension = (EditText) findViewById(R.id.editTextExtensions);
        editTextExtension.addTextChangedListener( this );
        button = (Button) findViewById(R.id.scraping_button);
        button.setOnClickListener(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack(); // Go to previous page
            return true;
        }
        // Use this as else part
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDownloadStart(String uri, String userAgent, String contentDisposition, String mimetype, long contentLength )
    {
        Toast.makeText(webView.getContext(),"Aggiunto nuovo download " + uri,Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this,DownloadService.class);
        intent.putExtra("URL", uri);
        startService(intent);
    }

    @Override
    public void onClick(View view)
    {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarForScraping);
        boolean result = webClient.startScraping( progressBar );
        if( result )
        {
            Toast.makeText(getApplicationContext(),"Scraping avviato con successo",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Scraping non avviato",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
    {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable)
    {
        String[] extensions = UTIL.convertStringToStringArray( editable.toString() );
        webClient.addCustomInterestExtensions( extensions );
    }
}
