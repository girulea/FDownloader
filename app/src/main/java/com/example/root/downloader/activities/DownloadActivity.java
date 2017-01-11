package com.example.root.downloader.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.root.downloader.R;
import com.example.root.downloader.utils.UTIL;

/**
 * Created by root on 20/11/16.
 */

public class DownloadActivity extends AppCompatActivity implements View.OnClickListener
{
    private EditText eTextName;
    private EditText eTextURL;
    private EditText eTextThreads;
    private CheckBox checkBoxStreaming;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        eTextName = (EditText) findViewById(R.id.editTextName);
        eTextURL = (EditText) findViewById(R.id.editTextURL);
        eTextThreads = (EditText) findViewById(R.id.editTextThreads);
        checkBoxStreaming = ( CheckBox ) findViewById( R.id.checkBoxStreaming );
        Button button = (Button) findViewById(R.id.buttonStartDownload);
        button.setOnClickListener(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Intent intent = getIntent();
        if( intent.getData() != null )
        {
            String url = intent.getData().toString();
            eTextURL.setText(url);
        }
        eTextThreads.setText("8");
    }
    @Override
    public void onClick(View view)
    {
        Intent intent = new Intent(this,DownloadService.class);
        String url = eTextURL.getText().toString();
        if( UTIL.validateURL(url))
        {
            intent.putExtra("URL", url);
        }
        String name = eTextName.getText().toString();
        if( UTIL.validateFilename(name))
        {
            intent.putExtra("name", name );
        }
        String number = eTextThreads.getText().toString();
        if( UTIL.isValidNumber(number) )
        {
            intent.putExtra("threads", Integer.parseInt( number ));
        }
        if( checkBoxStreaming.isChecked() )
        {
            intent.putExtra("streaming",true);
        }
        startService(intent);
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

}
