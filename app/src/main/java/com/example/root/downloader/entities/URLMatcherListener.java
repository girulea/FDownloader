package com.example.root.downloader.entities;

import java.util.ArrayList;

/**
 * Created by Amerigo on 03/01/17.
 */

public interface URLMatcherListener
{
    void onFinishedMatches( ArrayList<String> URIs, String group );
    void onMatchedURI( String uri, String group );
}
