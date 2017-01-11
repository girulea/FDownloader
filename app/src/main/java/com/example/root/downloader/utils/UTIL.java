package com.example.root.downloader.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Patterns;
import android.webkit.MimeTypeMap;

import com.example.root.downloader.entities.Download;
import com.example.root.downloader.entities.STREAMABLE_TYPE;
import com.example.root.downloader.entities.Segment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


/**
 * Created by root on 19/11/16.
 */

public class UTIL {


    private static final float KB = 1024;
    private static final float MB = 1048576;
    private static final float GB = 1073741824;

    private final static String[] videoExtensions = {"avi", "mp4", "flv", "mkv"};
    private final static String[] musicExtensions = {"mp3", "flac"};

    //in caso di non disponibilità, ci sarà un ritorno nullo
    public static boolean checkIfIsAvailable(String uri) {
        boolean result = false;
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(uri).openConnection();
            con.setRequestMethod("HEAD");
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                result = true;
            }
            con.disconnect();
        } catch (MalformedURLException e) {
            result = false;
        } catch (IOException e) {
            result = false;
            //e.printStackTrace();
        }
        return result;
    }

    public static boolean checkIfIsAvailableAndInteresting(String uri, String[] interestExtensions) {
        boolean result = false;
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(uri).openConnection();
            con.setRequestMethod("HEAD");
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String extension = MimeTypeMap.getFileExtensionFromUrl(uri);
                if (extension != null) {
                    for (int i = 0; i < interestExtensions.length; i++) {
                        if (interestExtensions[i].compareToIgnoreCase(extension) == 0) {
                            result = true;
                            i = interestExtensions.length;
                        }
                    }
                }
            }
            con.disconnect();
        } catch (MalformedURLException e) {
            result = false;
        } catch (IOException e) {
            result = false;
            //e.printStackTrace();
        }
        return result;
    }

    public static STREAMABLE_TYPE getStreamableTypeFromExtension(String extension) {
        STREAMABLE_TYPE result = null;
        for (String s : videoExtensions) {
            if (s.compareToIgnoreCase(extension) == 0) {
                result = STREAMABLE_TYPE.VIDEO;
            }
        }
        if (result == null) {
            for (String s : musicExtensions) {
                if (s.compareToIgnoreCase(extension) == 0) {
                    result = STREAMABLE_TYPE.MUSIC;
                }
            }
        }
        return result;
    }

    public static JSONArray convertTypedArrayListToJSONArray(ArrayList<Segment> segments) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        if (segments != null) {
            for (Segment segment : segments) {
                jsonArray.put(segment.toJSONObject());
            }
        }
        return jsonArray;
    }

    public static File getDownloadFile(String filename, String group)
    {
        File file;
        File tmpFile;
        if( group != null )
        {
            tmpFile =  new File ( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), group );
        }
        else
        {
            tmpFile = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS );
        }
        file = new File( tmpFile, filename);
        return file;
    }

    public static ArrayList<Segment> convertJSONStringToTypedArrayList(String jsonString) throws JSONException {
        JSONArray jsonArray = null;
        ArrayList<Segment> result = new ArrayList<>();
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (Exception e) {
            jsonArray = null;
            result = null;
        }
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                long start = object.getLong("start");
                long end = object.getLong("end");
                Segment segment = new Segment(start, end);
                result.add(segment);
            }
        }
        return result;
    }

    public static String convertLengthToString(long number) {
        String result = "";
        float tmp = number / GB;
        if (tmp >= 1) {
            result = String.format(Locale.ENGLISH, "%.2f", tmp);
            result += "GB";
        } else {
            tmp = number / MB;
            if (tmp >= 1) {
                result = String.format(Locale.ENGLISH, "%.2f", tmp);
                result += "MB";
            } else {
                result = String.format(Locale.ENGLISH, "%.2f", tmp);
                result += "KB";
            }
        }
        return result;
    }

    public static byte[] toByteArray(List<Byte> list) {
        byte[] result = new byte[list.size()];
        int index = 0;
        for (Byte b : list) {
            result[index++] = b;
        }
        return result;
    }

    public static boolean validateURL(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }

    public static boolean isValidNumber(String number) {
        boolean result = false;
        try {
            Integer.parseInt(number);
            result = true;
        } catch (NumberFormatException e) {
        }
        return result;
    }

    private static final char[] ReservedChars = "|\\?*<\":>+[]/'".toCharArray();

    public static boolean validateFilename(String filename) {
        boolean result = true;
        for (char c : ReservedChars) {
            if (filename.indexOf(c) != -1) {
                result = false;
            }
        }
        return result;
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(final Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public static Thread createStopwatch(IStopwatch stopwatch, long millis) {
        Thread thread = new Thread(new CustomRunnable(stopwatch, millis) {
            @Override
            public void run() {

                long current = System.currentTimeMillis();
                long pre = current;
                while (!Thread.interrupted()) {
                    try {
                        current = System.currentTimeMillis();
                        if ((current - pre) >= millis) {
                            stopwatch.onCompletedLap();
                            pre = current;
                        }
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    public static long getCurrentTimeMillis()
    {
        Calendar calendar = Calendar.getInstance( Locale.getDefault() );
        return calendar.getTimeInMillis();
    }
    public static Calendar convertTimeMillisToCalendar( long timeMillis )
    {
        Calendar cal = Calendar.getInstance( Locale.getDefault() );
        cal.setTimeInMillis( timeMillis );
        return cal;
    }
    public static void sortDownloadByTime(ArrayList<Download> list)
    {
        Collections.sort(list, (o1, o2) -> ( int ) ( o1.getTime() - o2.getTime() ));
    }

    public static String[] convertStringToStringArray(String s)
    {
        String[] result = null;
        try
        {
            result = s.split(",");
        }catch (Exception ignored)
        {}
        return result;
    }
}
