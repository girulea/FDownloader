package com.example.root.downloader.database;

import android.provider.BaseColumns;

/**
 * Created by root on 29/10/16.
 */

public final class EntryContract {

    private EntryContract() {}

    /* Inner class that defines the table contents */
    public static class DownloadEntry implements BaseColumns {
        public static final String TABLE_NAME = "DOWNLOAD";
        public static final String COLUMN_NAME_URL = "URL";
        public static final String COLUMN_NAME_NAME = "NAME";
        public static final String COLUMN_NAME_EXTENSION = "EXTENSION";
        public static final String COLUMN_NAME_SIZE = "SIZE";
        public static final String COLUMN_NAME_STATE = "STATE";
        public static final String COLUMN_NAME_SEGMENTS = "SEGMENTS";
        public static final String COLUMN_NAME_TOTAL_READED = "TOTALREADED";
        public static final String COLUMN_NAME_IS_BYTESERVING = "ISBYTESERVING";
        public static final String COLUMN_NAME_IS_STREAMABLE = "ISSTREAMABLE";
        public static final String COLUMN_NAME_DOWNLOAD_TYPE = "DOWNLOADTYPE";
        public static final String COLUMN_NAME_TIME = "TIME";
        public static final String COLUMN_NAME_GROUP = "GROUPS";
    }

}