package com.example.root.downloader.entities;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;


/**
 * Created by root on 08/12/16.
 */

public class FTPWorker extends Worker {

    public FTPWorker(String uri, long firstByte, long lastByte)
    {
        super(uri, firstByte, lastByte);
    }

    @Override
    public void start() {

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
    public void destroy() {

    }

    @Override
    public HashMap<String, Object> loadDetailsDownload(HashMap detail) throws IOException {
        return null;
    }

    @Override
    protected void createConnection() throws IOException {

    }

    @Override
    protected void download(InputStream inputStream) throws IOException, InterruptedException {

    }

    @Override
    public void run() {

    }
}
