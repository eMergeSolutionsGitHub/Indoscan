package com.Indoscan.channelbridge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.Indoscan.Entity.CreditPeriod;
import com.Indoscan.channelbridgebs.DownloadCustomerImagesTask;
import com.Indoscan.channelbridgebs.DownloadDealerSalesTask;
import com.Indoscan.channelbridgebs.Download_Branch;
import com.Indoscan.channelbridgebs.Download_DEL_Outstanding;
import com.Indoscan.channelbridgebs.Download_Master_Banks;
import com.Indoscan.channelbridgebs.Download_PaymentType;
import com.Indoscan.channelbridgebs.UploadCollectionNoteTask;
import com.Indoscan.channelbridgebs.UploadRetunHeaderTask;
import com.Indoscan.channelbridgedb.AutoSyncOnOffFlag;
import com.Indoscan.channelbridgedb.Customers;
import com.Indoscan.channelbridgedb.CustomersPendingApproval;
import com.Indoscan.channelbridgedb.DiscountStructures;
import com.Indoscan.channelbridgedb.ImageGallery;
import com.Indoscan.channelbridgedb.Invoice;
import com.Indoscan.channelbridgedb.InvoicePaymentType;
import com.Indoscan.channelbridgedb.InvoicedCheque;
import com.Indoscan.channelbridgedb.InvoicedProducts;
import com.Indoscan.channelbridgedb.Itinerary;
import com.Indoscan.channelbridgedb.ProductRepStore;
import com.Indoscan.channelbridgedb.ProductReturns;
import com.Indoscan.channelbridgedb.ProductUnload;
import com.Indoscan.channelbridgedb.Products;
import com.Indoscan.channelbridgedb.ShelfQuantity;
import com.Indoscan.channelbridgews.WebService;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SyncronizePreference extends PreferenceActivity {
    ProgressDialog dialog;
    ArrayList<CreditPeriod> responseCreditList;


    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(com.Indoscan.channelbridge.R.xml.syncronize_preference);
        responseCreditList = new ArrayList<>();
        findPreference("pSyncroniseProducts").setOnPreferenceClickListener(
                new PreferenceOnClickListener());
        findPreference("pSyncronizeCustomers").setOnPreferenceClickListener(
                new PreferenceOnClickListener());
        findPreference("pSyncronizeInventory").setOnPreferenceClickListener(
                new PreferenceOnClickListener());
        findPreference("pSyncronizeItinerary").setOnPreferenceClickListener(
                new PreferenceOnClickListener());
        findPreference("pUploadInvoices").setOnPreferenceClickListener(
                new PreferenceOnClickListener());
        findPreference("pUploadCustomers").setOnPreferenceClickListener(
                new PreferenceOnClickListener());
        findPreference("pUploadReturns").setOnPreferenceClickListener(
                new PreferenceOnClickListener());
        findPreference("pUploadShelfQuantity").setOnPreferenceClickListener(
                new PreferenceOnClickListener());
        //  findPreference("pUploadImages").setOnPreferenceClickListener(
        //         new PreferenceOnClickListener());
        findPreference("pUploadInvoiceOutstandings")
                .setOnPreferenceClickListener(new PreferenceOnClickListener());
        //   findPreference("pUploadInvoiceCheques").setOnPreferenceClickListener(
        //           new PreferenceOnClickListener());
        findPreference("pUploadProductUnload").setOnPreferenceClickListener(
                new PreferenceOnClickListener());
        findPreference("Dwonload_DEL_Outstanding").setOnPreferenceClickListener(
                new PreferenceOnClickListener());
        findPreference("Collection_Note").setOnPreferenceClickListener(
                new PreferenceOnClickListener());
        findPreference("credit_periods").setOnPreferenceClickListener(
                new PreferenceOnClickListener());
        findPreference("Upload_Collection_Note").setOnPreferenceClickListener(
                new PreferenceOnClickListener());
        findPreference("DownloadDealerSales").setOnPreferenceClickListener(
                new PreferenceOnClickListener());

        findPreference("DownloadFreeIssues").setOnPreferenceClickListener(
                new PreferenceOnClickListener());

    }

    private void CollectionNoteMaterDownload() {

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        String deviceId = sharedPreferences.getString("DeviceId", "-1");
        String repId = sharedPreferences.getString("RepId", "-1");

        InvoicePaymentType PaymentType = new InvoicePaymentType(
                SyncronizePreference.this);

        try {
            new Download_PaymentType(SyncronizePreference.this).execute(deviceId, repId);
        } catch (Exception e) {

        }

        try {
            new Download_Master_Banks(SyncronizePreference.this).execute(deviceId, repId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            new Download_Branch(SyncronizePreference.this).execute(deviceId, repId);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            new Download_Branch(SyncronizePreference.this).execute(deviceId, repId);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


    }

    public ArrayList<CreditPeriod> downloadCreditPeriods() {
        ArrayList<CreditPeriod> responseArr = new ArrayList<>();

        try {


        } catch (Exception e) {

            Log.e("cr web error", e.toString());
        }


        return responseArr;
    }

    private class DownloadCreditPeriods extends AsyncTask<Void, Void, Void> {

        String deviceId, repId;
        Context context;
        private ProgressDialog dialog;

        public DownloadCreditPeriods(Context context) {
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());
            deviceId = sharedPreferences.getString("DeviceId", "-1");
            repId = sharedPreferences.getString("RepId", "-1");
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            this.dialog.setMessage("Please wait");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            this.dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                WebService webService = new WebService();
                responseCreditList = webService.getCreditPeriods(deviceId, Integer.parseInt(repId));

            } catch (Exception e) {

                Log.e("cr web error", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            com.Indoscan.channelbridgedb.CreditPeriod periodController = new com.Indoscan.channelbridgedb.CreditPeriod(SyncronizePreference.this);
            if (responseCreditList != null) {
                for (CreditPeriod period : responseCreditList) {
                    periodController.addCreditPeriods(period);
                }
            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            Intent itineraryIntent = new Intent(this, ItineraryList.class);
            startActivity(itineraryIntent);
        }
        return super.onKeyDown(keyCode, event);
    }

    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {

            case 1:

                builder.setMessage("Unable to Upload data")
                        .setCancelable(false)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                });
                Dialog alert = builder.create();
                return alert;

            case 2:

                builder.setMessage("Data uploaded successfully")
                        .setCancelable(false)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                });
                Dialog alertTwo = builder.create();
                return alertTwo;

            case 3:

                builder.setMessage(
                        "There is no Internet Connectivity, Please check network connectivity.")
                        .setCancelable(false)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                });
                Dialog alertThree = builder.create();
                return alertThree;

            case 4:

                builder.setMessage("Theres no data to upload")
                        .setCancelable(false)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                });
                Dialog alertFour = builder.create();
                return alertFour;

            case 5:

                builder.setMessage("Data downloaded successfully")
                        .setCancelable(false)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                });
                Dialog alertFive = builder.create();
                return alertFive;

            case 6:

                builder.setMessage("Theres no data to download")
                        .setCancelable(false)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                });
                Dialog alertSix = builder.create();
                return alertSix;

            case 7:

                builder.setMessage("Unable to save data")
                        .setCancelable(false)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                });
                Dialog alertSeven = builder.create();
                return alertSeven;

            case 8:

                builder.setMessage("Data uploaded and sync with server successfully")
                        .setCancelable(false)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                });
                Dialog alertEight = builder.create();
                return alertEight;

            case 9:

                builder.setMessage("Theres no data to upload but sync with server successfully")
                        .setCancelable(false)
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                });
                Dialog alertNine = builder.create();
                return alertNine;

            default:
                break;

        }

        return null;
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

    public String changeDateFormat(String date) {

        Log.w("Date in Change date format: ", date);

        date = date.substring(0, 10);

        Log.w("Date in Change date format: ", date);

        SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat myFormat = new SimpleDateFormat("MM/dd/yyyy");
        String reformattedStr = "";
        Log.w("Date in Change date format: ", date);
        try {

            reformattedStr = myFormat.format(fromUser.parse(date));
        } catch (ParseException e) {
            Log.w("Date in Change date format: ParseException ", e.toString());
            e.printStackTrace();
        }
        Log.w("Date in Change date format: ", date);
        return reformattedStr;
    }

    private void dissmissProgressWithError(ProgressDialog progressDialog) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        runOnUiThread(new Runnable() {

            public void run() {
                Toast.makeText(SyncronizePreference.this, "Connection failed", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private class PreferenceOnClickListener implements
            Preference.OnPreferenceClickListener {
        public boolean onPreferenceClick(Preference preference) {
            // Do something...


            AutoSyncOnOffFlag autoSyncOnOffFlag = new AutoSyncOnOffFlag(SyncronizePreference.this);
            autoSyncOnOffFlag.openReadableDatabase();
            autoSyncOnOffFlag.AutoSyncActive(1);
            autoSyncOnOffFlag.closeDatabase();


            SharedPreferences preferencesTwo = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());
            boolean chequeEnabled = preferencesTwo.getBoolean(
                    "cbPrefEnableCheckDetails", true);


            if (preference.getKey().equals("pSyncroniseProducts")) {
                Log.w("Log", "PreferenceOnClickListener pSyncroniseProducts");
                new DownloadProductsTask(SyncronizePreference.this)
                        .execute("1");

            } else if (preference.getKey().equals("pSyncronizeCustomers")) {
                Log.w("Log", "PreferenceOnClickListener pSyncronizeCustomers");
                new DownloadCustomersTask(SyncronizePreference.this)
                        .execute("1");

            } else if (preference.getKey().equals("pSyncronizeInventory")) {
                Log.w("Log", "PreferenceOnClickListener pSyncronizeInventory");
                new DownloadProductRepStoreTask(SyncronizePreference.this)
                        .execute("1");

            } else if (preference.getKey().equals("pSyncronizeItinerary")) {
                Log.w("Log", "PreferenceOnClickListener pSyncronizeItinerary");
                new DownloadItineraryTask(SyncronizePreference.this)
                        .execute("1");

            } else if (preference.getKey().equals("pUploadInvoices")) {
                Log.w("Log", "PreferenceOnClickListener pUploadInvoices");
                // new Upload
                new UploadInvoiceTask(SyncronizePreference.this).execute("1");

            } else if (preference.getKey().equals("pUploadCustomers")) {
                Log.w("Log", "PreferenceOnClickListener pUploadCustomers");
                new UploadNewCustomersTask(SyncronizePreference.this)
                        .execute("1");

            } else if (preference.getKey().equals("pUploadReturns")) {
                Log.w("Log", "PreferenceOnClickListener pUploadCustomers");

                SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
                String deviceId = sharedPreferences.getString("DeviceId", "-1");
                String repId = sharedPreferences.getString("RepId", "-1");
                new UploadRetunHeaderTask(SyncronizePreference.this, repId, deviceId).execute();
                new UploadProductReturnsTask(SyncronizePreference.this)
                        .execute("1");

            } else if (preference.getKey().equals("pUploadShelfQuantity")) {
                Log.w("Log", "PreferenceOnClickListener pUploadShelfQuantity");
                new UploadShelfQtyTask(SyncronizePreference.this).execute("1");

            } else if (preference.getKey().equals("pUploadInvoiceOutstandings")) {
                Log.w("Log",
                        "PreferenceOnClickListener pUploadInvoiceOutstandings");

                if (chequeEnabled) {
                    new UploadInvoiceOutstandingTask(SyncronizePreference.this)
                            .execute("1");
                } else {
                    Toast discountPercentageMin = Toast.makeText(
                            getApplication(),
                            "This function is not available on your version.",
                            Toast.LENGTH_SHORT);
                    discountPercentageMin.show();
                }

            } else if (preference.getKey().equals("pUploadInvoiceCheques")) {
                if (chequeEnabled) {
                    Log.w("Log",
                            "PreferenceOnClickListener pUploadInvoiceCheques");
                    new UploadInvoicedChequesTask(SyncronizePreference.this)
                            .execute("1");
                } else {
                    Toast discountPercentageMin = Toast.makeText(
                            getApplication(),
                            "This function is not available on your version.",
                            Toast.LENGTH_SHORT);
                    discountPercentageMin.show();
                }

            } else if (preference.getKey().equals("pUploadImages")) {
                Log.w("Log", "PreferenceOnClickListener pUploadImages");
                new UploadCustomerImageTask(SyncronizePreference.this)
                        .execute("1");

            } else if (preference.getKey().equals("pUploadProductUnload")) {
                Log.w("Log", "PreferenceOnClickListener pUploadProductUnload");
                new UploadProductUnloadTask(SyncronizePreference.this)
                        .execute("1");

            } else if (preference.getKey().equals("Dwonload_DEL_Outstanding")) {
                Log.w("Log", "PreferenceOnClickListener Dwonload_DEL_Outstanding");
                new Download_DEL_Outstanding(SyncronizePreference.this).execute();

            } else if (preference.getKey().equals("Collection_Note")) {
                Log.w("Log", "PreferenceOnClickListener Collection_Note");
                CollectionNoteMaterDownload();

            } else if (preference.getKey().equals("credit_periods")) {
                new DownloadCreditPeriods(SyncronizePreference.this).execute();

            } else if (preference.getKey().equals("Upload_Collection_Note")) {
                Log.w("Log", "PreferenceOnClickListener Upload Collection Note");
                new UploadCollectionNoteTask(SyncronizePreference.this).execute();

            } else if (preference.getKey().equals("DownloadDealerSales")) {
                new DownloadDealerSalesTask(SyncronizePreference.this).execute();
            } else if (preference.getKey().equals("DownloadFreeIssues")) {
                new DownloadFreeIsues(SyncronizePreference.this).execute();
            }


            return false;
        }
    }

    private class UploadInvoiceTask extends AsyncTask<String, Integer, Integer> {

        private final Context context;

        public UploadInvoiceTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage("Upload Invoices to Server...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setProgress(0);

            dialog.setMax(100);
            dialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
            switch (progress[0]) {
                case 1:
                    Log.w("Log", "yyyyyyyyyy: ");
                    dialog.setMessage("Loading data from Tab");
                    break;
                case 2:
                    dialog.setMessage("Uploading data to the server...");
                    break;
                default:
                    break;
            }
        }

        protected void onPostExecute(Integer returnCode) {
            dialog.dismiss();
            showDialog(returnCode);

        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub

            int returnValue = 1;

            Log.w("Log", "param result : " + params[0]);

            Log.w("Log", "loadProductRepStoreData result : starting ");

            if (isOnline()) {

                publishProgress(1);

                Invoice invoiceObject = new Invoice(SyncronizePreference.this);
                invoiceObject.openReadableDatabase();

                List<String[]> invoice = invoiceObject
                        .getInvoicesByStatus("false");
                invoiceObject.closeDatabase();

                Log.w("Log", "invoice size :  " + invoice.size());

                for (String[] invoiceData : invoice) {

                    Log.w("Log", "invoice id :  " + invoiceData[0]);
                    Log.w("Log", "invoice date :  " + invoiceData[10]);

                    ArrayList<String[]> invoicedProductDetailList = new ArrayList<String[]>();

                    InvoicedProducts invoicedProductsObject = new InvoicedProducts(
                            SyncronizePreference.this);
                    invoicedProductsObject.openReadableDatabase();
                    List<String[]> invoicedProducts = invoicedProductsObject.getInvoicedProductsByInvoiceId(invoiceData[0]);

                    invoicedProductsObject.closeDatabase();

                    Log.w("Log",
                            "invoicedProducts size :  "
                                    + invoicedProducts.size());

                    for (String[] invoicedProduct : invoicedProducts) {

                        ProductRepStore productRepStore = new ProductRepStore(
                                SyncronizePreference.this);
                        productRepStore.openReadableDatabase();
                        String[] productRepStor = productRepStore.getProductDetailsByProductBatchAndProductCode(invoicedProduct[3], invoicedProduct[2]);
                        productRepStore.closeDatabase();

                        Log.w("Log", "batch :  " + invoicedProduct[3]);

                        Log.w("Log", "exp :  " + productRepStor[5]);

                        Products product = new Products(SyncronizePreference.this);
                        product.openReadableDatabase();
                        String[] productData = product.getProductDetailsByProductCode(invoicedProduct[2]);
                        product.closeDatabase();

                        Itinerary itinerary = new Itinerary(SyncronizePreference.this);
                        itinerary.openReadableDatabase();

                        String tempCust = itinerary.getItineraryStatus(invoiceData[1]);
                        itinerary.closeDatabase();

                        String custNo = "";

                        Itinerary itineraryTwo = new Itinerary(SyncronizePreference.this);
                        itineraryTwo.openReadableDatabase();

                        if (tempCust.equals("true")) {

                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            String deviceId = sharedPreferences.getString("DeviceId", "-1");

                            String[] itnDetails = itineraryTwo.getItineraryDetailsForTemporaryCustomer(invoiceData[1]);
                            custNo = deviceId + "_"
                                    + itnDetails[7];// this is where yu have to
                            // change..!!
                        } else {
                            String[] itnDetails = itineraryTwo.getItineraryDetailsById(invoiceData[1]);
                            custNo = itnDetails[4];
                        }

                        itineraryTwo.closeDatabase();

                        if (invoicedProduct[7] != ""
                                && Integer.parseInt(invoicedProduct[7]) > 0) {

                            String[] invoiceDetails = new String[18];

                            int qty = Integer.parseInt(invoicedProduct[7]);
                            double purchasePrice = 0;
                            double selleingPrice = 0;
                            if (productData[12] != null && productData[12].length() > 0) {
                                purchasePrice = Double.parseDouble(productData[12]);
                            }
                            if (productData[13] != null
                                    && productData[13].length() > 0) {
                                selleingPrice = Double
                                        .parseDouble(productData[13]);
                            }

                            double profit = (selleingPrice * qty)
                                    - (purchasePrice * qty);

                            Log.w("Log", "profit :  " + profit);

                            invoiceDetails[0] = invoicedProduct[2]; // Product
                            // code
                            invoiceDetails[1] = invoicedProduct[1]; // Invoice
                            // Id
                            invoiceDetails[2] = "N"; // Issue mode
                            invoiceDetails[3] = invoicedProduct[7]; // Normal
                            // qty
                            Log.w("Invoice Date: ########", invoiceData[10]);

                            // invoiceDetails[4] =
                            // changeDateFormat(invoiceData[10]); // Invoice
                            // date
                            Log.w("Invoice Date: ######## Payment type",
                                    invoiceData[2]);
                            invoiceDetails[5] = invoiceData[2]; // Payment type

                            Log.w("Invoice Date: ########", productRepStor[5]);
                            invoiceDetails[6] = changeDateFormat(productRepStor[5]); // Expire
                            // date
                            invoiceDetails[7] = invoicedProduct[3]; // Batch no
                            invoiceDetails[8] = custNo; // Customer no
                            invoiceDetails[9] = String.valueOf(profit); // Profit
                            invoiceDetails[10] = productData[13]; // Unit price
                            invoiceDetails[11] = invoicedProduct[6]; // Discount
                            invoiceDetails[12] = invoicedProduct[0]; // Id
                            invoiceDetails[13] = invoiceData[11]; // Invoice
                            invoiceDetails[14] = invoiceData[16];
                            invoiceDetails[15] = invoiceData[15];                                    // time
                            invoiceDetails[16] = invoicedProduct[4];
                            invoiceDetails[17] = invoicedProduct[10];


                            invoicedProductDetailList.add(invoiceDetails);

                        }

                        if (invoicedProduct[5] != ""
                                && Integer.parseInt(invoicedProduct[5]) > 0) {

                            String[] invoiceDetails = new String[18];

                            invoiceDetails[0] = invoicedProduct[2]; // Product
                            // code
                            invoiceDetails[1] = invoicedProduct[1]; // Invoice
                            // Id
                            invoiceDetails[2] = "F"; // Issue mode
                            invoiceDetails[3] = invoicedProduct[5]; // Normal
                            // qty

                            // invoiceDetails[4] =
                            // changeDateFormat(invoiceData[10]); // Invoice
                            // date
                            invoiceDetails[5] = invoiceData[2]; // Payment type

                            invoiceDetails[6] = changeDateFormat(productRepStor[5]);
                            ; // Expire
                            // date
                            invoiceDetails[7] = invoicedProduct[3]; // Batch no
                            invoiceDetails[8] = custNo; // Customer no
                            invoiceDetails[9] = "0.00"; // Profit
                            invoiceDetails[10] = "0"; // Unit price
                            invoiceDetails[11] = invoicedProduct[6]; // Discount
                            invoiceDetails[12] = invoicedProduct[0]; // Id
                            invoiceDetails[13] = invoiceData[11]; // Invoice
                            invoiceDetails[14] = invoiceData[16];
                            invoiceDetails[15] = invoiceData[15];                                    // time
                            invoiceDetails[16] = invoicedProduct[4];
                            invoiceDetails[17] = invoicedProduct[10];

                            invoicedProductDetailList.add(invoiceDetails);

                        }

                    }

                    Log.w("Log", "invoicedProductDetailList size :  ");

                    Log.w("Log", "invoicedProductDetailList size :  "
                            + invoicedProductDetailList.size());

                    publishProgress(2);
                    String responseArr = null;
                    while (responseArr == null) {
                        try {

                            SharedPreferences sharedPreferences = PreferenceManager
                                    .getDefaultSharedPreferences(getBaseContext());
                            String deviceId = sharedPreferences.getString("DeviceId", "-1");
                            String repId = sharedPreferences.getString("RepId", "-1");

                            WebService webService = new WebService();
                            responseArr = webService.uploadInvoiceDetails(
                                    deviceId,
                                    repId,
                                    invoicedProductDetailList);

                            Thread.sleep(100);

                        } catch (SocketException e) {
                            e.printStackTrace();
                            dissmissProgressWithError(dialog);
                            return 0;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            dissmissProgressWithError(dialog);
                            return 0;
                        }


                    }

                    Log.w("Log",
                            "update data result : "
                                    + responseArr.contains("No Error"));
                    if (responseArr.contains("No Error")) {

                        Log.w("Log", "Update the iternarary status");

                        Invoice invoiceObj = new Invoice(
                                SyncronizePreference.this);
                        invoiceObj.openReadableDatabase();
                        invoiceObj.setInvoiceUpdatedStatus(invoiceData[0],
                                "true");
                        invoiceObj.closeDatabase();

                        returnValue = 2;

                    }

                }

                if (invoice.size() < 1) {

                    returnValue = 4;
                }

            } else {

                returnValue = 3;
            }
            AutoSyncOnOffFlag autoSyncOnOffFlag = new AutoSyncOnOffFlag(SyncronizePreference.this);
            autoSyncOnOffFlag.openReadableDatabase();
            autoSyncOnOffFlag.AutoSyncActive(0);
            autoSyncOnOffFlag.closeDatabase();


            return returnValue;

        }

    }

    private class UploadNewCustomersTask extends AsyncTask<String, Integer, Integer> {

        private final Context context;

        public UploadNewCustomersTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage("upload data to Server...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setProgress(0);

            dialog.setMax(100);
            dialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
            switch (progress[0]) {
                case 1:
                    Log.w("Log", "yyyyyyyyyy: ");
                    dialog.setMessage("Loading data from Tab");
                    break;
                case 2:
                    dialog.setMessage("Uploading data to the server...");
                    break;
                default:
                    break;
            }
        }

        protected void onPostExecute(Integer returnCode) {
            dialog.dismiss();
            showDialog(returnCode);

        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub

            int returnValue = 1;

            Log.w("Log", "param result : " + params[0]);

            Log.w("Log", "loadProductRepStoreData result : starting ");

            if (isOnline()) {

                publishProgress(1);

                CustomersPendingApproval rtnProdObject = new CustomersPendingApproval(
                        SyncronizePreference.this);
                rtnProdObject.openReadableDatabase();

                List<String[]> rtnProducts = rtnProdObject
                        .getCustomersByUploadStatus("false");
                rtnProdObject.closeDatabase();

                Log.w("Log", "rtnProducts size :  " + rtnProducts.size());

                ArrayList<String[]> invoicedProductDetailList = new ArrayList<String[]>();

                SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
                String deviceId = sharedPreferences.getString("DeviceId", "-1");
                String repId = sharedPreferences.getString("RepId", "-1");

                for (String[] rtnProdData : rtnProducts) {

                    Log.w("Log", "rtnProducts id :  " + rtnProdData[0]);
                    // Log.w("Log", "rtnProducts date :  " + rtnProdData[10]);

                    String[] invoiceDetails = new String[24];

                    invoiceDetails[0] = deviceId + "_"
                            + rtnProdData[0];
                    invoiceDetails[1] = rtnProdData[1];
                    invoiceDetails[2] = rtnProdData[2];
                    invoiceDetails[3] = rtnProdData[3];
                    invoiceDetails[4] = rtnProdData[4];
                    invoiceDetails[5] = rtnProdData[5];
                    invoiceDetails[6] = rtnProdData[6];
                    invoiceDetails[7] = rtnProdData[7];
                    invoiceDetails[8] = rtnProdData[8];
                    invoiceDetails[9] = rtnProdData[9];
                    invoiceDetails[10] = rtnProdData[11];
                    invoiceDetails[11] = rtnProdData[12];
                    invoiceDetails[12] = rtnProdData[13];
                    invoiceDetails[13] = rtnProdData[15];
                    invoiceDetails[14] = rtnProdData[14];
                    invoiceDetails[15] = rtnProdData[16];
                    invoiceDetails[16] = rtnProdData[17];
                    invoiceDetails[17] = rtnProdData[18];
                    invoiceDetails[18] = rtnProdData[20];
                    invoiceDetails[19] = rtnProdData[21];
                    invoiceDetails[20] = rtnProdData[22];
                    Log.w("Log", "rtnProducts id :  " + rtnProdData[0]);

                    ImageGallery imageGallery = new ImageGallery(
                            SyncronizePreference.this);
                    imageGallery.openReadableDatabase();
                    Log.w("Log", "rtnProducts id :  " + rtnProdData[0]);
                    String primaryImage = imageGallery
                            .getPrimaryImageforCustomerId(rtnProdData[0]);
                    imageGallery.closeDatabase();

                    Log.w("Log", "rtnProducts id :  " + primaryImage);

                    Log.w("Primary Image", primaryImage + "");
                    File customerImageFile = new File(
                            Environment.getExternalStorageDirectory() + File.separator
                                    + "DCIM" + File.separator + "Channel_Bridge_Images"
                                    + File.separator + primaryImage);
                    if (customerImageFile.exists()) {

                        try {

                            Bitmap bm = ImageHandler.decodeSampledBitmapFromResource(String.valueOf(customerImageFile), 400, 550);
                            rtnProdData[24] = ImageHandler.encodeTobase64(bm);
                        } catch (IllegalArgumentException e) {
                            Log.w("Illegal argument exception", e.toString());
                        } catch (OutOfMemoryError e) {
                            Log.w("Out of memory error :(", e.toString());
                        }

                    }
                    invoiceDetails[20] = primaryImage;

                    invoiceDetails[21] = rtnProdData[10];
                    invoiceDetails[22] = rtnProdData[23];
                    invoiceDetails[23] = rtnProdData[24];
                    Log.w("Log", "rtnProducts id :  " + primaryImage);

                    publishProgress(2);
                    String responseArr = null;
                    while (responseArr == null) {
                        try {

                            WebService webService = new WebService();
                            responseArr = webService.uploadNewCustomerDetails(
                                    deviceId,
                                    repId, invoiceDetails);

                            Thread.sleep(100);

                        } catch (SocketException e) {
                            e.printStackTrace();
                            dissmissProgressWithError(dialog);
                            return 0;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            dissmissProgressWithError(dialog);
                            return 0;
                        }

                    }

                    Log.w("Log", "update data result : " + responseArr);

                    Log.w("Log",
                            "update data result : "
                                    + responseArr.contains("Successfully"));
                    if (responseArr.contains("Ok")) {

                        Log.w("Log", "Update the iternarary status");

                        CustomersPendingApproval rtnProdObj = new CustomersPendingApproval(
                                SyncronizePreference.this);
                        rtnProdObj.openReadableDatabase();
                        rtnProdObj.setCustomerUploadedStatus(rtnProdData[0],
                                "true");
                        rtnProdObj.closeDatabase();

                        returnValue = 2;

                    }

                    Log.w("Log", "loadProductRepStoreData result : "
                            + responseArr);

                }

                Log.w("Log", "invoicedProductDetailList size :  "
                        + invoicedProductDetailList.size());

                if (rtnProducts.size() < 1) {

                    returnValue = 4;
                }

            } else {

                returnValue = 3;
            }
            AutoSyncOnOffFlag autoSyncOnOffFlag = new AutoSyncOnOffFlag(SyncronizePreference.this);
            autoSyncOnOffFlag.openReadableDatabase();
            autoSyncOnOffFlag.AutoSyncActive(0);
            autoSyncOnOffFlag.closeDatabase();
            return returnValue;

        }

    }

    private class UploadProductReturnsTask extends AsyncTask<String, Integer, Integer> {

        private final Context context;

        public UploadProductReturnsTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage("upload data to Server...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setProgress(0);

            dialog.setMax(100);
            dialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
            switch (progress[0]) {
                case 1:
                    Log.w("Log", "yyyyyyyyyy: ");
                    dialog.setMessage("Loading data from Tab");
                    break;
                case 2:
                    dialog.setMessage("Uploading data to the server...");
                    break;
                default:
                    break;
            }
        }

        protected void onPostExecute(Integer returnCode) {
            dialog.dismiss();
            showDialog(returnCode);

        }

        @Override
        protected Integer doInBackground(String... params) {
            int returnValue = 1;

            Log.w("Log", "param result : " + params[0]);

            Log.w("Log", "loadProductRepStoreData result : starting ");

            if (isOnline()) {

                publishProgress(1);

                String timeStamp = new SimpleDateFormat("yyyy")
                        .format(new Date());

                // int year = new Date().getYear();

                ProductReturns rtnProdObject = new ProductReturns(
                        SyncronizePreference.this);
                rtnProdObject.openReadableDatabase();

                List<String[]> rtnProducts = rtnProdObject
                        .getProductReturnsByStatus("false");
                rtnProdObject.closeDatabase();

                Log.w("Log", "rtnProducts size :  " + rtnProducts.size());

                for (String[] rtnProdData : rtnProducts) {

                    Log.w("Log", "rtnProducts id :  " + rtnProdData[0]);
                    // Log.w("Log", "rtnProducts date :  " + rtnProdData[10]);

                    Products product = new Products(SyncronizePreference.this);
                    product.openReadableDatabase();
                    String[] productData = product
                            .getProductDetailsByProductCode(rtnProdData[1]);
                    product.closeDatabase();

                    ProductRepStore productRepStore = new ProductRepStore(
                            SyncronizePreference.this);
                    productRepStore.openReadableDatabase();
                    String[] productRepStor = productRepStore
                            .getProductDetailsByProductBatchAndProductCode(
                                    rtnProdData[2], rtnProdData[1]);
                    productRepStore.closeDatabase();

                    ArrayList<String[]> returnedProductList = new ArrayList<String[]>();

                    String[] invoiceDetails = new String[14];

                    invoiceDetails[0] = rtnProdData[1]; // Product
                    // code

                    Log.w("Log", "rtnProducts validated :  " + rtnProdData[13]);

                    Log.w("Log123", "rtnProducts Status :  " + rtnProdData[13]
                            + rtnProdData[13].equals("false") + "  "
                            + timeStamp);

                    // if (rtnProdData[13].equals("false")) {
                    // invoiceDetails[1] = timeStamp+rtnProdData[3]; // Invoice
                    // // Id
                    // }else{
                    invoiceDetails[1] = rtnProdData[3]; // Invoice
                    // Id
                    // }

                    String issueMode = rtnProdData[4];

//                    if (rtnProdData[4].equalsIgnoreCase("resalable")) {
//                        issueMode = "RS";
//                    } else if (rtnProdData[4].equalsIgnoreCase("company_returns")) {
//                        issueMode = "CR";
//                    }

                    invoiceDetails[2] = issueMode; // Issue mode
//					invoiceDetails[2] = "R"; // Issue mode
                    invoiceDetails[3] = rtnProdData[5]; // Normal
                    // qty
                    invoiceDetails[4] = changeDateFormat(rtnProdData[7]);
                    ; // Rtn date

                    Log.w("Log", "productRepStor[5] 3@@$@ :  "
                            + productRepStor[5]);

                    if (productRepStor[5] == null || productRepStor[5] == "") {
                        invoiceDetails[5] = changeDateFormat("2015-01-01 10:13:59.790"); // expire
                        // date
                    } else {
                        invoiceDetails[5] = changeDateFormat(productRepStor[5]); // expire
                        // date
                    }

                    invoiceDetails[6] = rtnProdData[2]; // batch no
                    // date
                    invoiceDetails[7] = rtnProdData[8]; // Batch no

                    invoiceDetails[8] = rtnProdData[10]; // Unit price
                    if (invoiceDetails[8] == null || invoiceDetails[8] == "") {
                        invoiceDetails[8] = productData[14]; // Unit price
                    }

                    invoiceDetails[9] = rtnProdData[0]; // Id

                    invoiceDetails[10] = rtnProdData[11]; // Discount
                    invoiceDetails[11] = rtnProdData[14];
                    invoiceDetails[12] = rtnProdData[15];
                    invoiceDetails[13] = rtnProdData[16];
                    returnedProductList.add(invoiceDetails);

                    Log.w("Log", "Test ##### 11:  " + rtnProdData[11]);

                    Log.w("Log", "Test ##### :  " + rtnProdData[6]);

                    Log.w("Log",
                            "Test #####  bool :  "
                                    + (rtnProdData[6] != null && Integer
                                    .parseInt(rtnProdData[6]) > 0));

                    if (rtnProdData[6] != null
                            && Integer.parseInt(rtnProdData[6]) > 0) {

                        String[] invoiceDetailsFree = new String[14];

                        invoiceDetailsFree[0] = rtnProdData[1]; // Product
                        // code
                        // if
                        // (rtnProdData[13].equals("false"))
                        // {
                        // invoiceDetailsFree[1] = timeStamp+rtnProdData[3]; //
                        // Invoice
                        // // Id
                        // }else{
                        invoiceDetailsFree[1] = rtnProdData[3]; // Invoice
                        // Id
                        // }
                        invoiceDetailsFree[2] = "RF"; // Issue mode
                        invoiceDetailsFree[3] = rtnProdData[6]; // Free qty
                        invoiceDetailsFree[4] = changeDateFormat(rtnProdData[7]); // Rtn
                        // date

                        Log.w("Log", "productRepStor[5] 3### :  "
                                + productRepStor[5]);

                        if (productRepStor[5] == null
                                || productRepStor[5] == "") {
                            invoiceDetailsFree[5] = changeDateFormat("2015-01-01 10:13:59.790"); // expire
                            // date
                        } else {
                            invoiceDetailsFree[5] = changeDateFormat(productRepStor[5]); // expire
                            // date
                        }

                        invoiceDetailsFree[6] = rtnProdData[2]; // batch no
                        // date
                        invoiceDetailsFree[7] = rtnProdData[8]; // cust no

                        invoiceDetailsFree[8] = "0"; // Unit price

                        invoiceDetailsFree[9] = rtnProdData[0]; // Id
                        invoiceDetailsFree[10] = rtnProdData[11]; // Discount
                        invoiceDetailsFree[11] = rtnProdData[14];
                        invoiceDetailsFree[12] = rtnProdData[15];
                        invoiceDetailsFree[13] = rtnProdData[16];
                        Log.w("Log", "Test ##### 11:  " + rtnProdData[11]);

                        returnedProductList.add(invoiceDetailsFree);
                    }

                    publishProgress(2);
                    String responseArr = null;
                    while (responseArr == null) {
                        try {

                            SharedPreferences sharedPreferences = PreferenceManager
                                    .getDefaultSharedPreferences(getBaseContext());
                            String deviceId = sharedPreferences.getString("DeviceId", "-1");
                            String repId = sharedPreferences.getString("RepId", "-1");

                            WebService webService = new WebService();
                            responseArr = webService
                                    .uploadProductReturnsDetails(
                                            deviceId,
                                            repId,
                                            returnedProductList);

                            Thread.sleep(100);

                        } catch (SocketException e) {
                            e.printStackTrace();
                            dissmissProgressWithError(dialog);
                            return 0;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            dissmissProgressWithError(dialog);
                            return 0;
                        }

                    }

                    Log.w("Log",
                            "update data result : "
                                    + responseArr.contains("No Error"));
                    if (responseArr.contains("No Error")) {

                        Log.w("Log", "Update the iternarary status");

                        ProductReturns rtnProdObj = new ProductReturns(
                                SyncronizePreference.this);
                        rtnProdObj.openReadableDatabase();
                        rtnProdObj.setRtnProductsUploadedStatus(rtnProdData[0],
                                "true");
                        rtnProdObj.closeDatabase();

                        returnValue = 2;

                    }

                    Log.w("Log", "loadProductRepStoreData result : "
                            + responseArr);

                }

                if (rtnProducts.size() < 1) {

                    returnValue = 4;
                }

            } else {

                returnValue = 3;
            }
            AutoSyncOnOffFlag autoSyncOnOffFlag = new AutoSyncOnOffFlag(SyncronizePreference.this);
            autoSyncOnOffFlag.openReadableDatabase();
            autoSyncOnOffFlag.AutoSyncActive(0);
            autoSyncOnOffFlag.closeDatabase();
            return returnValue;

        }

    }

    private class DownloadProductRepStoreTask extends AsyncTask<String, Integer, Integer> {

        private final Context context;

        public DownloadProductRepStoreTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage("upload data to Server...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setProgress(0);

            dialog.setMax(100);
            dialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
            switch (progress[0]) {
                case 1:
                    Log.w("Log", "yyyyyyyyyy: ");
                    dialog.setMessage("Fetching Inventory Data from Server...");
                    break;
                case 2:
                    dialog.setMessage("Saving Inventory Data to Tab...");
                    break;
                default:
                    break;
            }
        }

        protected void onPostExecute(Integer returnCode) {
            dialog.dismiss();
            showDialog(returnCode);

        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub

            int returnValue = 1;

            Log.w("Log", "param result : " + params[0]);

            Log.w("Log", "loadProductRepStoreData result : starting ");

            if (isOnline()) {

                publishProgress(1);

                int maxRowID = 0;

                //

                ProductRepStore repStoreObject = new ProductRepStore(
                        SyncronizePreference.this);
                repStoreObject.openReadableDatabase();

                String lastProductId = repStoreObject.getMaxRepstoreId();
                repStoreObject.closeDatabase();
                Log.w("Log", "lastRepstoreId:  " + lastProductId);

                if (lastProductId != null && (!lastProductId.equals("null"))) {
                    maxRowID = Integer.parseInt(lastProductId);
                }

                ArrayList<String[]> repStoreDataResponse = null;
                while (repStoreDataResponse == null) {
                    try {

                        SharedPreferences sharedPreferences = PreferenceManager
                                .getDefaultSharedPreferences(getBaseContext());
                        String deviceId = sharedPreferences.getString("DeviceId", "-1");
                        String repId = sharedPreferences.getString("RepId", "-1");

                        WebService webService = new WebService();
                        repStoreDataResponse = webService
                                .getProductRepStoreList(
                                        deviceId,
                                        repId, maxRowID);

                        Thread.sleep(100);

                    } catch (SocketException e) {
                        e.printStackTrace();
                        dissmissProgressWithError(dialog);
                        return 0;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        dissmissProgressWithError(dialog);
                        return 0;
                    }
                }

                if (repStoreDataResponse.size() > 0) {

                    ProductRepStore productRepStore = new ProductRepStore(
                            SyncronizePreference.this);

                    String timeStamp = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

                    for (int i = 0; i < repStoreDataResponse.size(); i++) {

                        boolean flag = false;
                        String[] custDetails = repStoreDataResponse.get(i);
                        productRepStore.openReadableDatabase();
                        flag = productRepStore.isBatchAvailable(custDetails[5], custDetails[2], custDetails[4], custDetails[6], custDetails[7], custDetails[8]);
                        productRepStore.closeDatabase();

                        //    if (!flag) {
                        productRepStore.openWritableDatabase();
                        Long result = productRepStore.insertProductRepStore(custDetails[0],
                                custDetails[2], custDetails[5],
                                custDetails[3], custDetails[4], custDetails[6], custDetails[7], custDetails[8],
                                timeStamp);

                        if (result == -1) {
                            returnValue = 7;
                            productRepStore.closeDatabase();
                            break;
                        }
                        productRepStore.closeDatabase();

                        returnValue = 5;
                       /* } else {
                            productRepStore.openReadableDatabase();
                            String qty = productRepStore.getCurrentStockByBatch(custDetails[5]);
                            productRepStore.closeDatabase();
                            Log.w("QUANTITY", qty);
                            Log.w("QUANTITY FROM SERVER", custDetails[3]);

                            Log.w("Product ID", custDetails[0]);

                            int quantity = Integer.parseInt(qty)
                                    + Integer.parseInt(custDetails[3]);

                            productRepStore.openWritableDatabase();
                            long result = productRepStore.updateProductRepstore(custDetails[5], String.valueOf(quantity), custDetails[4], timeStamp, custDetails[0], custDetails[6], custDetails[7], custDetails[8]);

                            System.out.println("custDetails[4] :"+custDetails[4]);
                            System.out.println("custDetails[4] :"+custDetails[6]);
                            System.out.println("custDetails[4] :"+custDetails[7]);
                            productRepStore.closeDatabase();

                            if (result == -1) {
                                returnValue = 7;
                                break;
                            }

                            returnValue = 5;
                        }*/

                    }

                } else {

                    returnValue = 6;

                }

            } else {

                returnValue = 3;
            }
            AutoSyncOnOffFlag autoSyncOnOffFlag = new AutoSyncOnOffFlag(SyncronizePreference.this);
            autoSyncOnOffFlag.openReadableDatabase();
            autoSyncOnOffFlag.AutoSyncActive(0);
            autoSyncOnOffFlag.closeDatabase();

            return returnValue;

        }

    }

    private class UploadShelfQtyTask extends
            AsyncTask<String, Integer, Integer> {

        private final Context context;

        public UploadShelfQtyTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage("upload data to Server...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setProgress(0);

            dialog.setMax(100);
            dialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
            switch (progress[0]) {
                case 1:
                    Log.w("Log", "yyyyyyyyyy: ");
                    dialog.setMessage("Loading data from Tab");
                    break;
                case 2:
                    dialog.setMessage("Uploading data to the server...");
                    break;
                default:
                    break;
            }
        }

        protected void onPostExecute(Integer returnCode) {
            dialog.dismiss();
            showDialog(returnCode);

        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub

            int returnValue = 1;

            Log.w("Log", "param result : " + params[0]);

            Log.w("Log", "loadProductRepStoreData result : starting ");

            if (isOnline()) {

                publishProgress(1);

                ShelfQuantity rtnProdObject = new ShelfQuantity(
                        SyncronizePreference.this);
                rtnProdObject.openReadableDatabase();

                List<String[]> rtnProducts = rtnProdObject
                        .getShelfQuantitiesByStatus("false");
                rtnProdObject.closeDatabase();

                Log.w("Log", "rtnProducts size :  " + rtnProducts.size());

                ArrayList<String[]> invoicedProductDetailList = new ArrayList<String[]>();

                for (String[] invoicedProduct : rtnProducts) {
                    Log.w("Log", "rtnProducts :  " + invoicedProduct[0]);
                    Log.w("Log", "rtnProducts :  " + invoicedProduct[1]);
                    Log.w("Log", "rtnProducts :  " + invoicedProduct[2]);
                    Log.w("Log", "rtnProducts :  " + invoicedProduct[3]);
                    Log.w("Log", "rtnProducts :  " + invoicedProduct[4]);
                    Log.w("Log", "rtnProducts :  " + invoicedProduct[5]);
                    Log.w("Log", "rtnProducts :  " + invoicedProduct[6]);
                    Log.w("Log", "rtnProducts :  " + invoicedProduct[7]);
                    Log.w("Log", "rtnProducts :  " + invoicedProduct[8]);

                }

                SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
                String deviceId = sharedPreferences.getString("DeviceId", "-1");
                String repId = sharedPreferences.getString("RepId", "-1");

                for (String[] rtnProdData : rtnProducts) {

                    Log.w("Log", "rtnProducts id :  " + rtnProdData[0]);

                    String[] invoiceDetails = new String[13];

                    invoiceDetails[0] = repId; // rep id

                    invoiceDetails[1] = rtnProdData[1]; // Invoice no
                    invoiceDetails[2] = rtnProdData[2]; // Invoice date

                    invoiceDetails[3] = rtnProdData[3]; // customer id

                    invoiceDetails[4] = rtnProdData[4]; // item code
                    invoiceDetails[5] = rtnProdData[6]; // item code
                    invoiceDetails[6] = rtnProdData[5]; // item code

                    publishProgress(2);
                    String responseArr = null;
                    while (responseArr == null) {
                        try {

                            WebService webService = new WebService();
                            responseArr = webService
                                    .uploadShelfQuantityDetails(
                                            deviceId,
                                            repId,
                                            invoiceDetails);

                            Thread.sleep(100);

                        } catch (SocketException e) {
                            e.printStackTrace();
                            dissmissProgressWithError(dialog);
                            return 0;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            dissmissProgressWithError(dialog);
                            return 0;
                        }

                    }

                    Log.w("Log",
                            "update data result : "
                                    + responseArr
                                    .contains("Record Inserted Successfully"));
                    if (responseArr.contains("Record Inserted Successfully")) {

                        Log.w("Log", "Update the iternarary status");

                        ShelfQuantity rtnProdObj = new ShelfQuantity(
                                SyncronizePreference.this);
                        rtnProdObj.openReadableDatabase();
                        rtnProdObj.setShelfQtyUploadedStatus(rtnProdData[0],
                                "true");
                        rtnProdObj.closeDatabase();

                        returnValue = 2;

                    }

                    Log.w("Log", "loadProductRepStoreData result : "
                            + responseArr);

                }

                Log.w("Log", "invoicedProductDetailList size :  "
                        + invoicedProductDetailList.size());

                if (rtnProducts.size() < 1) {

                    returnValue = 4;
                }

            } else {

                returnValue = 3;
            }
            AutoSyncOnOffFlag autoSyncOnOffFlag = new AutoSyncOnOffFlag(SyncronizePreference.this);
            autoSyncOnOffFlag.openReadableDatabase();
            autoSyncOnOffFlag.AutoSyncActive(0);
            autoSyncOnOffFlag.closeDatabase();
            return returnValue;

        }

    }

    private class UploadCustomerImageTask extends
            AsyncTask<String, Integer, Integer> {

        private final Context context;

        public UploadCustomerImageTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage("upload data to Server...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setProgress(0);

            dialog.setMax(100);
            dialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
            switch (progress[0]) {
                case 1:
                    Log.w("Log", "yyyyyyyyyy: ");
                    dialog.setMessage("Loading data from Tab");
                    break;
                case 2:
                    dialog.setMessage("Uploading data to the server...");
                    break;
                default:
                    break;
            }
        }

        protected void onPostExecute(Integer returnCode) {
            dialog.dismiss();
            showDialog(returnCode);

        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub

            int returnValue = 1;

            Log.w("Log", "param result : " + params[0]);

            Log.w("Log", "loadProductRepStoreData result : starting ");

            if (isOnline()) {

                publishProgress(1);

                ImageGallery rtnProdObject = new ImageGallery(
                        SyncronizePreference.this);
                rtnProdObject.openReadableDatabase();

                List<String[]> rtnProducts = rtnProdObject
                        .getImagesByStatus("false");
                rtnProdObject.closeDatabase();

                Log.w("Log", "rtnProducts sized :  " + rtnProducts.size());

                if (rtnProducts.size() < 1) {
                    returnValue = 4;

                } else {

                    for (String[] rtnProdData : rtnProducts) {

                        Log.w("Log", "SimpleFTP ???");

                        Log.w("Log", "rtnProducts id :  " + rtnProdData[0]);

                        FTPClient con = new FTPClient();
                        try {
                            con.connect(SyncronizePreference.this
                                    .getString(com.Indoscan.channelbridge.R.string.ftp_host), Integer
                                    .parseInt(SyncronizePreference.this
                                            .getString(com.Indoscan.channelbridge.R.string.ftp_port)));
                            if (con.login(SyncronizePreference.this
                                            .getString(com.Indoscan.channelbridge.R.string.ftp_username),
                                    SyncronizePreference.this
                                            .getString(com.Indoscan.channelbridge.R.string.ftp_password))) {

                                con.enterLocalPassiveMode();
                                con.setFileType(FTP.BINARY_FILE_TYPE);

                                String str = Environment
                                        .getExternalStorageDirectory()
                                        + File.separator
                                        + "DCIM"
                                        + File.separator
                                        + "Channel_Bridge_Images"
                                        + File.separator + rtnProdData[3];

                                FileInputStream srcFileStream = new FileInputStream(
                                        str);

                                boolean status = con.storeFile(rtnProdData[3],
                                        srcFileStream);

                                srcFileStream.close();

                                if (status) {

                                    Log.w("Log", "Update the iternarary status");

                                    ImageGallery rtnProdObj = new ImageGallery(
                                            SyncronizePreference.this);
                                    rtnProdObj.openReadableDatabase();
                                    rtnProdObj.setImageUploadedStatus(
                                            rtnProdData[0], "true");
                                    rtnProdObj.closeDatabase();

                                    returnValue = 2;

                                }

                                // con.stor(str);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            returnValue = 4;
                        }

                    }
                }

            } else {

                returnValue = 3;
            }
            AutoSyncOnOffFlag autoSyncOnOffFlag = new AutoSyncOnOffFlag(SyncronizePreference.this);
            autoSyncOnOffFlag.openReadableDatabase();
            autoSyncOnOffFlag.AutoSyncActive(0);
            autoSyncOnOffFlag.closeDatabase();
            return returnValue;

        }

    }

    private class DownloadItineraryTask extends
            AsyncTask<String, Integer, Integer> {

        private final Context context;

        public DownloadItineraryTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage("Download data from Server...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setProgress(0);

            dialog.setMax(100);
            dialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
            switch (progress[0]) {
                case 1:
                    Log.w("Log", "yyyyyyyyyy: ");
                    dialog.setMessage("Fetching Itinerary Data from Server...");
                    break;
                case 2:
                    dialog.setMessage("Saving Itinerary Data to Tab...");
                    break;
                default:
                    break;
            }
        }

        protected void onPostExecute(Integer returnCode) {
            dialog.dismiss();
            showDialog(returnCode);

        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub

            int returnValue = 1;

            Log.w("Log", "param result : " + params[0]);

            Log.w("Log", "DownloadItineraryTask result : starting ");

            if (isOnline()) {

                publishProgress(1);

                String maxRowID = "0";

                Itinerary itineraryObj = new Itinerary(
                        SyncronizePreference.this);
                itineraryObj.openReadableDatabase();

                String itineraryId = itineraryObj.getMaxItnId();
                itineraryObj.closeDatabase();
                Log.w("Log", "lastProductId:  " + itineraryId);

                if (itineraryId != "" && itineraryId != null) {
                    maxRowID = itineraryId;
                }

                ArrayList<String[]> repStoreDataResponse = null;
                while (repStoreDataResponse == null) {
                    try {

                        SharedPreferences sharedPreferences = PreferenceManager
                                .getDefaultSharedPreferences(getBaseContext());
                        String deviceId = sharedPreferences.getString("DeviceId", "-1");
                        String repId = sharedPreferences.getString("RepId", "-1");

                        WebService webService = new WebService();
                        repStoreDataResponse = webService
                                .getItineraryListForRep(repId,
                                        deviceId, maxRowID);

                        Thread.sleep(100);

                    } catch (SocketException e) {
                        e.printStackTrace();
                        dissmissProgressWithError(dialog);
                        return 0;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        dissmissProgressWithError(dialog);
                        return 0;
                    }
                }

                Log.w("Log", "repStoreDataResponse.size() :  "
                        + repStoreDataResponse.size());

                if (repStoreDataResponse.size() > 0) {

                    Itinerary itinerary = new Itinerary(
                            SyncronizePreference.this);
                    itinerary.openWritableDatabase();

                    String timeStamp = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

                    for (int i = 0; i < repStoreDataResponse.size(); i++) {

                        String[] itnDetails = repStoreDataResponse.get(i);

                        Long result = itinerary.insertItinerary(itnDetails[8],
                                itnDetails[0], itnDetails[1], itnDetails[2],
                                itnDetails[3], itnDetails[4], itnDetails[5],
                                itnDetails[6], itnDetails[7], timeStamp,
                                "false", "false", "false");

                        if (result == -1) {
                            returnValue = 7;
                            break;
                        }

                        returnValue = 5;
                    }

                    itinerary.closeDatabase();

                } else {

                    returnValue = 6;

                }

            } else {

                returnValue = 3;
            }
            AutoSyncOnOffFlag autoSyncOnOffFlag = new AutoSyncOnOffFlag(SyncronizePreference.this);
            autoSyncOnOffFlag.openReadableDatabase();
            autoSyncOnOffFlag.AutoSyncActive(0);
            autoSyncOnOffFlag.closeDatabase();

            return returnValue;

        }

    }

    private class DownloadProductsTask extends
            AsyncTask<String, Integer, Integer> {

        private final Context context;

        public DownloadProductsTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage("upload data to Server...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setProgress(0);

            dialog.setMax(100);
            dialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
            switch (progress[0]) {
                case 1:
                    Log.w("Log", "yyyyyyyyyy: ");
                    dialog.setMessage("Fetching Products Data from Server...");
                    break;
                case 2:
                    dialog.setMessage("Saving Products Data to Tab...");
                    break;
                default:
                    break;
            }
        }

        protected void onPostExecute(Integer returnCode) {
            dialog.dismiss();
            showDialog(returnCode);

        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub

            int returnValue = 1;

            Log.w("Log", "param result : " + params[0]);

            Log.w("Log", "loadProductRepStoreData result : starting ");

            if (isOnline()) {

                publishProgress(1);

                String maxRowID = "0";

                Products prodObject = new Products(SyncronizePreference.this);
                prodObject.openReadableDatabase();

                String lastProductId = prodObject.getMaxProductId();
                prodObject.closeDatabase();
                Log.w("Log", "lastProductId:  " + lastProductId);

                if (lastProductId != "") {
                    maxRowID = lastProductId;
                }

                ArrayList<String[]> repStoreDataResponse = null;
                while (repStoreDataResponse == null) {
                    try {

                        SharedPreferences sharedPreferences = PreferenceManager
                                .getDefaultSharedPreferences(getBaseContext());
                        String deviceId = sharedPreferences.getString("DeviceId", "-1");
                        String repId = sharedPreferences.getString("RepId", "-1");

                        WebService webService = new WebService();
                        repStoreDataResponse = webService.getProductList(
                                deviceId, repId,
                                maxRowID);

                        Thread.sleep(100);

                    } catch (SocketException e) {
                        e.printStackTrace();
                        dissmissProgressWithError(dialog);
                        repStoreDataResponse = new ArrayList<String[]>();
                        return 0;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        dissmissProgressWithError(dialog);
                        repStoreDataResponse = new ArrayList<String[]>();
                        return 0;
                    }
                }

                Log.w("Log", "repStoreDataResponse.size() :  "
                        + repStoreDataResponse.size());

                if (repStoreDataResponse.size() > 0) {

                    Products products = new Products(SyncronizePreference.this);

                    String timeStamp = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

                    for (int i = 0; i < repStoreDataResponse.size(); i++) {

                        String[] custDetails = repStoreDataResponse.get(i);

                        Log.w("Log", "prod id  " + custDetails[0].trim());

                        products.openWritableDatabase();
                        boolean flag = products
                                .isProductAvailable(custDetails[0].trim());
                        products.closeDatabase();

                        if (flag) {

                            Log.w("Log", " inside flag true  ");

                            products.openWritableDatabase();
                            Long result = products.updateProduct(
                                    custDetails[0], custDetails[1],
                                    custDetails[2], custDetails[3],
                                    custDetails[4], custDetails[5],
                                    custDetails[6], custDetails[7],
                                    custDetails[8], "", custDetails[9],
                                    custDetails[10], custDetails[11],
                                    custDetails[12], custDetails[13],
                                    custDetails[14], custDetails[15],
                                    custDetails[16], timeStamp,
                                    custDetails[17].trim());

                            Log.w("Log", " inside flag true  "
                                    + custDetails[17] + " result :" + result);

                            if (result == -1) {
                                returnValue = 7;
                                products.closeDatabase();
                                break;
                            }
                            Log.w("Log", " inside flag true  " + result);
                            products.closeDatabase();
                        } else {

                            Log.w("Log", " inside flag false ");

                            products.openWritableDatabase();
                            Long result = products.insertProduct(
                                    custDetails[0].trim(), custDetails[1],
                                    custDetails[2], custDetails[3],
                                    custDetails[4], custDetails[5],
                                    custDetails[6], custDetails[7],
                                    custDetails[8], "", custDetails[9],
                                    custDetails[10], custDetails[11],
                                    custDetails[12], custDetails[13],
                                    custDetails[14], custDetails[15],
                                    custDetails[16], timeStamp,
                                    custDetails[17].trim());

                            if (result == -1) {
                                returnValue = 7;
                                products.closeDatabase();
                                break;
                            }
                            products.closeDatabase();
                        }

                        returnValue = 5;
                    }

                    products.closeDatabase();

                } else {

                    returnValue = 6;

                }

            } else {

                returnValue = 3;
            }
            AutoSyncOnOffFlag autoSyncOnOffFlag = new AutoSyncOnOffFlag(SyncronizePreference.this);
            autoSyncOnOffFlag.openReadableDatabase();
            autoSyncOnOffFlag.AutoSyncActive(0);
            autoSyncOnOffFlag.closeDatabase();

            return returnValue;

        }

    }

    private class DownloadCustomersTask extends AsyncTask<String, Integer, Integer> {

        private final Context context;

        public DownloadCustomersTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage("Download data from Server...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setProgress(0);

            dialog.setMax(100);
            dialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
            switch (progress[0]) {
                case 1:
                    Log.w("Log", "yyyyyyyyyy: ");
                    dialog.setMessage("Fetching Customer Data from Server...");
                    break;
                case 2:
                    dialog.setMessage("Saving Customer Data to Tab...");
                    break;
                default:
                    break;
            }
        }

        protected void onPostExecute(Integer returnCode) {
            dialog.dismiss();
            showDialog(returnCode);
            new DownloadCustomerImagesTask(context).execute("1");

        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub

            int returnValue = 1;

            Log.w("Log", "param result : " + params[0]);

            Log.w("Log", "loadProductRepStoreData result : starting ");

            if (isOnline()) {

                publishProgress(1);

                String maxRowID = "0";

                Customers customerObject = new Customers(context);
                customerObject.openReadableDatabase();

                String lastProductId = customerObject.getMaxCustomerId();
                customerObject.closeDatabase();
                Log.w("Log", "lastCustId:  " + lastProductId);

                if (lastProductId != "") {
                    if (lastProductId != null) {

                        maxRowID = lastProductId;
                    }


                }

                ArrayList<String[]> repStoreDataResponse = null;
                while (repStoreDataResponse == null) {
                    try {

                        SharedPreferences sharedPreferences = PreferenceManager
                                .getDefaultSharedPreferences(getBaseContext());
                        String deviceId = sharedPreferences.getString("DeviceId", "-1");
                        String repId = sharedPreferences.getString("RepId", "-1");

                        WebService webService = new WebService();
                        repStoreDataResponse = webService.getCustomerList(
                                deviceId, repId,
                                maxRowID);

                        Thread.sleep(100);

                    } catch (SocketException e) {
                        e.printStackTrace();
                        dissmissProgressWithError(dialog);
                        return 0;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        dissmissProgressWithError(dialog);
                        return 0;
                    }
                }

                Log.w("Log", "repStoreDataResponse.size() :  "
                        + repStoreDataResponse.size());

                if (repStoreDataResponse.size() > 0) {

                    Customers customers = new Customers(
                            SyncronizePreference.this);

                    String timeStamp = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss.SSS").format(new Date());

                    for (int i = 0; i < repStoreDataResponse.size(); i++) {

                        String[] custDetails = repStoreDataResponse.get(i);

                        customers.openReadableDatabase();
                        boolean isAvailable = customers.isCustomerDownloaded(custDetails[0]);
                        customers.closeDatabase();

                        if (isAvailable) {
                            Log.w("AVAILABLE", "Customer AVAILABLE");
                            customers.openWritableDatabase();
                            Long result = customers.updateCustomerDetails(
                                    custDetails[0], // pharmacyId
                                    custDetails[1], // pharmacyCode,
                                    custDetails[2], // dealerId,
                                    custDetails[3], // companyCode,
                                    custDetails[4], // customerName,
                                    custDetails[5], // address,
                                    custDetails[7], // area,
                                    custDetails[8], // town,
                                    custDetails[6], // district,
                                    custDetails[9], // telephone,
                                    custDetails[10], // fax,
                                    custDetails[11], // email,
                                    custDetails[12], // customerStatus,
                                    custDetails[13], // creditLimit,
                                    custDetails[33], // currentCredit,//test
                                    custDetails[14], // creditExpiryDate,
                                    custDetails[15], // creditDuration,
                                    custDetails[16], // vatNo,
                                    custDetails[17], // status,
                                    timeStamp, // timeStamp,
                                    custDetails[28], // latitude,
                                    custDetails[29], // longitude,
                                    custDetails[20], // web,
                                    custDetails[21], // brNo,
                                    custDetails[22], // ownerContact,
                                    custDetails[24], // ownerWifeBday,
                                    custDetails[23], // pharmacyRegNo,
                                    custDetails[25], // pharmacistName,
                                    custDetails[26], // purchasingOfficer,
                                    custDetails[27], // noStaff,
                                    custDetails[19], // customerCode
                                    custDetails[30],
                                    custDetails[31],
                                    android.util.Base64.decode(custDetails[32], Base64.DEFAULT),
                                    custDetails[34],
                                    custDetails[35],
                                    custDetails[36]

                            );
                            customers.closeDatabase();

                            if (result == -1) {
                                returnValue = 7;
                                break;
                            }

                            returnValue = 5;

                        } else {
                            Log.w("UNAVAILABLE", "Customer UNAVAILABLE");
                            customers.openWritableDatabase();
                            Long result = customers.insertCustomer(
                                    custDetails[0], // pharmacyId
                                    custDetails[1], // pharmacyCode,
                                    custDetails[2], // dealerId,
                                    custDetails[3], // companyCode,
                                    custDetails[4], // customerName,
                                    custDetails[5], // address,
                                    custDetails[7], // area,
                                    custDetails[8], // town,
                                    custDetails[6], // district,
                                    custDetails[9], // telephone,
                                    custDetails[10], // fax,
                                    custDetails[11], // email,
                                    custDetails[12], // customerStatus,
                                    custDetails[13], // creditLimit,
                                    "0", // currentCredit,
                                    custDetails[14], // creditExpiryDate,
                                    custDetails[15], // creditDuration,
                                    custDetails[16], // vatNo,
                                    custDetails[17], // status,
                                    timeStamp, // timeStamp,
                                    custDetails[28], // latitude,
                                    custDetails[29], // longitude,
                                    custDetails[20], // web,
                                    custDetails[21], // brNo,
                                    custDetails[22], // ownerContact,
                                    custDetails[24], // ownerWifeBday,
                                    custDetails[23], // pharmacyRegNo,
                                    custDetails[25], // pharmacistName,
                                    custDetails[26], // purchasingOfficer,
                                    custDetails[27], // noStaff,
                                    custDetails[19], // customerCode
                                    custDetails[30],
                                    custDetails[31],
                                    android.util.Base64.decode(custDetails[32], Base64.DEFAULT),
                                    custDetails[33],
                                    custDetails[34], custDetails[35]
                                    // Byte.parseByte(custDetails[32])//add image sk
                            );
                            customers.closeDatabase();

                            if (result == -1) {
                                returnValue = 7;
                                break;
                            }

                            returnValue = 5;
                        }
                    }
                } else {

                    returnValue = 6;

                }

            } else {

                returnValue = 3;
            }
            AutoSyncOnOffFlag autoSyncOnOffFlag = new AutoSyncOnOffFlag(SyncronizePreference.this);
            autoSyncOnOffFlag.openReadableDatabase();
            autoSyncOnOffFlag.AutoSyncActive(0);
            autoSyncOnOffFlag.closeDatabase();

            return returnValue;

        }

    }

    private class UploadInvoiceOutstandingTask extends AsyncTask<String, Integer, Integer> {

        private final Context context;

        public UploadInvoiceOutstandingTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage("Upload Invoices Outstandings to Server...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setProgress(0);

            dialog.setMax(100);
            dialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
            switch (progress[0]) {
                case 1:
                    Log.w("Log", "yyyyyyyyyy: ");
                    dialog.setMessage("Loading data from Tab");
                    break;
                case 2:
                    dialog.setMessage("Uploading data to the server...");
                    break;
                default:
                    break;
            }
        }

        protected void onPostExecute(Integer returnCode) {
            dialog.dismiss();
            showDialog(returnCode);

        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub

            int returnValue = 1;

            Log.w("Log", "param result : " + params[0]);

            Log.w("Log", "loadProductRepStoreData result : starting ");

            if (isOnline()) {

                publishProgress(1);

                Invoice invoiceObject = new Invoice(SyncronizePreference.this);
                invoiceObject.openReadableDatabase();

                List<String[]> invoice = invoiceObject
                        .getInvoicesByOutstandingUploadStatus("false");
                invoiceObject.closeDatabase();

                Log.w("Log", "invoice size :  " + invoice.size());

                for (String[] invoiceData : invoice) {

                    Log.w("Log", "invoice id :  " + invoiceData[0]);
                    Log.w("Log", "invoice date :  " + invoiceData[10]);

                    Itinerary itinerary = new Itinerary(
                            SyncronizePreference.this);
                    itinerary.openReadableDatabase();

                    String tempCust = itinerary
                            .getItineraryStatus(invoiceData[1]);
                    itinerary.closeDatabase();

                    String custNo = "";

                    Itinerary itineraryTwo = new Itinerary(
                            SyncronizePreference.this);
                    itineraryTwo.openReadableDatabase();

                    if (tempCust.equals("true")) {

                        SharedPreferences sharedPreferences = PreferenceManager
                                .getDefaultSharedPreferences(getBaseContext());
                        String deviceId = sharedPreferences.getString("DeviceId", "-1");

                        String[] itnDetails = itineraryTwo
                                .getItineraryDetailsForTemporaryCustomer(invoiceData[1]);
                        custNo = deviceId + "_" + itnDetails[7];// this
                        // is
                        // where
                        // yu
                        // have
                        // to
                        // change..!!
                    } else {
                        String[] itnDetails = itineraryTwo
                                .getItineraryDetailsById(invoiceData[1]);
                        custNo = itnDetails[4];
                    }

                    itineraryTwo.closeDatabase();

                    String[] invoiceOutstandingDetails = new String[6];

                    invoiceOutstandingDetails[0] = invoiceData[0]; // Invoice Id
                    invoiceOutstandingDetails[1] = custNo; // cust No
                    invoiceOutstandingDetails[2] = invoiceData[11].substring(0,
                            10); // invoice
                    // date
                    invoiceOutstandingDetails[3] = invoiceData[3]; // total
                    // amount
                    invoiceOutstandingDetails[4] = invoiceData[5]; // credit
                    // amount
                    invoiceOutstandingDetails[5] = invoiceData[13]; // credit
                    // duration

                    publishProgress(2);
                    String responseArr = null;
                    while (responseArr == null) {
                        try {

                            SharedPreferences sharedPreferences = PreferenceManager
                                    .getDefaultSharedPreferences(getBaseContext());
                            String deviceId = sharedPreferences.getString("DeviceId", "-1");
                            String repId = sharedPreferences.getString("RepId", "-1");

                            WebService webService = new WebService();
                            responseArr = webService
                                    .uploadInvoiceOutstandingDetails(
                                            deviceId,
                                            repId,
                                            invoiceOutstandingDetails);

                            Thread.sleep(100);

                        } catch (SocketException e) {
                            e.printStackTrace();
                            dissmissProgressWithError(dialog);
                            return 0;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            dissmissProgressWithError(dialog);
                            return 0;
                        }

                    }

                    Log.w("Log",
                            "update data result : "
                                    + responseArr.contains("No Error"));
                    if (responseArr.contains("No Error")) {

                        Log.w("Log", "Update the iternarary status");

                        Invoice invoiceObj = new Invoice(
                                SyncronizePreference.this);
                        invoiceObj.openReadableDatabase();
                        invoiceObj.setInvoiceOutstandingUpdatedStatus(
                                invoiceData[0], "true");
                        invoiceObj.closeDatabase();

                        returnValue = 2;

                    }

                }

                if (invoice.size() < 1) {

                    returnValue = 4;
                }

            } else {

                returnValue = 3;
            }
            AutoSyncOnOffFlag autoSyncOnOffFlag = new AutoSyncOnOffFlag(SyncronizePreference.this);
            autoSyncOnOffFlag.openReadableDatabase();
            autoSyncOnOffFlag.AutoSyncActive(0);
            autoSyncOnOffFlag.closeDatabase();
            return returnValue;

        }

    }

    private class UploadInvoicedChequesTask extends AsyncTask<String, Integer, Integer> {

        private final Context context;

        public UploadInvoicedChequesTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage("Upload Invoices Cheques to Server...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setProgress(0);

            dialog.setMax(100);
            dialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
            switch (progress[0]) {
                case 1:
                    Log.w("Log", "yyyyyyyyyy: ");
                    dialog.setMessage("Loading data from Tab");
                    break;
                case 2:
                    dialog.setMessage("Uploading data to the server...");
                    break;
                default:
                    break;
            }
        }

        protected void onPostExecute(Integer returnCode) {
            dialog.dismiss();
            showDialog(returnCode);

        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub

            int returnValue = 1;

            Log.w("Log", "param result : " + params[0]);

            if (isOnline()) {

                publishProgress(1);

                InvoicedCheque invoiceChequeObject = new InvoicedCheque(
                        SyncronizePreference.this);
                invoiceChequeObject.openReadableDatabase();

                List<String[]> invoiceCheque = invoiceChequeObject
                        .getInvoicedChequesByStatus("false");
                invoiceChequeObject.closeDatabase();

                Log.w("Log", "invoice size :  " + invoiceCheque.size());

                for (String[] invoiceChequeData : invoiceCheque) {

                    Log.w("Log", "invoice id :  " + invoiceChequeData[0]);

                    String[] invoiceChequeDetails = new String[5];

                    invoiceChequeDetails[0] = invoiceChequeData[3]; // Cheque No
                    invoiceChequeDetails[1] = invoiceChequeData[2]; // cust No
                    invoiceChequeDetails[2] = invoiceChequeData[5]; // collected
                    // date
                    invoiceChequeDetails[3] = invoiceChequeData[6]; // release
                    // date
                    invoiceChequeDetails[4] = invoiceChequeData[4]; // Cheque
                    // Amount

                    publishProgress(2);
                    String responseArr = null;
                    while (responseArr == null) {
                        try {

                            SharedPreferences sharedPreferences = PreferenceManager
                                    .getDefaultSharedPreferences(getBaseContext());
                            String deviceId = sharedPreferences.getString("DeviceId", "-1");
                            String repId = sharedPreferences.getString("RepId", "-1");

                            WebService webService = new WebService();
                            responseArr = webService
                                    .uploadInvoiceChequeDetails(
                                            deviceId,
                                            repId,
                                            invoiceChequeDetails);

                            Thread.sleep(100);


                        } catch (SocketException e) {
                            e.printStackTrace();
                            dissmissProgressWithError(dialog);
                            return 0;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            dissmissProgressWithError(dialog);
                            return 0;
                        }

                    }

                    Log.w("Log",
                            "update data result : "
                                    + responseArr.contains("No Error"));
                    if (responseArr.contains("No Error")) {

                        Log.w("Log", "Update the iternarary status");

                        InvoicedCheque invoiceChequeObj = new InvoicedCheque(
                                SyncronizePreference.this);
                        invoiceChequeObj.openReadableDatabase();
                        invoiceChequeObj.setInvoicedChequesUploadedStatus(
                                invoiceChequeData[0], "true");
                        invoiceChequeObj.closeDatabase();

                        returnValue = 2;

                    }

                }

                if (invoiceCheque.size() < 1) {

                    returnValue = 4;
                }

            } else {

                returnValue = 3;
            }
            AutoSyncOnOffFlag autoSyncOnOffFlag = new AutoSyncOnOffFlag(SyncronizePreference.this);
            autoSyncOnOffFlag.openReadableDatabase();
            autoSyncOnOffFlag.AutoSyncActive(0);
            autoSyncOnOffFlag.closeDatabase();
            return returnValue;

        }

    }

    private class UploadProductUnloadTask extends AsyncTask<String, Integer, Integer> {

        private final Context context;

        public UploadProductUnloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage("Upload product unloads to Server...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setProgress(0);

            dialog.setMax(100);
            dialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
            switch (progress[0]) {
                case 1:
                    Log.w("Log", "yyyyyyyyyy: ");
                    dialog.setMessage("Loading data from Tab");
                    break;
                case 2:
                    dialog.setMessage("Uploading data to the server...");
                    break;
                default:
                    break;
            }
        }

        protected void onPostExecute(Integer returnCode) {
            dialog.dismiss();
            showDialog(returnCode);

        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub

            int returnValue = 1;

            Log.w("Log", "param result : " + params[0]);

            if (isOnline()) {

                publishProgress(1);

                ProductUnload productUnloadObject = new ProductUnload(
                        SyncronizePreference.this);
                productUnloadObject.openReadableDatabase();

                List<String[]> productUnload = productUnloadObject
                        .getProdUnloadsByUploadStatus("1");
                productUnloadObject.closeDatabase();

                Log.w("Log", "invoice size :  " + productUnload.size());

                for (String[] invoiceChequeData : productUnload) {

                    Log.w("Log", "invoice id :  " + invoiceChequeData[0]);

                    String[] unloadProdDetails = new String[5];

                    unloadProdDetails[0] = invoiceChequeData[0];
                    unloadProdDetails[1] = invoiceChequeData[1];
                    unloadProdDetails[2] = invoiceChequeData[4];
//					unloadProdDetails[3] ="14/03/2013";

                    Log.w("Log", "invoiceChequeData[3] : " + invoiceChequeData[3]);

                    unloadProdDetails[3] = changeDateFormat(invoiceChequeData[3].substring(0, 10));
                    unloadProdDetails[4] = invoiceChequeData[2];

                    publishProgress(2);
                    String responseArr = null;
                    while (responseArr == null) {
                        try {

                            SharedPreferences sharedPreferences = PreferenceManager
                                    .getDefaultSharedPreferences(getBaseContext());
                            String deviceId = sharedPreferences.getString("DeviceId", "-1");
                            String repId = sharedPreferences.getString("RepId", "-1");

                            WebService webService = new WebService();
                            responseArr = webService.SetUnloadingDetails(
                                    deviceId,
                                    repId, unloadProdDetails);

                            Thread.sleep(100);

                        } catch (SocketException e) {
                            e.printStackTrace();
                            dissmissProgressWithError(dialog);
                            return 0;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            dissmissProgressWithError(dialog);
                            return 0;
                        }

                    }

                    Log.w("Log",
                            "update data result : "
                                    + responseArr.contains("No Error"));
                    if (responseArr.contains("No Error")) {

                        Log.w("Log", "Update the iternarary status");

                        ProductUnload invoiceChequeObj = new ProductUnload(
                                SyncronizePreference.this);
                        invoiceChequeObj.openReadableDatabase();
                        invoiceChequeObj.setProdUnloadStatus(
                                invoiceChequeData[0], "0");
                        invoiceChequeObj.closeDatabase();

                        returnValue = 2;

                    }

                }

                if (productUnload.size() < 1) {

                    returnValue = 4;
                }

                publishProgress(1);

                ProductUnload proUnloadObject = new ProductUnload(
                        SyncronizePreference.this);
                proUnloadObject.openReadableDatabase();

                List<String[]> prodUnload = proUnloadObject
                        .getProdUnloadsByUploadStatus("0");
                proUnloadObject.closeDatabase();

                Log.w("Log", "invoice size :  " + prodUnload.size());

                String unloadIds = "";

                boolean flag = true;

                for (String[] invoiceChequeData : prodUnload) {

                    Log.w("Log", "invoice id :  " + invoiceChequeData[0]);

                    if (flag) {
                        unloadIds = unloadIds + invoiceChequeData[0];
                        flag = false;
                    } else {
                        unloadIds = unloadIds + "," + invoiceChequeData[0];
                    }


                }

                publishProgress(2);
                ArrayList<String[]> responseArr = null;
                while (responseArr == null) {
                    try {

                        SharedPreferences sharedPreferences = PreferenceManager
                                .getDefaultSharedPreferences(getBaseContext());
                        String deviceId = sharedPreferences.getString("DeviceId", "-1");
                        String repId = sharedPreferences.getString("RepId", "-1");

                        WebService webService = new WebService();
                        responseArr = webService.getGetUnloadingStatus(
                                deviceId, repId,
                                unloadIds);

                        Thread.sleep(100);

                    } catch (SocketException e) {
                        e.printStackTrace();
                        dissmissProgressWithError(dialog);
                        return 0;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        dissmissProgressWithError(dialog);
                        return 0;
                    }

                }

                Log.w("Log",
                        "update data result : "
                                + responseArr.contains("No Error"));

                Log.w("Log", "update data result : " + responseArr.size());

                boolean responseFlag = false;


                if (responseArr.size() > 0) {

                    for (String[] details : responseArr) {
                        Log.w("Log", "Update the iternarary status");

                        String idNo = details[0];

                        Log.w("Log", "idNo : " + idNo);

                        Log.w("Log", "details[0] : " + details[0]);
                        Log.w("Log", "details[1] : " + details[1]);
                        Log.w("Log", "details[2] : " + details[2]);
                        Log.w("Log", "details[3] : " + details[3]);
                        Log.w("Log", "details[4] : " + details[4]);
                        Log.w("Log", "details[5] : " + details[5]);

                        String unloadStatus = "0";
                        Long result = new Long(-1);

                        if (details[5].trim().equalsIgnoreCase("a")) {
                            unloadStatus = "2";
                            result = (long) 0;
                        } else if (details[5].trim().equalsIgnoreCase("r")) {
                            unloadStatus = "3";

                            int unloadQty = Integer.parseInt(details[2]);

                            ProductRepStore ProductRepStore = new ProductRepStore(
                                    getApplication());
                            ProductRepStore.openReadableDatabase();
                            result = ProductRepStore.updateRepStoreQtyAdd(
                                    details[4], unloadQty, details[1]);
                            ProductRepStore.closeDatabase();
                        }

                        if (result != -1) {
                            ProductUnload invoiceChequeObj = new ProductUnload(
                                    SyncronizePreference.this);
                            invoiceChequeObj.openReadableDatabase();
                            invoiceChequeObj.setProdUnloadStatus(idNo,
                                    unloadStatus);
                            invoiceChequeObj.closeDatabase();

                            responseFlag = true;

                        }

                    }


                }

                if (responseFlag && returnValue == 2) {

                    returnValue = 8;

                } else if (responseFlag && returnValue == 4) {

                    returnValue = 9;

                } else if (!responseFlag && returnValue == 2) {

                    returnValue = 2;

                } else if (!responseFlag && returnValue == 4) {

                    returnValue = 4;

                }


            } else {

                returnValue = 3;
            }
            AutoSyncOnOffFlag autoSyncOnOffFlag = new AutoSyncOnOffFlag(SyncronizePreference.this);
            autoSyncOnOffFlag.openReadableDatabase();
            autoSyncOnOffFlag.AutoSyncActive(0);
            autoSyncOnOffFlag.closeDatabase();
            return returnValue;

        }

    }

    //Himanshu
    private class DownloadFreeIsues extends AsyncTask<String, Integer, Integer> {

        private final Context context;
        ArrayList<String[]> responseArr = null;

        public DownloadFreeIsues(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage("Download Free issues...");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setProgress(0);

            dialog.setMax(100);
            dialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
            switch (progress[0]) {
                case 1:
                    dialog.setMessage("Loading data");
                    break;
                default:
                    break;
            }
        }

        protected void onPostExecute(Integer returnCode) {

            System.out.println("responseArr :" + responseArr);
            try {
                DiscountStructures ds = new DiscountStructures(SyncronizePreference.this);
                ds.openReadableDatabase();
                for (int i = 0; i < responseArr.size(); i++) {
                    String[] disDetails = responseArr.get(i);
                    ds.insertDiscountStructures(disDetails[0], disDetails[1], disDetails[2], disDetails[3], disDetails[4], disDetails[5], disDetails[6], disDetails[7], disDetails[8]);

                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            dialog.dismiss();
            showDialog(returnCode);

        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub

            int returnValue = 1;

            if (isOnline()) {
                while (responseArr == null) {
                    try {

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

                        String repId = sharedPreferences.getString("RepId", "-1");

                        WebService webService = new WebService();
                        responseArr = webService.getDiscountStructures(repId);
                        Thread.sleep(100);


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                returnValue = 2;
            } else {

                returnValue = 3;
            }

            return returnValue;

        }

    }


}