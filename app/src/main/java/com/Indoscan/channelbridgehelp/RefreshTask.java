package com.Indoscan.channelbridgehelp;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by Hasitha on 4/29/15.
 */
public class RefreshTask extends AsyncTask<Void,Void,Void> {

    private Context context;

    public RefreshTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {

        new HttpClientConnector(context).addVideoEntryToDB();
        return null;
    }
}