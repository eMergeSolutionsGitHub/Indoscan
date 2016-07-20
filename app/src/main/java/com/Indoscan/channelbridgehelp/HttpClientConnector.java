package com.Indoscan.channelbridgehelp;

import android.content.Context;
import android.util.Log;


import com.Indoscan.channelbridgedb.VideoList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 12/17/2014.
 */
public class HttpClientConnector {

    private String responseDataString;
    private HttpClient client;
    private List<String> artistList;
    private String baseurl = "http://192.168.2.77:8089/api.channelBridge.video/public/";
    // private String baseurl ="http://www.buddy.canbura.com/api";
    private List<NameValuePair> pairs;
    private String conversationId = null;
    private HttpPost request;
    private VideoList videoCaller;


    public HttpClientConnector(Context c) {
        videoCaller = new VideoList(c);
    }

    public String callToServer() throws IOException {
        Log.i("bot message end   =======================>", "msg");
        client = new DefaultHttpClient();

        HttpGet request = new HttpGet(baseurl);

        HttpResponse response = client.execute(request);
        if (response.getStatusLine().getStatusCode() == 200) {
            HttpEntity entity = response.getEntity();
            responseDataString = EntityUtils.toString(entity);
        }

        return responseDataString;
    }


    /**
     * commented due to bot change*
     */

    public void addVideoEntryToDB() {

        String receivedData = null;

        List<VideoObject> videoObjects = new ArrayList<VideoObject>();
        Log.i("bot message start   =======================>", "pppppppp");
        try {

            receivedData = callToServer();
            Log.i("response from server", receivedData);
            JSONObject botResponse = new JSONObject(receivedData);

            JSONArray arr = botResponse.getJSONArray("data");
            videoCaller.openWritableDatabase();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                videoCaller.insertVideo(o.getString("text"), o.getString("videoId"));
                videoObjects.add(new VideoObject(o.getString("text"), o.getString("videoId")));
            }
            videoCaller.closeDatabase();
            //  returnResponse = botResponse.getString("botsay");
            // conversationId = botResponse.getString("convo_id");


        } catch (JSONException e) {
            Log.e("Json error", e.toString());
        } catch (IOException e) {
            Log.e("io error", e.toString());
        }

        // return videoObjects;
    }
}
