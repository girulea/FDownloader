package com.example.root.downloader.entities;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by root on 19/11/16.
 */

public class Segment
{
    private long start;
    private long end;
    public Segment(long start, long end)
    {
        this.start = start;
        this.end = end;
    }

    public long getStart()
    {
        return start;
    }
    public long getEnd()
    {
        return end;
    }

    public JSONObject toJSONObject() throws JSONException {

        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("start", start);
            jsonObject.put("end", end);
        } catch (JSONException e) {
        }
        return  jsonObject;
    }
}
