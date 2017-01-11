package com.example.root.downloader.activities;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.root.downloader.R;
import com.example.root.downloader.entities.Crawler;
import com.example.root.downloader.entities.Downloader;
import com.example.root.downloader.entities.STATE;
import com.example.root.downloader.entities.STREAMABLE_TYPE;
import com.example.root.downloader.entities.StreamRequestListener;
import com.example.root.downloader.utils.CustomClickListener;
import com.example.root.downloader.utils.UTIL;
import com.example.root.downloader.entities.Download;

import java.util.ArrayList;

/**
 * Created by root on 21/11/16.
 */

public class DownloadRecyclerViewAdapter extends RecyclerView.Adapter<DownloadViewHolder>
{
    private ArrayList<Download> activeItems;
    private ArrayList<Download> savedItems;
    //private MediaController mediaController;
    private StreamRequestListener listener;
    public DownloadRecyclerViewAdapter(ArrayList<Download> savedItems, ArrayList<Download> activeItems, StreamRequestListener listener )
    {
        this.savedItems = savedItems;
        this.activeItems = activeItems;
        this.listener = listener;
    }

    public void setActiveItems( ArrayList<Download> activeItems )
    {
        this.activeItems = activeItems;
    }

    public void setSavedItems(ArrayList<Download> savedItems)
    {
        this.savedItems = savedItems;
    }
    @Override
    public DownloadViewHolder onCreateViewHolder(ViewGroup parent, int position)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate( R.layout.download_view, parent, false);
        Download download = null;
        if( position < activeItems.size() )
        {
            download = activeItems.get( position );
        }
        else if( position - activeItems.size() >= 0 )
        {
            position = position - activeItems.size();
            download = savedItems.get( position );
        }
        loadDetailOnViewHolder( download, view);
        return new DownloadViewHolder(view, download);
    }

    @Override
    public void onBindViewHolder(DownloadViewHolder holder, int position)
    {
        Download download = null;
        if( position < activeItems.size() )
        {
            download = activeItems.get( position );
        }
        else if( position - activeItems.size() >= 0 )
        {
            position = position - activeItems.size();
            download = savedItems.get( position );
        }
        loadDetailOnViewHolder( download, holder.v );
        holder.setDownloader(download);
    }

    @Override
    public int getItemCount()
    {
        return savedItems.size() +  activeItems.size();
    }

    private void loadDetailOnViewHolder( Download download, View view )
    {
        loadURI( download, view);
        if( download instanceof Downloader)
        {
            loadViewByDownloader( download, view );
        }
        else if( download instanceof Crawler )
        {
            loadViewByCrawler( download, view);
        }
        loadImageButtonForStreaming( download, view);  //nel caso che lo streaming sia possibile, viene caricato un pulsante in base al tipo di streaming ( video o audio )

    }

    private void loadViewByCrawler(Download download, View view)
    {
        hideLength( view );
        hideSeparator( view );
        hideDownloaded( view );
        hideProgressBar( view );
        hidePercent( view );
        loadName( download, view );
        hidePlayButton(view);
        loadCrawlingFunctionOnDeleteButton( download, view);
    }

    private void loadCrawlingFunctionOnDeleteButton(Download download, View view)
    {
        ImageButton deleteButton = ( ImageButton ) view.findViewById( R.id.deleteButton2 );
        deleteButton.setVisibility( View.VISIBLE );
        deleteButton.setImageResource( R.mipmap.ic_crawler );
        deleteButton.setOnClickListener(new CustomClickListener( download ) {
            @Override
            public void onClick(View view)
                {
                    listener.onRequestCrawling( download );
                }
        });
    }


    private void loadViewByDownloader(Download download, View view )
    {
        loadPercent(download,view);
        STATE state = download.getState();
        loadName( download, view );
        loadLength( download, view);
        if( state == STATE.NOT_STARTED )
        {
            hideSeparator( view );
            hideDownloaded( view );
            hideProgressBar( view );
            loadStartFunctionOnPlayButton( download, view);
            loadDeleteFunctionOnDeleteButton(download, view);
        }
        else if( state == STATE.STARTED )
        {
            loadDownloaded(download, view);
            loadProgressBar(download, view);
            loadSeparator(view);
            loadPauseFunctionOnPlayButton( download, view);
            loadStopFunctionOnDeleteButton(download,view);
        }
        else if( state == STATE.PAUSED)
        {
            loadDownloaded(download, view);
            loadProgressBar(download, view);
            loadSeparator(view);
            loadResumeFunctionOnPlayButton( download, view);
            loadStopFunctionOnDeleteButton(download, view);
        }
        else if( state == STATE.STOPPED)
        {
            loadDownloaded(download, view);
            loadProgressBar(download, view);
            loadSeparator(view);
            loadStartFunctionOnPlayButton(download, view);
            loadDeleteFunctionOnDeleteButton(download, view);
        }
        else if( state == STATE.FINISHED )
        {
            hideSeparator( view );
            hideDownloaded( view );
            hideProgressBar( view );
            if( download.isByteServing() )
            {
                loadRestartFunctionOnPlayButton( download, view );
            }
            else
            {
                hidePlayButton( view );
            }
            loadDeleteFunctionOnDeleteButton(download, view);
        }
    }


    private void loadImageButtonForStreaming(Download download, View view)
    {
        ImageButton imageButtonStreaming = ( ImageButton ) view.findViewById( R.id.imageButtonStream );
        if( download.isStreamable() && download.getStreamableType() == STREAMABLE_TYPE.VIDEO )
        {
            imageButtonStreaming.setVisibility( View.VISIBLE );
            imageButtonStreaming.setImageResource( R.mipmap.ic_see );
            imageButtonStreaming.setOnClickListener(new CustomClickListener( download ) {
                @Override
                public void onClick(View view)
                {
                    listener.onRequestPlayVideo( download );
                }
            });
        }
        else if( download.isStreamable() && download.getStreamableType() == STREAMABLE_TYPE.MUSIC )
        {
            imageButtonStreaming.setVisibility( View.VISIBLE );
            imageButtonStreaming.setImageResource( R.mipmap.ic_listen );
            imageButtonStreaming.setOnClickListener(new CustomClickListener( download ) {
                @Override
                public void onClick(View view)
                {
                    listener.onRequestPlayMusicTrack( download );
                }
            });
        }
        else
        {
            imageButtonStreaming.setVisibility( View.INVISIBLE );
        }
    }

    private void loadName( Download download, View view)
    {
        TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
        textViewName.setVisibility(View.VISIBLE);
        textViewName.setText(download.getFilename());
    }
    private void hideName( View view)
    {
        TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
        textViewName.setVisibility(View.INVISIBLE);
    }
    private void loadURI( Download download, View view)
    {
        TextView textViewURI = (TextView) view.findViewById(R.id.textViewURI);
        textViewURI.setText( download.getURI() );
    }
    private void loadLength( Download download, View view)
    {
        TextView textViewLength = (TextView) view.findViewById(R.id.textViewLength);
        textViewLength.setVisibility(View.VISIBLE);
        textViewLength.setText( UTIL.convertLengthToString( download.getLength() ) );
    }
    private void hideLength( View view)
    {
        TextView textViewLength = (TextView) view.findViewById(R.id.textViewLength);
        textViewLength.setVisibility(View.INVISIBLE);
    }
    private void loadDownloaded(Download download, View view )
    {
        TextView textViewDownloaded = (TextView)view.findViewById(R.id.textViewDownloaded);
        textViewDownloaded.setVisibility(View.VISIBLE);
        textViewDownloaded.setText( UTIL.convertLengthToString( download.getTotalReaded() ) );
    }
    private void hideDownloaded(View view)
    {
        TextView textViewDownloaded = (TextView)view.findViewById(R.id.textViewDownloaded);
        textViewDownloaded.setVisibility(View.INVISIBLE);
    }
    private void loadPercent( Download download, View view )
    {
        TextView textViewPercent = (TextView)view.findViewById(R.id.textViewPercent);
        textViewPercent.setVisibility(View.VISIBLE);
        long length = download.getLength();
        long totalReaded = download.getTotalReaded();
        if( length > 0 )
        {
            textViewPercent.setText(((100 *  totalReaded ) /  length ) + "%");
        }else
        {
            textViewPercent.setText("0%");
        }
    }
    private void hidePercent(View view)
    {
        TextView textViewPercent = (TextView)view.findViewById(R.id.textViewPercent);
        textViewPercent.setVisibility(View.INVISIBLE);
    }

    private void loadSeparator( View view)
    {
        TextView textViewSeparator = (TextView) view.findViewById(R.id.textViewSeparator);
        textViewSeparator.setVisibility(View.VISIBLE);
    }
    private void hideSeparator( View view )
    {
        TextView textViewSeparator = (TextView) view.findViewById(R.id.textViewSeparator);
        textViewSeparator.setVisibility(View.INVISIBLE);
    }

    private void loadStartFunctionOnPlayButton(Download download, View view)
    {
        ImageButton playButton = ( ImageButton ) view.findViewById(R.id.playButton);
        playButton.setVisibility(View.VISIBLE);
        playButton.setImageResource( R.mipmap.download_icon );
        playButton.setOnClickListener(new CustomClickListener( download ) {
            @Override
            public void onClick(View view)
            {
                download.addListenerUpdateDetails( DownloadService.getActualListener() );
                download.start();
            }
        });
    }
    private void loadPauseFunctionOnPlayButton( Download download, View view )
    {
        ImageButton playButton = ( ImageButton ) view.findViewById(R.id.playButton);
        playButton.setVisibility(View.VISIBLE);
        playButton.setImageResource( R.mipmap.pause_icon );
        playButton.setOnClickListener(new CustomClickListener(download)
        {
            @Override
            public void onClick( View view )
            {
                download.pause();
            }
        });
    }
    private void loadResumeFunctionOnPlayButton( Download download, View view )
    {
        ImageButton playButton = ( ImageButton ) view.findViewById(R.id.playButton);
        playButton.setVisibility(View.VISIBLE);
        playButton.setImageResource( R.mipmap.start_icon);
        playButton.setOnClickListener(new CustomClickListener(download)
        {
            @Override
            public void onClick( View view )
            {
                download.addListenerUpdateDetails( DownloadService.getActualListener() );
                download.resume();
            }
        });
    }
    private void loadRestartFunctionOnPlayButton(Download download, View view)
    {
        ImageButton playButton = ( ImageButton ) view.findViewById(R.id.playButton);
        playButton.setVisibility(View.VISIBLE);
        playButton.setImageResource(R.mipmap.restart_icon);
        playButton.setOnClickListener(new CustomClickListener(download) {
            @Override
            public void onClick(View view) {
                download.addListenerUpdateDetails(DownloadService.getActualListener());
                download.restart();
            }
        });
    }
    private void loadStopFunctionOnDeleteButton( Download download, View view )
    {
        ImageButton deleteButton = ( ImageButton ) view.findViewById(R.id.deleteButton2);
        deleteButton.setVisibility( View.VISIBLE );
        deleteButton.setImageResource( R.mipmap.stop_icon );
        deleteButton.setOnClickListener( new CustomClickListener(download)
        {
            @Override
            public void onClick( View view )
            {
                download.addListenerUpdateDetails( DownloadService.getActualListener() );
                download.stop();
            }
        });
    }
    private void loadDeleteFunctionOnDeleteButton( Download download, View view )
    {
        ImageButton deleteButton = ( ImageButton ) view.findViewById(R.id.deleteButton2);
        deleteButton.setVisibility(View.VISIBLE);
        deleteButton.setImageResource( R.mipmap.delete_icon );
        deleteButton.setOnClickListener( new CustomClickListener(download)
        {
            @Override
            public void onClick( View view )
            {
                download.addListenerUpdateDetails( DownloadService.getActualListener() );
                download.delete();
            }
        });
    }
    private void hidePlayButton(View view)
    {
        ImageButton playButton = ( ImageButton ) view.findViewById(R.id.playButton);
        playButton.setVisibility( View.INVISIBLE );
    }
    private void loadProgressBar( Download download, View view )
    {
        ProgressBar progressBar = ( ProgressBar ) view.findViewById(R.id.progressBarDownload);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax( (int) download.getLength() );
        progressBar.setProgress((int) download.getTotalReaded() );
    }
    private void hideProgressBar( View view )
    {
        ProgressBar progressBar = ( ProgressBar ) view.findViewById(R.id.progressBarDownload);
        progressBar.setVisibility(View.INVISIBLE);
    }
    public void notifyActiveSetChanged()
    {
        for( int i = 0; i < activeItems.size();i++)
        {
            notifyItemChanged( i );
        }
    }

    public void notifySavedSetChanged()
    {
        int offset = activeItems.size();
        for( int i = offset; i < savedItems.size() + offset;i++)
        {
            notifyItemChanged( i );
        }
    }


}
