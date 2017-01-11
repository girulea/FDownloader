package com.example.root.downloader.entities;

import android.os.Environment;

import com.example.root.downloader.utils.UTIL;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Amerigo on 26/12/16.
 */

public class Crawler extends Download {

    private Crawler( String uri, String filename, long time )
    {
        this.uri = uri;
        this.time = time;
        this.filename = filename;
        this.downloadType = DownloadType.CRAWLER;
        prepareDownload();
    }

    @Override
    public void start()
    {

    }

    @Override
    void prepareDownload()
    {
        // create a File object for the parent directory
        File crawlerDirectory =new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), filename);
        boolean mkdirs = crawlerDirectory.mkdirs();
    }
    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void restart() {

    }

    @Override
    public void delete() {

    }

    @Override
    public String getAbsoluteFilePath() {
        return null;
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
    public void onProgressWorker(List<Byte> data, long start) throws IOException {

    }

    @Override
    public void onFinishWorker(Worker w) throws IOException {

    }

    @Override
    public void onInterruptedWorker(Worker w) {

    }

    @Override
    public void onErrorWorker(Worker worker, String errorMessage) {

    }

    @Override
    public void run()
    {

    }

    static Crawler createView( String uri, String filename, long time )
    {
        return new Crawler(uri, filename, time);
    }
}
