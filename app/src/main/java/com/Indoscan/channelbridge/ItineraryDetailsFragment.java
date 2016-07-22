package com.Indoscan.channelbridge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.Indoscan.Entity.Product;
import com.Indoscan.channelbridge.R;
import com.Indoscan.channelbridgebs.DownloadImage;
import com.Indoscan.channelbridgebs.UploadRemarksTask;
import com.Indoscan.channelbridgedb.Approval_Details;
import com.Indoscan.channelbridgedb.Approval_Persons;
import com.Indoscan.channelbridgedb.Customers;
import com.Indoscan.channelbridgedb.CustomersPendingApproval;
import com.Indoscan.channelbridgedb.DEL_Outstandiing;
import com.Indoscan.channelbridgedb.DealerSales;
import com.Indoscan.channelbridgedb.ImageGallery;
import com.Indoscan.channelbridgedb.Invoice;
import com.Indoscan.channelbridgedb.InvoicedProducts;
import com.Indoscan.channelbridgedb.Itinerary;
import com.Indoscan.channelbridgedb.ProductRepStore;
import com.Indoscan.channelbridgedb.Products;
import com.Indoscan.channelbridgedb.Remarks;
import com.Indoscan.channelbridgedb.Reps;
import com.Indoscan.channelbridgedb.TemporaryInvoice;
import com.Indoscan.channelbridgehelp.RemarksType;
import com.Indoscan.channelbridgews.WebService;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.net.SocketException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class ItineraryDetailsFragment extends Fragment implements LocationListener {

    public static String cusName = "", contactNumber = "", pharmacyId1 = "";
    Cursor cursor;
    String error = "";
    String rowID = "";
    String repId = "";
    String pharmacyId = "";
    String itenararyDate;
    TextView tViewName, tViewPOfficer, tViewTelephone, tViewAddress, tViewArea,
            tViewTown, tViewInvoiceNumber, tViewInvoiceVal, tViewVariance, tvCreditLimit,
            tViewTarget, tvVariancefr, tvInvoiceNumber, tvCurrntCredit;
    EditText txtRemarks;
    Button btnViewCustomerDetails, btnGenerateInvoice, btnLastInvoice;
    ImageView iViewCustomerPic;
    ImageButton iBtnSaveRemark, iBtnEditInvoice;
    private String globalPharmaId = "";
    private Reps repconnector;
    private Boolean isInvoiceOption2;
    private Boolean isBlockerActivated;
    private Intent startInvoiceGen1;
    InvocieTemporyLoadDataTask1 temporyLoadDataTask1;
    private Invoice invoHandler;
    private Boolean iswebApprovalActive = false;
    private Customers customerHandler;
    private DealerSales salesHandler;
    private LocationManager locationManager;
    Location location;
    double lat, lng;
    EditText txtEditInvoiceRemark;
    int customertype = 0;

    Dialog invoiceEdit;

    public static ItineraryDetailsFragment newInstance(int index, String rowid) {
        ItineraryDetailsFragment itineraryDetailsFragmentObject = new ItineraryDetailsFragment();

        Bundle args = new Bundle();
        args.putInt("index", index);
        args.putString("rowIdString", rowid);
        itineraryDetailsFragmentObject.setArguments(args);

        return itineraryDetailsFragmentObject;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 0;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String path,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }

    @SuppressWarnings("unused")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (container == null) {
            return null;

        }

        //  try {


        View v = getActivity().findViewById(R.id.details);
        SharedPreferences shared = getActivity().getApplicationContext().getSharedPreferences("CBHesPreferences", Context.MODE_PRIVATE);
        isInvoiceOption2 = (shared.getBoolean("InvoiceOption", true));
        isBlockerActivated = (shared.getBoolean("ISOutStandingBlock", true));
        Log.i("isInvoiceOption2 -> ", isInvoiceOption2.toString());

        iswebApprovalActive = (shared.getBoolean("WebApproval", true));
        invoHandler = new Invoice(getActivity().getApplicationContext());
        salesHandler = new DealerSales(getActivity().getApplicationContext());

        iViewCustomerPic = (ImageView) v.findViewById(R.id.ivCustomerImage);
        tViewName = (TextView) v.findViewById(R.id.tvName);
        tViewTelephone = (TextView) v.findViewById(R.id.tvTelephone);
        tViewAddress = (TextView) v.findViewById(R.id.tvAddress);
        tViewInvoiceNumber = (TextView) v
                .findViewById(R.id.tvInvoiceNumber);
        tViewInvoiceVal = (TextView) v.findViewById(R.id.tvInvoiceVal);
        tvVariancefr = (TextView) v.findViewById(R.id.tvVariancefr);
        tViewTarget = (TextView) v.findViewById(R.id.tvTarget);
        tViewVariance = (TextView) v.findViewById(R.id.tvVariance);
        tvInvoiceNumber = (TextView) v.findViewById(R.id.tvInvoiceNumber);
        txtRemarks = (EditText) v.findViewById(R.id.etRemarks);
        tvCreditLimit = (TextView) v.findViewById(R.id.tvCreditLimit);
        tvCurrntCredit = (TextView) v.findViewById(R.id.tvCurrntCredit);
        btnViewCustomerDetails = (Button) v
                .findViewById(R.id.bViewCustomerDetails);
        btnGenerateInvoice = (Button) v.findViewById(R.id.bGenerateInvoice);
        btnLastInvoice = (Button) v.findViewById(R.id.bLastInvoice);
        iBtnSaveRemark = (ImageButton) v.findViewById(R.id.ibSaveRemarks);
        iBtnEditInvoice = (ImageButton) v.findViewById(R.id.ibEditInvoice);
        Button btnInvoiceHistory = (Button) v.findViewById(R.id.bInvoiceHistory);
        rowID = getArguments().getString("rowIdString");
        customerHandler = new Customers(getActivity().getApplicationContext());
        Log.w("ROW ID SENT FROM ITINERARY LIST", rowID + "");
        invoiceEdit = new Dialog(getActivity());
        invoiceEdit.setContentView(R.layout.invoice_edit_popup);
        invoiceEdit.setTitle("Edit Invoice");
        invoiceEdit.setCanceledOnTouchOutside(false);
        final RadioGroup rGroupEditType = (RadioGroup) invoiceEdit.findViewById(R.id.rgEditType);
        RadioButton rBtnReturnProduct = (RadioButton) invoiceEdit.findViewById(R.id.rbReturnProduct);
        RadioButton rBtnReturnInvoice = (RadioButton) invoiceEdit.findViewById(R.id.rbReturnInvoice);
        final TextView tViewInvoiceNumber = (TextView) invoiceEdit.findViewById(R.id.tvInvoiceNo);
        txtEditInvoiceRemark = (EditText) invoiceEdit.findViewById(R.id.etRemarks);
        final Button btnSave = (Button) invoiceEdit.findViewById(R.id.bSave);
        Button btnCancel = (Button) invoiceEdit.findViewById(R.id.bCancel);


//			Itinerary itinerary = new Itinerary(getActivity());
//			itinerary.openReadableDatabase();
//			String[] itineraryDetails = itinerary.getItineraryDetailsById(rowID);
//			itinerary.closeDatabase();


        //  btnGenerateInvoice.setEnabled(false);
        itineraryStatus();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("AutoSyncRun", true);
        editor.commit();

        SharedPreferences btnPpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        boolean invoiceQtySuggestion = btnPpreferences.getBoolean("cbPrefProductAvg", true);


        if (!invoiceQtySuggestion) {
            btnInvoiceHistory.setEnabled(false);
        }


        btnInvoiceHistory.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("PharmacyId", pharmacyId);
                Intent intent = new Intent("com.Indoscan.channelbridge.INVOICEHISTORYACTIVITY");
                intent.putExtras(bundle);
                startActivity(intent);


            }
        });
        iViewCustomerPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String deviceId = sharedPreferences.getString("DeviceId", "-1");
                String repId = sharedPreferences.getString("RepId", "-1");

                DownloadImage downloadImage = new DownloadImage(getActivity());
                String imageWithImageId[] = new String[0];
                try {
                    imageWithImageId = downloadImage.execute(repId, pharmacyId).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                if (imageWithImageId.length > 0) {
                    byte[] image1 = new byte[0];

                    image1 = android.util.Base64.decode(imageWithImageId[0], Base64.DEFAULT);
                    Bitmap bm = BitmapFactory.decodeByteArray(image1, 0, image1.length);
                    createDirectoryAndSaveFile(bm, imageWithImageId[1]);
                    iViewCustomerPic.setImageBitmap(bm);


                } else {
                    iViewCustomerPic.setImageResource(R.drawable.unknown_image);
                }
            }
        });
        btnLastInvoice.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent startLastInvoice = new Intent("com.Indoscan.channelbridge.LASTINVOICEACTIVITY");
                Bundle bundleToView = new Bundle();
                bundleToView.putString("Id", rowID);
                bundleToView.putString("PharmacyId", pharmacyId);

                startLastInvoice.putExtras(bundleToView);
                startActivity(startLastInvoice);
                getActivity().finish();

            }
        });

        txtEditInvoiceRemark.setOnKeyListener(new View.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_TAB) {
                    txtEditInvoiceRemark.setInputType(InputType.TYPE_NULL);
                    btnSave.setFocusable(true);
                    btnSave.requestFocus();
                }
                return false;
            }

        });

        txtEditInvoiceRemark.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                txtEditInvoiceRemark.setInputType(InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                txtEditInvoiceRemark.clearFocus();
                txtEditInvoiceRemark.setText(null);
                invoiceEdit.dismiss();
            }
        });
        final Remarks remarksObject = new Remarks(getActivity());
        btnSave.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                String remarks = txtEditInvoiceRemark.getText().toString();
                String timeStamp = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                int selectedEditType = rGroupEditType.getCheckedRadioButtonId();

                switch (selectedEditType) {
                    case R.id.rbReturnProduct:

                        try {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
                            boolean history = preferences.getBoolean("cbPrefEnableNoHistoryReturns", true);
                            if (history) {

                                if (customertype == 0) {
                                    Toast featureNotEnabled = Toast.makeText(getActivity(), "Sorry, this feature has not been enabled", Toast.LENGTH_SHORT);
                                    featureNotEnabled.setGravity(Gravity.TOP, 100, 100);
                                    featureNotEnabled.show();
                                } else {
                                    boolean statusAddNewInvoice = saveInvoice(remarks, timeStamp, RemarksType.RETURN_PRODUCT_WITHOUT_HISTORY.toString());

                                    if (statusAddNewInvoice) {
                                        //   new UploadRemarksTask(getActivity()).execute();
                                        new UploadRemarksTask(getActivity()).execute();
                                        txtEditInvoiceRemark.clearFocus();
                                        txtEditInvoiceRemark.setText(null);
                                        Intent startProductReturn = new Intent(getActivity(), ReturnProductNoHistoryActivity.class);

                                        Bundle bundleToView = new Bundle();
                                        bundleToView.putString("Id", rowID);
                                        bundleToView.putString("PharmacyId", pharmacyId);
                                        bundleToView.putString("onTimeOrNot", "1");
                                        startProductReturn.putExtras(bundleToView);
                                        getActivity().finish();
                                        invoiceEdit.dismiss();
                                        startActivity(startProductReturn);
                                    } else {
                                    }
                                }


                            } else {
                                Toast featureNotEnabled = Toast.makeText(getActivity(), "Sorry, this feature has not been enabled", Toast.LENGTH_SHORT);
                                featureNotEnabled.setGravity(Gravity.TOP, 100, 100);
                                featureNotEnabled.show();
                            }
                        } catch (Exception e) {
                            // TODO: handle exception
                            Log.w("error starting no history return", e.toString());
                        }
                        break;

                    case R.id.rbReturnInvoice:
                        try {
                            InvoicedProducts invoiceProductsObject = new InvoicedProducts(getActivity());
                            invoiceProductsObject.openReadableDatabase();
                            List<String[]> invoiceData = invoiceProductsObject.getInvoicesByItineraryDate(rowID);
                            invoiceProductsObject.closeDatabase();
                            if (!invoiceData.isEmpty()) {

                                if (customertype == 0) {
                                    // showDialogSendMessage(getActivity(), 6);
                                } else {
                                    boolean statusReturnInvoiceReason = saveInvoice(remarks, timeStamp, RemarksType.RETURN_INVOICE.toString());
                                    if (statusReturnInvoiceReason) {
                                        txtEditInvoiceRemark.clearFocus();
                                        txtEditInvoiceRemark.setText(null);
                                        new UploadRemarksTask(getActivity()).execute();
                                        new UploadRemarksTask(getActivity()).execute();
                                        Intent startInvoiceReturn = new Intent(getActivity(), ReturnInvoiceActivity.class);

                                        Bundle bundleToView = new Bundle();
                                        bundleToView.putString("Id", rowID);
                                        bundleToView.putString("PharmacyId", pharmacyId);
                                        bundleToView.putString("onTimeOrNot", "1");
                                        startInvoiceReturn.putExtras(bundleToView);
                                        txtEditInvoiceRemark.setText(null);
                                        invoiceEdit.dismiss();
                                        getActivity().finish();
                                        startActivity(startInvoiceReturn);
                                    } else {
                                    }
                                }


                            } else {
                                Toast noInvoiceToday = Toast.makeText(getActivity(), "No Invoice has been made today!", Toast.LENGTH_SHORT);
                                noInvoiceToday.setGravity(Gravity.TOP, 100, 100);
                                noInvoiceToday.show();
                                invoiceEdit.dismiss();
                            }


                        } catch (Exception e) {

                        }
                        break;

                    case R.id.rbReInvoice:
                        try {

                            if (customertype == 0) {
                                showDialogSendMessage(getActivity(), 6);
                            } else {
                                boolean statusReInvoice = saveInvoice(remarks, timeStamp, RemarksType.REINVOICE.toString());
                                Products productsController = new Products(getActivity());
                                int count = productsController.getRowCount();
                                if (count > 0) {
                                    if (statusReInvoice) {
                                        new UploadRemarksTask(getActivity()).execute();
                                        //new UploadRemarksTask(getActivity()).execute();
                                        // Intent startInvoiceGen1 = new Intent("com.Indoscan.channelbridge.INVOICEGEN1ACTIVITY");
//                                    if (isInvoiceOption2) {
//                                        startInvoiceGen1 = new Intent("com.Indoscan.channelbridge.INVOICEGEN1ALTERNATE");
//                                    }else{
//                                        startInvoiceGen1 = new Intent(
//                                                "com.Indoscan.channelbridge.INVOICEGEN1ACTIVITY");
//
//                                    }
//                                    Bundle bundleToView = new Bundle();
//                                    bundleToView.putString("Id", rowID);
//                                    bundleToView.putString("PharmacyId", pharmacyId);
//                                    bundleToView.putString("onTimeOrNot","1");
//                                    startInvoiceGen1.putExtras(bundleToView);
//                                    startActivity(startInvoiceGen1);
                                        //                                   getActivity().finish();
                                        invoiceEdit.dismiss();
                                        temporyLoadDataTask1 = new InvocieTemporyLoadDataTask1(getActivity());
                                        temporyLoadDataTask1.execute();
                                    }
                                } else {
                                    Toast to = Toast.makeText(getActivity().getApplicationContext(), "Please synchronise products", Toast.LENGTH_SHORT);
                                    to.setGravity(Gravity.CENTER, 0, 0);
                                    to.show();
                                }

                            }


                        } catch (Exception e) {
                            Log.w("Unable to Start ReInvoice", e.toString());
                        }

                        break;

                    case R.id.rbReturnInvoiceHistoryValidated:
                        try {

                            if (customertype == 0) {
                                Toast featureNotEnabled = Toast.makeText(getActivity(), "Sorry, this feature has not been enabled", Toast.LENGTH_SHORT);
                                featureNotEnabled.setGravity(Gravity.TOP, 100, 100);
                                featureNotEnabled.show();
                            } else {
                                boolean statusAddNewInvoice = saveInvoice(remarks, timeStamp, RemarksType.RETURN_PRODUCT_WITH_HISTORY.toString());

                                if (statusAddNewInvoice) {
                                    txtEditInvoiceRemark.clearFocus();
                                    txtEditInvoiceRemark.setText(null);
                                    new UploadRemarksTask(getActivity()).execute();
                                    new UploadRemarksTask(getActivity()).execute();
                                    Intent startProductReturn = new Intent(getActivity(), ReturnProductHistoryActivity.class);

                                    Bundle bundleToView = new Bundle();
                                    bundleToView.putString("Id", rowID);
                                    bundleToView.putString("PharmacyId", pharmacyId);
                                    bundleToView.putString("onTimeOrNot", "1");
                                    startProductReturn.putExtras(bundleToView);
                                    getActivity().finish();
                                    invoiceEdit.dismiss();
                                    startActivity(startProductReturn);
                                } else {

                                }

                            }


                        } catch (Exception e) {
                            Log.w("error trying to start return product with history validated", e.toString());
                        }
                        break;


                    default:
                        break;
                }


            }
        });

        PopulateItineryDetails(rowID);

        btnGenerateInvoice.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {


                LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                Products productsController = new Products(getActivity());
                int count = productsController.getRowCount();
                Customers data = new Customers(getActivity());
                data.openReadableDatabase();

                DEL_Outstandiing deloustanding = new DEL_Outstandiing(getActivity());
                deloustanding.openReadableDatabase();


                if (statusOfGPS == true) {
                    if (count > 0) {

                        String isInvoiceallowed = data.getInvoiceAlloweStstusByPharmacyId(pharmacyId);
                        int aaa = 0;


                        if (isInvoiceallowed.equals("Null")) {
                            showDialogSendMessage(getActivity(), 4);
                        } else {
                            String tempCredit = tvCreditLimit.getText().toString();
                            String tempOutStanding = tvCurrntCredit.getText().toString();

                            if (tempCredit.isEmpty() || tempCredit == null) {
                                tempCredit = "0";
                            }
                            if (tempOutStanding.isEmpty() || tempOutStanding == null) {
                                tempOutStanding = "0";
                            }
                            double dCredit = Double.parseDouble(tempCredit);
                            double dOutstanding = Double.parseDouble(tempOutStanding);


                            if (data.getCustomerBlockStatesByPharmacyId(pharmacyId).equals("1")) {
                                showDialogSendMessage(getActivity(), 5);
                            } else {
                                if (Integer.parseInt(isInvoiceallowed) == 0) {
                                    showDialogSendMessage(getActivity(), 1);
                                } else {
                                    if (Integer.parseInt(data.getMaxInvoiceCountByPharmacyId(pharmacyId)) <= deloustanding.getOustandCount(pharmacyId)) {
                                        showDialogSendMessage(getActivity(), 2);
                                        data.setInvoiceAlloweStstus(pharmacyId, 0);
                                    } else {
                                        if (dOutstanding > dCredit) {
                                            showDialogSendMessage(getActivity(), 3);
                                            data.setInvoiceAlloweStstus(pharmacyId, 0);
                                        } else {
                                            temporyLoadDataTask1 = new InvocieTemporyLoadDataTask1(getActivity());
                                            temporyLoadDataTask1.execute();
                                        }
                                    }
                                }

                            }


                        }
                    } else {
                        Toast to = Toast.makeText(getActivity().getApplicationContext(), "Please synchronise products", Toast.LENGTH_SHORT);
                        to.setGravity(Gravity.CENTER, 0, 0);
                        to.show();
                    }
//                        if (isInvoiceOption2) {
//                            startInvoiceGen1 = new Intent("com.Indoscan.channelbridge.INVOICEGEN1ALTERNATE");
//                        }else{
//                            startInvoiceGen1 = new Intent(
//                                    "com.Indoscan.channelbridge.INVOICEGEN1ACTIVITY");
//
//                        }
//                        String timeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
//                                .format(new Date().getTime());
//                        Log.i("time frag -e->",timeStamp );
//                        Bundle bundleToView = new Bundle();
//                        bundleToView.putString("Id", rowID);
//                        bundleToView.putString("PharmacyId", pharmacyId);
//                        bundleToView.putString("startTime",timeStamp);
//                        SharedPreferences preferences = PreferenceManager
//                                .getDefaultSharedPreferences(getActivity().getBaseContext());
//                        SharedPreferences.Editor editor = preferences.edit();
//                        editor.putBoolean("AutoSyncRun", false);
//                        editor.commit();
//
//                        cusName=tViewName.getText().toString();
//                        contactNumber=tViewTelephone.getText().toString();
//                        pharmacyId1=pharmacyId;
//
//                        startInvoiceGen1.putExtras(bundleToView);
//                        startActivity(startInvoiceGen1);
                    //getActivity().finish();


                } else {
                    //  private void showGPSDisabledAlertToUser(){

                    showGpsAlert();

                }


                //     }


                // TODO Auto-generated method stub


            }
        });


        iBtnEditInvoice.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                if (statusOfGPS == true) {
                    Invoice invoiceObject = new Invoice(getActivity());
                    invoiceObject.openReadableDatabase();
                    ArrayList<String> invoicedIds = invoiceObject.getInvoiceIdByItineraryId(rowID);
                    invoiceObject.closeDatabase();

                    if (!invoicedIds.isEmpty()) {
                        String invoiceId = invoicedIds.get(invoicedIds.size() - 1);
                        tViewInvoiceNumber.setText(invoiceId);
                        invoiceEdit.show();
                    } else {
                        invoiceObject.openReadableDatabase();
                        List<String[]> invoices = invoiceObject.getAllInvoice();
                        invoiceObject.closeDatabase();
                        String invoiceId = String.valueOf(invoices.size() + 1);
                        tViewInvoiceNumber.setText(invoiceId);

                        Customers customersObject = new Customers(getActivity());
                        customersObject.openReadableDatabase();

                        String customerDetails = customersObject.getCustomerByPharmacyId(pharmacyId);

                        if (customerDetails == null) {
                            customertype = 0;

                        } else {
                            customertype = 1;
                        }
                        invoiceEdit.show();


                    }
                } else {
                    showGpsAlert();
                }


            }
            //
        });

        iBtnSaveRemark.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                String remarks = txtRemarks.getText().toString();
                String timeStamp = java.text.DateFormat
                        .getDateTimeInstance().format(
                                Calendar.getInstance().getTime());

                boolean status = saveInvoice(remarks, timeStamp, RemarksType.ITINERARY.toString());
                /**
                 * upload remarks
                 */

                if (status) {
                    new UploadRemarksTask(getActivity()).execute();
                    txtRemarks.clearFocus();
                    txtRemarks.setText(null);
                }
            }
        });

        btnViewCustomerDetails.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent tabWidget = new Intent(
                        "com.Indoscan.channelbridge.CUSTOMERDETAILSCOMMENTSTABWIDGET");

                Bundle bundleToView = new Bundle();
                bundleToView.putString("Id", rowID);
                bundleToView.putString("PharmacyId", pharmacyId);

                tabWidget.putExtras(bundleToView);
                startActivity(tabWidget);

                getActivity().finish();

            }
        });

        /*} catch (Exception e) {
            e.printStackTrace();

            String error = e.toString();

            AlertDialog alertDialog = new AlertDialog.Builder(
                    this.getActivity()).create();

            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });

            alertDialog.setTitle("Error");
            alertDialog.setMessage(error);
            alertDialog.show();
        }*/

        ScrollView scroller = new ScrollView(getActivity());
        return scroller;
    }

    public void itineraryStatus() {
        Itinerary itineraryObject = new Itinerary(getActivity());

        String lastInvoicedItinerary = "-1";


        itineraryObject.openReadableDatabase();
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String currentDate = sdfDateTime.format(new Date(System.currentTimeMillis()));
        List<String[]> result = itineraryObject.getAllItinerariesForADay(currentDate);
        itineraryObject.closeDatabase();

        for (String[] a : result) {
            if (a[9].contentEquals("true")) {
                Log.w("itinerary Details Frag", a[9] + "");
                lastInvoicedItinerary = a[0];
            }
        }

        if (lastInvoicedItinerary.contentEquals("-1")) {
            if (!result.isEmpty()) {
                String[] itn = result.get(0);
                itineraryObject.openWritableDatabase();
                itineraryObject.setIsActiveTrue(itn[0]);
                itineraryObject.closeDatabase();
            }

        } else {
            if (rowID.contentEquals(lastInvoicedItinerary)) {
                btnGenerateInvoice.setEnabled(true);
            } else {
                //  btnGenerateInvoice.setEnabled(false);
            }
        }
    }

    public void PopulateItineryDetails(String ROWID) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        String deviceId = sharedPreferences.getString("DeviceId", "-1");
        String repId = sharedPreferences.getString("RepId", "-1");

        Itinerary itinerary = new Itinerary(getActivity());
        itinerary.openReadableDatabase();
        String status = itinerary.getItineraryStatus(rowID);
        itinerary.closeDatabase();

        if (status.contentEquals("true")) {
            itinerary.openReadableDatabase();
            String[] itnDetails = itinerary.getItineraryDetailsForTemporaryCustomer(ROWID);
            itinerary.closeDatabase();
            String address = itnDetails[2] + ", " + itnDetails[3] + ", " + itnDetails[4] + ", " + itnDetails[5];
            tViewName.setText(itnDetails[0]);
            tViewTarget.setText(itnDetails[1]);
            tViewAddress.setText(address);
            tViewTelephone.setText(itnDetails[6]);
            pharmacyId = itnDetails[8];
            String primaryImage = null;
            String[] imgWord = pharmacyId.split("_");


            byte[] image = new byte[0];

            CustomersPendingApproval data = new CustomersPendingApproval(this.getActivity());
            data.openReadableDatabase();
            image = data.getByteArrayImage(imgWord[1]);
            data.closeDatabase();

          /*  Bitmap bm = BitmapFactory.decodeByteArray(image, 0, image.length);
            iViewCustomerPic.setImageBitmap(bm);*/
            try {
                ImageGallery imageGallery = new ImageGallery(getActivity());
                imageGallery.openReadableDatabase();
                primaryImage = imageGallery.getPrimaryImageforCustomerId(itnDetails[7]);
                imageGallery.closeDatabase();
            } catch (Exception e) {
                Log.w("Unable to get display pic", e.toString());
            }

            try {
                Log.w("Primary Image", primaryImage + "");
                File customerImageFile = new File(
                        Environment.getExternalStorageDirectory() + File.separator
                                + "DCIM" + File.separator + "Channel_Bridge_Images"
                                + File.separator + primaryImage);

                if (customerImageFile.exists()) {

                    try {
                        iViewCustomerPic.setImageBitmap(decodeSampledBitmapFromResource(
                                String.valueOf(customerImageFile), 400, 550));
                    } catch (IllegalArgumentException e) {
                        Log.w("Illegal argument exception", e.toString());
                    } catch (OutOfMemoryError e) {
                        Log.w("Out of memory error :(", e.toString());
                    }

                } else {

                }
            } catch (Exception e) {

            }

        } else if (status.contentEquals("false")) {
            itinerary.openReadableDatabase();
            String[] itnDetails = itinerary.getItineraryDetailsById(ROWID);
            itinerary.closeDatabase();
            String primaryImage = itnDetails[7];
            pharmacyId = itnDetails[4];

         //   try {

                byte[] image = new byte[0];
                Customers data = new Customers(this.getActivity());
                data.openReadableDatabase();
                image = data.getByteArrayImage(pharmacyId);
                data.closeDatabase();
/*
                Bitmap bm = BitmapFactory.decodeByteArray(image, 0, image.length);
                iViewCustomerPic.setImageBitmap(bm);*/


                Log.w("Primary Image", primaryImage + "");
                File customerImageFile = new File(Environment.getExternalStorageDirectory() + File.separator + "DCIM" + File.separator + "Channel_Bridge_Images" + File.separator + primaryImage);

                if (customerImageFile.exists()) {
                    try {
                        iViewCustomerPic.setImageBitmap(decodeSampledBitmapFromResource(
                                String.valueOf(customerImageFile), 400, 550));
                    } catch (IllegalArgumentException e) {
                        Log.w("Illegal argument exception", e.toString());
                    } catch (OutOfMemoryError e) {
                        Log.w("Out of memory error :(", e.toString());
                    }

                } else {
                    iViewCustomerPic.setImageResource(R.drawable.unknown_image);
                }
           // } catch (Exception e) {
           //     Log.w("Error setting image file", e.toString());
          //      iViewCustomerPic.setImageResource(R.drawable.unknown_image);
          //  }
            //			Log.w("Itn DETAILS LEngth", itnDetails.length+"");
//			Log.w("ITN DETAILS", itnDetails[0]);
//			Log.w("ITN DETAILS", itnDetails[1]);
//			Log.w("ITN DETAILS", itnDetails[2]);
//			Log.w("ITN DETAILS", itnDetails[3]);
            tViewName.setText(itnDetails[0]);
            tViewTarget.setText(itnDetails[1]);
            tViewAddress.setText(itnDetails[2]);
            tViewTelephone.setText(itnDetails[3]);
            pharmacyId = itnDetails[4];
        }

        String idd = pharmacyId;

        if (pharmacyId == null) {
            AlertDialog dialog;
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle("Warning");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setMessage("Please download customer master data");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent syncronizePreferences = new Intent("com.Indoscan.channelbridge.SYNCRONIZEPREFERENCE");
                            getActivity().finish();
                            startActivity(syncronizePreferences);
                        }
                    });
            dialog=alertDialogBuilder.create();
            dialog.show();

        } else {
            String invoDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            String xSum = "0";
            String invoNo = "";
            if (iswebApprovalActive == false) {
                xSum = invoHandler.getInvoiceSumforGivenDateAndCustomer(pharmacyId, invoDate);
                invoNo = invoHandler.getLastInvoiceForFivenDate(pharmacyId, invoDate);
            } else {
                String compCode = "";
                customerHandler.openReadableDatabase();
                compCode = customerHandler.getCompanyCodeFromPhamcyId(pharmacyId);
                customerHandler.closeDatabase();
                xSum = salesHandler.getInvoiceSumforGivenDateAndCustomer(compCode, invoDate);
                invoNo = salesHandler.getLastInvoiceForGivenDate(compCode, invoDate);
            }
            if (xSum.isEmpty() || xSum == null) {
                xSum = "0";
            }
            double dSum = Double.parseDouble(xSum);
            tViewInvoiceVal.setText(String.format("%.2f", dSum));
            String sTarget = tViewTarget.getText().toString();
            if (sTarget.isEmpty() || sTarget.equals("") || sTarget == null) {
                sTarget = "0";
            }

            double dTarget = Double.parseDouble(sTarget);
            double dVariance = 0.00;
            dVariance = dTarget - dSum;
            Log.i("vari", "" + dVariance);


            customerHandler.openReadableDatabase();
            String[] selectedCustomer = customerHandler.getCustomerDetailsByPharmacyId(pharmacyId);
            customerHandler.closeDatabase();
            String currCredit = "0";

            try {
                currCredit = selectedCustomer[15];
                tvCurrntCredit.setText(String.format("%.2f", Double.parseDouble(currCredit)));
            } catch (NullPointerException nul) {
                currCredit = "0";
                tvCurrntCredit.setText("0");
            }

            tvCreditLimit.setText(selectedCustomer[14]);

            tvVariancefr.setText(String.format("%.2f", dVariance));
            tvInvoiceNumber.setText(invoNo);
        }


    }

    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {

        File direct = new File(Environment.getExternalStorageDirectory() + "/DCIM/Channel_Bridge_Images");

        if (!direct.exists()) {
            File wallpaperDirectory = new File("/sdcard/DCIM/Channel_Bridge_Images/");
            wallpaperDirectory.mkdirs();
        }

        File file = new File(new File("/sdcard/DCIM/Channel_Bridge_Images/"), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * should update the
     *
     * @param remark
     * @param timestamp
     * @return
     */

    public boolean saveInvoice(String remark, String timestamp, String remarkType) {

        repconnector = new Reps(getActivity());
        SimpleDateFormat sdfDateTime2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
        repconnector.openReadableDatabase();


        repconnector.openReadableDatabase();
        List<String[]> repList = repconnector.getAllRepsDetails();
        repconnector.closeDatabase();
        if (!repList.isEmpty()) {
            for (String[] ids : repList) {
                repId = ids[1];
            }
        }

        Itinerary itinerary = new Itinerary(getActivity());
        itinerary.openReadableDatabase();
        itenararyDate = itinerary.getDateforSelectedROWID(rowID);
        globalPharmaId = itinerary.getGlobalPharmaIDForRowID(rowID);
        Log.i("Itinarary Date ****", "---->" + itenararyDate);
        Log.i("Pharmacy ID ****", "---->" + globalPharmaId);
        Log.i("row    ID ****", "---->" + rowID);

        String[] itineraryDetails = itinerary.getItineraryDetailsById(rowID);
        for (int i = 0; i < itineraryDetails.length; i++) {
            Log.i("server  ID ****", "---->" + itineraryDetails[i]);
        }

        Log.i("server  ID ****", "---->" + itineraryDetails[1]);
        itinerary.closeDatabase();
        getGPS();
        turnGPSOff();
        if (!remark.isEmpty()) {
            try {
                Remarks remarksObject = new Remarks(getActivity().getApplicationContext());
                remarksObject.openWritableDatabase();
                String itiID;
                if (itineraryDetails[8] == null) {
                    itinerary.openReadableDatabase();
                    itiID = itinerary.getItineraryDetailsByIdForNewCus(rowID);
                    itinerary.closeDatabase();
                } else {
                    itiID = itineraryDetails[8];
                }

                long result = remarksObject.insertRemark(rowID, remark, timestamp, itenararyDate, globalPharmaId, repId, remarkType, "0", itiID, Double.toString(lng), Double.toString(lat));


                Log.w("Remarks Table: ", String.valueOf(result));
                remarksObject.closeDatabase();
                if (result != -1) {
                    Toast toast = Toast.makeText(getActivity(),
                            "Remark has been added",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 50, 100);
                    toast.show();

                    Invoice invoiceObject = new Invoice(getActivity());
                    invoiceObject.openReadableDatabase();
                    ArrayList<String> invoices = new ArrayList<String>();
                    invoices = invoiceObject.getInvoiceIdByItineraryId(rowID);
                    invoiceObject.closeDatabase();

                    if (invoices.isEmpty()) {
                        Itinerary itineraryObject = new Itinerary(getActivity());
                        itineraryObject.openReadableDatabase();
                        SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        String currentDate = sdfDateTime.format(new Date(System.currentTimeMillis()));
                        List<String[]> resultforToday = itineraryObject.getAllItinerariesForADay(currentDate);
                        itineraryObject.closeDatabase();

                        int nxtItn = 0;
                        for (int i = 0; i < resultforToday.size(); i++) {
                            String[] temp = resultforToday.get(i);
                            if (temp[0].contentEquals(rowID)) {
                                nxtItn = i + 1;
                            }
                        }
                        if (nxtItn < resultforToday.size()) {
                            String[] temp = resultforToday.get(nxtItn);

                            itineraryObject.openWritableDatabase();
                            itineraryObject.setIsActiveTrue(temp[0]);
                            itineraryObject.closeDatabase();
                        }

                    }

                    return true;
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(
                            getActivity()).create();

                    alertDialog.setButton("OK",
                            new DialogInterface.OnClickListener() {

                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {
                                    return;
                                }
                            });

                    alertDialog.setTitle("Alert");
                    alertDialog
                            .setMessage("Oops! Something Fent wrong, Please try again or contact Administrator");
                    alertDialog.show();
                    return false;
                }

            } catch (Exception e) {
                Log.w("Inserting Remarked: ", e.toString());
                return false;
            }
        } else {
            Toast toast = Toast.makeText(getActivity(),
                    "Remark Feild is Empty!", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 50, 100);
            toast.show();
            return false;
        }
    }


    public class InvocieTemporyLoadDataTask1 extends AsyncTask<Void, Void, Void> {

        private Context context;
        private ProductRepStore productRepStoreController;
        private TemporaryInvoice temporaryInvoiceController;
        private ArrayList<Product> repStockList;
        private ProgressDialog dialog;

        public InvocieTemporyLoadDataTask1(Context context) {
            this.context = context;
            productRepStoreController = new ProductRepStore(context);
            temporaryInvoiceController = new TemporaryInvoice(context);
            repStockList = new ArrayList<>();

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            productRepStoreController.openReadableDatabase();
            temporaryInvoiceController.openWritableDatabase();
            temporaryInvoiceController.deleteAllRecords();
            dialog = new ProgressDialog(context);
            this.dialog.setMessage("Please wait");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            this.dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (isInvoiceOption2) {
                repStockList = productRepStoreController.getAllRepAtoreDetails();

                for (Product repStock : repStockList) {
                    temporaryInvoiceController.insertTempInvoStock(repStock);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            productRepStoreController.closeDatabase();
            temporaryInvoiceController.closeDatabase();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (isInvoiceOption2) {

                startInvoiceGen1 = new Intent("com.Indoscan.channelbridge.INVOICEGEN1ALTERNATE");
            } else {

                startInvoiceGen1 = new Intent("com.Indoscan.channelbridge.INVOICEGEN1ACTIVITY");

            }
            String timeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS")
                    .format(new Date().getTime());
            Log.i("time frag -e->", timeStamp);
            Bundle bundleToView = new Bundle();
            bundleToView.putString("Id", rowID);
            bundleToView.putString("PharmacyId", pharmacyId);
            bundleToView.putString("startTime", timeStamp);
            bundleToView.putString("onTimeOrNot", "1");

            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(getActivity().getBaseContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("AutoSyncRun", false);
            editor.commit();

            cusName = tViewName.getText().toString();
            contactNumber = tViewTelephone.getText().toString();
            pharmacyId1 = pharmacyId;

            startInvoiceGen1.putExtras(bundleToView);

            startActivity(startInvoiceGen1);
            getActivity().finish();
        }
    }

    private void showGpsAlert() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Go to Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

//                                Intent iternaryListActivity = new Intent(
//                                        "com.Indoscan.channelbridge.ITINERARYLIST");
//                                startActivity(iternaryListActivity);
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);


                                // GetGPS();
                                // dialog.cancel();

                                // finish();
                                ////------


                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  dialog.cancel();
//                        Intent iternaryListActivity = new Intent(
//                                "com.Indoscan.channelbridge.ITINERARYLIST");
//                        startActivity(iternaryListActivity);
                        //  finish();
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    private void getGPS() {

        String GPS = "";

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 25, this);
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        if (location != null) {


            lat = (double) (location.getLatitude());
            lng = (double) (location.getLongitude());


        }

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void turnGPSOff() {
        String provider = Settings.Secure.getString(getActivity().getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (provider.contains("gps")) { //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            this.getActivity().getApplicationContext().sendBroadcast(poke);
        }
    }

    //Himanshu
    public void showDialogSendMessage(Context context, final int status) {

        final Dialog dialogBox = new Dialog(context);
        dialogBox.setTitle("Invoice Approval");
        dialogBox.setContentView(R.layout.dialog_send_approval);
        dialogBox.setCancelable(true);

        String reason = null;
        cusName = tViewName.getText().toString();


        final TextView txtMessage = (TextView) dialogBox.findViewById(R.id.textView_dialogsendapproval_customername);

        final RelativeLayout layoutResend = (RelativeLayout) dialogBox.findViewById(R.id.layout_dialogsendapproval_resend);
        final TextView txtPhoneNumber = (TextView) dialogBox.findViewById(R.id.textView_dialogsendapproval_phoneNumber);
        final Spinner spinPerson = (Spinner) dialogBox.findViewById(R.id.spinner_approve_person);
        final EditText edtComment = (EditText) dialogBox.findViewById(R.id.editText_dialogsendapproval_comment);
        final EditText edtCode = (EditText) dialogBox.findViewById(R.id.editTextdialogsendapproval_code);
        Button btnContinue = (Button) dialogBox.findViewById(R.id.buttondialogsendapproval_continue);
        final Button btnSende = (Button) dialogBox.findViewById(R.id.buttondialogsendapproval_send);
        Button btnCancel = (Button) dialogBox.findViewById(R.id.buttondialogsendapproval_cancel);


        final Approval_Persons ap = new Approval_Persons(getActivity());
        ap.openReadableDatabase();

        final Approval_Details aPProDetails = new Approval_Details(getActivity());
        aPProDetails.openWritableDatabase();

        final Reps rep = new Reps(getActivity());
        rep.openReadableDatabase();

        final Customers data = new Customers(getActivity());
        data.openReadableDatabase();

        final ArrayList<String> approvalPersonsList = ap.getAllPerson();
        ArrayAdapter<String> pesronNameAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, approvalPersonsList);

        pesronNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinPerson.setAdapter(pesronNameAdapter);


        if (aPProDetails.checkAccessibility(pharmacyId) == 0) {
            // edtCode.setEnabled(false);
            layoutResend.setEnabled(false);
        } else {
            btnSende.setEnabled(false);
            //  edtCode.setEnabled(true);

        }

        if (status == 1) {
            reason = "invoice is not allowed";
            txtMessage.setText(cusName + ",This customer invoice is not allowed.Do you want to send for approval and proceed ?");
        } else if (status == 2) {
            reason = "invoice count exceeded";
            txtMessage.setText(cusName + ",Invoice count exceeded .Do you want to send for approval and proceed ?");
        } else if (status == 3) {
            reason = "credit limit exceeded";
            txtMessage.setText(cusName + ",Credit limit exceeded.Do you want to send for approval and proceed ?");
        } else if (status == 4) {
            reason = "new customer invoice ";
            txtMessage.setText(cusName + ",New customer.Do you want to send for approval and proceed ?");
        } else if (status == 5) {
            reason = "customer block";
            txtMessage.setText(cusName + ",this customer has been blocked.Do you want to send for approval and proceed ?");
        } else if (status == 6) {
            reason = "return invoice";
            txtMessage.setText(cusName + ",this customer has been blocked.Do you want to send for approval and proceed ?");
        }


        final String pesronName = approvalPersonsList.get(0);
        spinPerson.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        int position = spinPerson.getSelectedItemPosition();
                        String pesronName = approvalPersonsList.get(position);
                        txtPhoneNumber.setText("Phone Number : " + String.valueOf(ap.getPhoneNumberByPersonName(pesronName)));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO Auto-generated method stub

                    }

                }
        );
        txtPhoneNumber.setText("Phone Number : " + String.valueOf(ap.getPhoneNumberByPersonName(pesronName)));


        //button
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ap.closeDatabase();
                aPProDetails.closeDatabase();
                rep.closeDatabase();
                data.closeDatabase();
                dialogBox.dismiss();
            }
        });


        final String finalReason = reason;
        btnSende.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String code = nextSessionId();
                Date date = new Date(System.currentTimeMillis());
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                final String repId = sharedPreferences.getString("RepId", "-1");
                String msg = "Rep: " + rep.getRepNameByRepId(repId) + " Customer: " + cusName + " Comment: " + edtComment.getText().toString() + " Date: " + dateFormat.format(date) + " Code: " + code;


                boolean resultSend = sendSMS(String.valueOf(ap.getPhoneNumberByPersonName(pesronName)), msg);

                if (resultSend == true) {
                    aPProDetails.insertDetails(pharmacyId, code, finalReason, edtComment.getText().toString(), pesronName);
                    edtComment.setText("");
                    showErrorMessage("Send Approval Successfully");
                    btnSende.setEnabled(false);
                    edtCode.setEnabled(true);
                    layoutResend.setEnabled(true);

                } else {
                    showErrorMessage("Send Approval Fail,please try again");
                }

            }
        });


        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (edtCode.getText().toString().isEmpty()) {
                    showErrorMessage("Code is empty,please try again");

                } else if (edtCode.getText().toString().equals("0000")) {
                    aPProDetails.setAccess(pharmacyId, edtCode.getText().toString());

                    data.setInvoiceAlloweStstus(pharmacyId, 1);
                    ap.closeDatabase();
                    aPProDetails.closeDatabase();
                    rep.closeDatabase();
                    data.closeDatabase();
                    dialogBox.dismiss();

                    if (isOnline()) {
                        new uploadApproveDetails().execute();
                    } else {

                    }

                    if (status == 6) {
                        String remarks = txtEditInvoiceRemark.getText().toString();
                        String timeStamp = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

                        boolean statusReInvoice = saveInvoice(remarks, timeStamp, RemarksType.REINVOICE.toString());
                        Products productsController = new Products(getActivity());
                        int count = productsController.getRowCount();
                        if (count > 0) {
                            if (statusReInvoice) {
                                new UploadRemarksTask(getActivity()).execute();
                                invoiceEdit.dismiss();
                                temporyLoadDataTask1 = new InvocieTemporyLoadDataTask1(getActivity());
                                temporyLoadDataTask1.execute();
                            }
                        } else {
                            Toast to = Toast.makeText(getActivity().getApplicationContext(), "Please synchronise products", Toast.LENGTH_SHORT);
                            to.setGravity(Gravity.CENTER, 0, 0);
                            to.show();
                        }
                    } else {
                        temporyLoadDataTask1 = new InvocieTemporyLoadDataTask1(getActivity());
                        temporyLoadDataTask1.execute();
                    }


                } else if (edtCode.getText().toString().length() == 4) {
                    boolean resultContinue = aPProDetails.checkCode(pharmacyId, edtCode.getText().toString());
                    if (resultContinue == true) {

                        aPProDetails.setAccess(pharmacyId, edtCode.getText().toString());

                        data.setInvoiceAlloweStstus(pharmacyId, 1);
                        ap.closeDatabase();
                        aPProDetails.closeDatabase();
                        rep.closeDatabase();
                        data.closeDatabase();
                        dialogBox.dismiss();

                        if (isOnline()) {
                            new uploadApproveDetails().execute();
                        } else {

                        }
                        if (status == 6) {
                            String remarks = txtEditInvoiceRemark.getText().toString();
                            String timeStamp = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

                            boolean statusReInvoice = saveInvoice(remarks, timeStamp, RemarksType.REINVOICE.toString());
                            Products productsController = new Products(getActivity());
                            int count = productsController.getRowCount();
                            if (count > 0) {
                                if (statusReInvoice) {
                                    new UploadRemarksTask(getActivity()).execute();
                                    invoiceEdit.dismiss();
                                    temporyLoadDataTask1 = new InvocieTemporyLoadDataTask1(getActivity());
                                    temporyLoadDataTask1.execute();
                                }
                            } else {
                                Toast to = Toast.makeText(getActivity().getApplicationContext(), "Please synchronise products", Toast.LENGTH_SHORT);
                                to.setGravity(Gravity.CENTER, 0, 0);
                                to.show();
                            }
                        } else {
                            temporyLoadDataTask1 = new InvocieTemporyLoadDataTask1(getActivity());
                            temporyLoadDataTask1.execute();
                        }
                    } else {
                        showErrorMessage("Invalid code,please try again");

                    }

                } else {
                    showErrorMessage("Invalid code,please try again");

                }

            }
        });


        layoutResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = nextSessionId();
                Date date = new Date(System.currentTimeMillis());
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
                int rowid = aPProDetails.checkAccessibility(pharmacyId);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                final String repId = sharedPreferences.getString("RepId", "-1");
                String msg = "Rep: " + rep.getRepNameByRepId(repId) + " Customer: " + cusName + " Comment: " + edtComment.getText().toString() + " Date: " + dateFormat.format(date) + " Code: " + code;

                if (sendSMS(String.valueOf(ap.getPhoneNumberByPersonName(pesronName)), msg)) {
                    boolean resultResend = aPProDetails.updateCode(rowid, code);
                    if (resultResend == true) {
                        showErrorMessage("Resend Approval Successfully");
                    } else {
                        showErrorMessage("Resend Approval Fail,please try again");
                    }
                } else {
                    showErrorMessage("Resend Approval Fail,please try again");
                }


            }
        });
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtComment.getWindowToken(), 0);
        // ap.closeDatabase();
        dialogBox.show();
    }

    public void showErrorMessage(String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public String nextSessionId() {
        String genCode = null;
        SecureRandom random = new SecureRandom();
        genCode = new BigInteger(20, random).toString(32);
        if (genCode.length() != 4) {
            genCode = genCode + 0;
        } else {

        }
        return genCode;
    }

    public boolean sendSMS(String number, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, msg, null, null);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

    }

    public boolean isOnline() {
        boolean flag = false;
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            flag = true;
        }
        return flag;
    }

    private class uploadApproveDetails extends AsyncTask<Void, Void, Void> {

        Approval_Details approvdetails;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            approvdetails = new Approval_Details(getActivity());
            approvdetails.openReadableDatabase();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final String repId = sharedPreferences.getString("RepId", "-1");

            List<String[]> rtnRemarks = approvdetails.getApprovaDetails();

            System.out.println("accesss cout " + rtnRemarks.size());


            for (java.lang.String[] rtnData : rtnRemarks) {
                java.lang.String[] remarksDetails = new String[10];
                remarksDetails[0] = rtnData[0];
                remarksDetails[1] = rtnData[1];
                remarksDetails[2] = rtnData[2];
                remarksDetails[3] = rtnData[3];
                remarksDetails[4] = rtnData[4];
                remarksDetails[5] = rtnData[5];
                remarksDetails[6] = rtnData[6];
                remarksDetails[7] = rtnData[7];
                remarksDetails[8] = rtnData[8];
                remarksDetails[9] = rtnData[9];

                String responseArr = null;

                while (responseArr == null) {
                    try {

                        WebService webService = new WebService();
                        responseArr = webService.uploadApprovalDetails(remarksDetails, repId);

                        if (responseArr.equals("OK")) {
                            approvdetails.updateUploadStstus(rtnData[0]);
                        }
                        Thread.sleep(100);

                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }

                }

            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);


        }

    }

}
