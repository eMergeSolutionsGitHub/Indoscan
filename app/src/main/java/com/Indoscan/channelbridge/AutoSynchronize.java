package com.Indoscan.channelbridge;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.Indoscan.channelbridgebs.AutoBackupService;
import com.Indoscan.channelbridgebs.DownloadCustomerImagesTask;
import com.Indoscan.channelbridgebs.DownloadCustomersTask;
import com.Indoscan.channelbridgebs.DownloadDealerSalesTask;
import com.Indoscan.channelbridgebs.DownloadItineraryTask;
import com.Indoscan.channelbridgebs.DownloadProductRepStoreTask;
import com.Indoscan.channelbridgebs.DownloadProductTask;
import com.Indoscan.channelbridgebs.UploadBackupDatabase;
import com.Indoscan.channelbridgebs.UploadCustomersImagesTask;
import com.Indoscan.channelbridgebs.UploadInvoiceChequesDetailsTask;
import com.Indoscan.channelbridgebs.UploadInvoiceHeaderTask;
import com.Indoscan.channelbridgebs.UploadInvoiceOutstandingTask;
import com.Indoscan.channelbridgebs.UploadInvoiceTask;
import com.Indoscan.channelbridgebs.UploadNewCustomersTask;
import com.Indoscan.channelbridgebs.UploadProductReturnsTask;
import com.Indoscan.channelbridgebs.UploadRetunHeaderTask;
import com.Indoscan.channelbridgebs.UploadShelfQtyTask;
import com.Indoscan.channelbridgedb.Reps;
import com.Indoscan.channelbridgedb.UserLogin;
import com.Indoscan.channelbridgedb.AutoSyncOnOffFlag;

public class AutoSynchronize extends Service {

    private static final String TAG = "AUTOSYNCHRONIZE";
    private final IBinder mBinder = new MyBinder();
    Context context;

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        context = getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // use this method to call upload web service

        super.onStartCommand(intent, flags, startId);

        System.out.println("onStartCommand ");

        Log.e(TAG, "onStartCommand");
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        boolean autoSyncActivate = preferences.getBoolean("AutoSyncRun", true);
        AutoSyncOnOffFlag autoSyncOnOffFlag = new AutoSyncOnOffFlag(AutoSynchronize.this);
        autoSyncOnOffFlag.openReadableDatabase();
        String dbStatus = autoSyncOnOffFlag.GetAutoSyncStatus();
        autoSyncOnOffFlag.closeDatabase();
        if (dbStatus.isEmpty())
            dbStatus = "0";
        Log.w("AUTOSYNC ACTIVATE?  ####### ", String.valueOf(autoSyncActivate) + "");

        if (isOnline() && autoSyncActivate) {

            String deviceId = "", repId = "";

            UserLogin login = new UserLogin(AutoSynchronize.this);
            login.openReadableDatabase();
            ArrayList<String[]> users = login.getAllUsersDetails();
            login.closeDatabase();

            if (users.size() > 0) {
                deviceId = users.get(0)[6];
            }

            Reps reps = new Reps(AutoSynchronize.this);
            reps.openReadableDatabase();
            ArrayList<String[]> repsDetails = reps.getAllRepsDetails();
            reps.closeDatabase();

            if (repsDetails.size() > 0) {
                repId = repsDetails.get(0)[1];
            }
            int flag = Integer.parseInt(dbStatus);
            if (deviceId != "" && repId != "" && flag == 0) {

                try {


                    System.out
                            .println("inside DownloadCustomerImagesTask task ");
                    new DownloadCustomerImagesTask(AutoSynchronize.this)
                            .execute("1");
                    Reps repController = new Reps(AutoSynchronize.this);
                    repController.openReadableDatabase();
                    String[] repks = repController.getRepDetails();
                    repController.closeDatabase();
                    System.out.println("inside UploadInvoiceTask Task ");
                    new UploadInvoiceTask(AutoSynchronize.this).execute(
                            deviceId, repId);
                    new UploadInvoiceHeaderTask(AutoSynchronize.this,repId,deviceId,repks[8]).execute();
                    System.out.println("inside UploadNewCustomersTask Task ");
                    new UploadNewCustomersTask(AutoSynchronize.this).execute(
                            deviceId, repId);

                    System.out.println("inside UploadProductReturnsTask Task ");
                    new UploadRetunHeaderTask(AutoSynchronize.this,repId,deviceId).execute();

                    new UploadProductReturnsTask(AutoSynchronize.this).execute(
                            deviceId, repId);

                    System.out.println("inside UploadShelfQtyTask Task ");
                    new UploadShelfQtyTask(AutoSynchronize.this).execute(
                            deviceId, repId);

                    System.out.println("inside DownloadCustomersTask Task ");
                    new DownloadCustomersTask(AutoSynchronize.this).execute(
                            deviceId, repId);

                    System.out.println("inside DownloadItineraryTask Task ");
                    new DownloadItineraryTask(AutoSynchronize.this).execute(
                            deviceId, repId);

                    System.out
                            .println("inside DownloadProductRepStoreTask Task ");
                    new DownloadProductRepStoreTask(AutoSynchronize.this)
                            .execute(deviceId, repId);

                    System.out.println("inside DownloadProductTask Task ");
                    new DownloadProductTask(AutoSynchronize.this).execute(
                            deviceId, repId);

                    new DownloadDealerSalesTask(AutoSynchronize.this).execute();

                    System.out
                            .println("inside UploadCustomersImagesTask Task ");
                    new UploadCustomersImagesTask(AutoSynchronize.this)
                            .execute("1");

                    if (preferences.getBoolean(AutoBackupService.BACKUP_REQUIRED, false)) {
                       // new UploadBackupDatabase(preferences.getString(AutoBackupService.BACKUP_NAME, ""), getApplicationContext()).execute();
                    }

                    SharedPreferences preferencesTwo = PreferenceManager
                            .getDefaultSharedPreferences(getBaseContext());
                    boolean chequeEnabled = preferencesTwo.getBoolean("cbPrefEnableCheckDetails", false);

                    if (chequeEnabled) {

                        System.out.println("inside UploadInvoiceOutstandingTask Task ");
                        new UploadInvoiceOutstandingTask(AutoSynchronize.this).execute(
                                deviceId, repId);

                        System.out.println("inside UploadInvoiceChequesDetailsTask Task ");
                        new UploadInvoiceChequesDetailsTask(AutoSynchronize.this).execute(
                                deviceId, repId);
                    }


                } catch (Exception e) {
                    // TODO: handle exception
                    Log.e(TAG, "Background service exception : " + e.toString());

                }
            } else
                System.out.println("Auto Synchronized disable when using manual Upload");

        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        System.out.println("onBind ");
        return mBinder;
    }

    public boolean isOnline() {
        boolean flag = false;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            flag = true;
        }
        return flag;
    }

    public class MyBinder extends Binder {
        AutoSynchronize getService() {
            System.out.println("getService ");
            return AutoSynchronize.this;
        }
    }

}
