package com.Indoscan.channelbridge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.Indoscan.Entity.Product;
import com.Indoscan.Entity.RepStock;
import com.Indoscan.Entity.TempInvoiceStock;
import com.Indoscan.channelbridge.R;
import com.Indoscan.channelbridgebs.InvocieTemporyLoadDataTask;
import com.Indoscan.channelbridgedb.Approval_Details;
import com.Indoscan.channelbridgedb.Approval_Persons;
import com.Indoscan.channelbridgedb.Customers;
import com.Indoscan.channelbridgedb.CustomersPendingApproval;
import com.Indoscan.channelbridgedb.DiscountStructures;
import com.Indoscan.channelbridgedb.ProductRepStore;
import com.Indoscan.channelbridgedb.Products;
import com.Indoscan.channelbridgedb.Reps;
import com.Indoscan.channelbridgedb.Sequence;
import com.Indoscan.channelbridgedb.ShelfQuantity;
import com.Indoscan.channelbridgedb.TemporaryInvoice;
import com.Indoscan.channelbridgews.WebService;

import java.math.BigInteger;
import java.net.SocketException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Amila on 11/12/15.
 */
public class InvoiceGen1Alternate extends Activity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private TableLayout tblTest, tblHeader;
    private String[] columns = {"Code", "Product Name", "Batch","Exp Date", "Stock", "Shelf", "Request", "Order", "Free", "Discount(%)"};
    private Button btnAdd;

    private Products productController;
    private ArrayList<String> principleList;
    private ArrayList<String> categoryList;
    private ArrayList<Product> prductList;
    Spinner spPrinciple, spCategory;
    ArrayAdapter<String> principleAdapter;
    ArrayAdapter<String> categoryAdapter;
    InvocieTemporyLoadDataTask temporyLoadDataTask;
    TemporaryInvoice tempInvoiceStockController;
    private ProductRepStore productRepStoreController;
    private TemporaryInvoice temporaryInvoiceController;
    private ArrayList<RepStock> repStockList;
    //private Boolean isChanged;
    AlertDialog.Builder alertCancel;
    String rowId, pharmacyId, selectedBatch;
    //should change this
    private Boolean iswebApprovalActive = true;
    //  private Boolean isFirstTime = false,isChanged = false,isFirstTimeCategory = false;
    private Boolean isChanged = false;
    ArrayList<ReturnProduct> returnProductsArray;
    ArrayList<SelectedProduct> mergeList;
    boolean chequeEnabled = false;
    String collectionDate = "", releaseDate = "", chequeNumber = "";
    String startTime = "",cusName;

    boolean manualFreeEnable;
    CheckBox freeIssue;


    double tot;
TextView totalValue;

    boolean multipaleBatchStatus =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invoice_gen_1_option2);
//        temporyLoadDataTask = new InvocieTemporyLoadDataTask(getApplicationContext());
//        temporyLoadDataTask.execute();
        SharedPreferences shared = getApplicationContext().getSharedPreferences("CBHesPreferences", Context.MODE_PRIVATE);
        iswebApprovalActive = (shared.getBoolean("WebApproval", true));
        Log.i("iswebApprovalActive  ---> ", iswebApprovalActive.toString());
//        isFirstTime = true;
//        isChanged = true ;
//        isFirstTimeCategory = true;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        returnProductsArray = new ArrayList<ReturnProduct>();
        if (savedInstanceState != null) {
            getDataFromPreviousActivity(savedInstanceState);
        } else {
            getDataFromPreviousActivity(getIntent().getExtras());
        }
        initializeVariables();
//        temporyLoadDataTask.execute();



    }

    private void initializeVariables() {
        productController = new Products(getApplicationContext());
        principleList = new ArrayList<>();
        categoryList = new ArrayList<>();
        prductList = new ArrayList<>();
        mergeList = new ArrayList<>();
        productController.openReadableDatabase();
        principleList = productController.getPrincipleList();
        tempInvoiceStockController = new TemporaryInvoice(getApplicationContext());
        productController.closeDatabase();
        //
        productRepStoreController = new ProductRepStore(getApplicationContext());
        temporaryInvoiceController = new TemporaryInvoice(getApplicationContext());
        repStockList = new ArrayList<>();

        spPrinciple = (Spinner) findViewById(R.id.spPrinciple);
        spCategory = (Spinner) findViewById(R.id.spCategory);
        spPrinciple.setOnItemSelectedListener(this);
        spCategory.setOnItemSelectedListener(this);
        tblTest = (TableLayout) findViewById(R.id.tblTest);
        tblHeader = (TableLayout) findViewById(R.id.tblheader);
        btnAdd = (Button) findViewById(R.id.btnNextToPayment);
        btnAdd.setOnClickListener(this);
        freeIssue =(CheckBox)findViewById(R.id.checkBox_freeIssues);

        totalValue =(TextView)findViewById(R.id.textViewtot);
        totalValue.setText("Total Value : "+String.valueOf(tempInvoiceStockController.getCurrentTotal()));



       if(manualFreeEnable==true){
            freeIssue.setChecked(true);
            freeIssue.setEnabled(false);
        }else {
            freeIssue.setEnabled(true);
        }
        setTableHeader();
///
//        productRepStoreController.openReadableDatabase();
//
//        repStockList = productRepStoreController.getAllRepstores();
//        productRepStoreController.closeDatabase();
//        temporaryInvoiceController.openWritableDatabase();
//        for (RepStock repStock:repStockList){
//            temporaryInvoiceController.insertTempInvoStock(repStock.getProductCode(),repStock.getBatchCode());
//        }
//
//        temporaryInvoiceController.closeDatabase();
        /////

        principleAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.single_list_item, principleList);
        spPrinciple.setAdapter(principleAdapter);
        refreshViewOnSelection();
//        String selected = spPrinciple.getSelectedItem().toString();
//        categoryList = productController.getCategoryListForPriciple(selected);
//        categoryAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.single_list_item,categoryList);
//        spCategory.setAdapter(categoryAdapter);
//        String selectedCategory = spCategory.getSelectedItem().toString();
//        prductList = productController.getProductsByPricipleAndCategory(selected,selectedCategory);
//        populateProductTable(prductList);


        freeIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(manualFreeEnable==true){

                }else {
                    showDialogSendMessage(InvoiceGen1Alternate.this,1);
                }
            }
        });
    }


    private void getDataFromPreviousActivity(Bundle extras) {

        try {
//			Bundle extras = getIntent().getExtras();
            rowId = extras.getString("Id");
            pharmacyId = extras.getString("PharmacyId");
            startTime = extras.getString("startTime");
            Log.i("time gen1 -e->", startTime);
            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());
            chequeEnabled = preferences.getBoolean("cbPrefEnableCheckDetails", true);
            manualFreeEnable =extras.getBoolean("ManualFreeEnable");

            if (chequeEnabled) {
                if (extras.containsKey("ChequeNumber")) {
                    chequeNumber = extras.getString("ChequeNumber");
                }
                if (extras.containsKey("CollectionDate")) {
                    collectionDate = extras.getString("CollectionDate");
                }
                if (extras.containsKey("ReleaseDate")) {
                    releaseDate = extras.getString("ReleaseDate");
                }
                Log.w("cheque details", chequeNumber + " # " + collectionDate + " # " + releaseDate);

            }


            if (extras.containsKey("ReturnProducts")) {
                returnProductsArray = extras.getParcelableArrayList("ReturnProducts");
            }
//            if (extras.containsKey("SelectedProducts") == true) {
//                selectedProductsArray = extras.getParcelableArrayList("SelectedProducts");
//                Log.w("tempDataFromIG2.size()", selectedProductsArray.size() + "");
//                for (SelectedProduct selectedProduct : selectedProductsArray) {
//                    Log.w("IG1", "Inside For loop (get data from prev activity)" + pharmacyId);
//                    String[] tempData = new String[14];
//                    tempData[0] = String.valueOf(selectedProduct.getRowId());
//                    tempData[1] = String.valueOf(selectedProduct.getProductId());
//                    tempData[2] = String.valueOf(selectedProduct.getProductCode());
//                    tempData[3] = String.valueOf(selectedProduct.getProductBatch());
//                    tempData[4] = String.valueOf(selectedProduct.getQuantity());
//                    tempData[5] = String.valueOf(selectedProduct.getExpiryDate());
//                    tempData[6] = String.valueOf(selectedProduct.getTimeStamp());
//                    tempData[7] = String.valueOf(selectedProduct.getRequestedQuantity());
//                    tempData[8] = String.valueOf(selectedProduct.getFree());
//                    tempData[9] = String.valueOf(selectedProduct.getNormal());
//                    tempData[10] = String.valueOf(selectedProduct.getDiscount());
//                    tempData[11] = String.valueOf(selectedProduct.getProductDescription());
//                    tempData[12] = String.valueOf(selectedProduct.getPrice());
//                    tempData[13] = String.valueOf(selectedProduct.getShelfQuantity());
//                    Log.w("tempDataFromIG2[0]", tempData[0]);
//                    Log.w("tempDataFromIG2[1]", tempData[1]);
//                    Log.w("tempDataFromIG2[2]", tempData[2]);
//                    Log.w("tempDataFromIG2[3]", tempData[3]);
//                    Log.w("tempDataFromIG2[4]", tempData[4]);
//                    Log.w("tempDataFromIG2[5]", tempData[5]);
//                    Log.w("tempDataFromIG2[6]", tempData[6]);
//                    Log.w("tempDataFromIG2[7]", tempData[7]);
//                    Log.w("tempDataFromIG2[8]", tempData[8]);
//                    Log.w("tempDataFromIG2[10]", tempData[10]);
//                    Log.w("tempDataFromIG2[11]", tempData[11]);
//                    Log.w("tempDataFromIG2[12]", tempData[12]);
//                    selectedProductList.add(tempData);
//
//                }
//            }
//            Log.w("IG1", "Pharmacy id " + pharmacyId);
//            Log.w("IG1", "rowId " + rowId);

        } catch (Exception e) {
            Log.w("InvoiceGen1: ", e.toString());
        }
    }

    private void setTableHeader() {

        TableRow header = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        //  lp.setMargins(5,5,5,5);
        //lp.weight = 1;
        header.setLayoutParams(lp);

        TextView tvProducCode = new TextView(this);
        tvProducCode.setText("Code");
        tvProducCode.setLayoutParams(lp);
        tvProducCode.setMinWidth(100);

        TextView tvProductName = new TextView(this);
        tvProductName.setText("Product Name");
        tvProductName.setLayoutParams(lp);
        tvProductName.setMinWidth(300);



        TextView tvBatch = new TextView(this);
        tvBatch.setText("Batch");
        tvBatch.setLayoutParams(lp);
        tvBatch.setMinWidth(100);

        TextView tvExdate = new TextView(this);
        tvExdate.setText("Exp Date");
        tvExdate.setLayoutParams(lp);
        tvExdate.setMinWidth(100);



        TextView tvStock = new TextView(this);
        tvStock.setText("Stock");
        tvStock.setLayoutParams(lp);
        tvStock.setMinWidth(80);

         TextView tvPrice = new TextView(this);
         tvPrice.setText("Price");
         tvPrice.setLayoutParams(lp);
         tvPrice.setMinWidth(80);

        TextView tvShelfQuantity = new TextView(this);
        tvShelfQuantity.setText("Shelf");
        tvShelfQuantity.setLayoutParams(lp);
        tvShelfQuantity.setMinWidth(90);

        TextView tvRequest = new TextView(this);
        tvRequest.setText("Request");
        tvRequest.setLayoutParams(lp);
        tvRequest.setMinWidth(100);

        TextView tvormal = new TextView(this);
        tvormal.setText("Order");
        tvormal.setLayoutParams(lp);
        tvormal.setMinWidth(90);

        TextView tvFree = new TextView(this);
        tvFree.setText("Free");
        tvFree.setLayoutParams(lp);
        tvFree.setMinWidth(90);

        TextView tvDiscount = new TextView(this);
        tvDiscount.setText("Discount(%)");
        tvDiscount.setLayoutParams(lp);
        tvDiscount.setMinWidth(100);


        header.addView(tvProducCode);
        header.addView(tvProductName);

        header.addView(tvBatch);
        header.addView(tvExdate);
        header.addView(tvStock);
        header.addView(tvPrice);
        header.addView(tvShelfQuantity);
        header.addView(tvRequest);
        header.addView(tvormal);
        header.addView(tvFree);
        header.addView(tvDiscount);


        // header.

        tblHeader.addView(header, 0);
    }

    private void populateProductTable(ArrayList<Product> prductList) {
        String preProduct = "";

        tempInvoiceStockController.openReadableDatabase();
        final DiscountStructures discoutStrac = new DiscountStructures(InvoiceGen1Alternate.this);
        discoutStrac.openReadableDatabase();


        try {
            for (final Product product : prductList) {

                final TempInvoiceStock stock = tempInvoiceStockController.getTempData(product.getCode(), product.getBatchNumber());
                final TempInvoiceStock stockfull = tempInvoiceStockController.getStockcountMultipaleBatch(product.getCode());
                final TempInvoiceStock stockBatchCount = tempInvoiceStockController.checkMultipaleBatchcount(product.getCode());
                TableRow dataRow = new TableRow(this);
                TableRow.LayoutParams lpInner = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                lpInner.weight = 1;
                dataRow.setLayoutParams(lpInner);


                // TableRow.LayoutParams paramsforEdittext = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1);
             //   int batchcount = tempInvoiceStockController.checkMultipaleBatch(product.getCode());

                //0
                final TextView proNumber = new TextView(this);
                proNumber.setText(product.getCode());
                proNumber.setTextColor(Color.BLACK);
                proNumber.setMinWidth(100);
                proNumber.setTextSize(12);



                TextView proName = new TextView(this);
                proName.setText(product.getProDes());
                proName.setTextColor(Color.BLUE);
                // proName.setTypeface(null, Typeface.BOLD);
                proName.setSingleLine(false);
                proName.setMaxLines(3);
                proName.setMinWidth(300);
                proName.setWidth(300);
                proName.setTextSize(12);
                // proName.setLayoutParams(paramsExample);
                proName.setLines(3);


                //2
                TextView proBatch = new TextView(this);
                proBatch.setText(product.getBatchNumber());
                proBatch.setTextColor(Color.BLACK);
                proName.setTypeface(null, Typeface.BOLD);
                proBatch.setMinWidth(100);
                proBatch.setTextSize(12);


                TextView proEx = new TextView(this);
                proEx.setText(product.getExpiryDate().substring(0,10));
                proEx.setTextColor(Color.BLACK);
                proEx.setMinWidth(100);
                proEx.setTextSize(12);


                //3


                TextView proStock = new TextView(this);
                proStock.setText("" + stockfull.getStockFull());
                proStock.setTextColor(Color.RED);
                proName.setTypeface(null, Typeface.BOLD);
                proStock.setMinWidth(80);
                //proStock.setTextSize(13);

                //4
                TextView proPrice = new TextView(this);
                proPrice.setText("" + product.getSellingPrice());
                proPrice.setTextColor(Color.RED);
                proPrice.setMinWidth(60);
                proPrice.setTypeface(null, Typeface.BOLD);


                final EditText edSQuantity = new EditText(this);
                final EditText edNormal = new EditText(this);
                final EditText edQuantity = new EditText(this);
                final EditText edRequest = new EditText(this);
                final EditText edDiscount = new EditText(this);

                if(manualFreeEnable==true || (tempInvoiceStockController.getFree(product.getCode())==0)){
                    edQuantity.setEnabled(true);
                }else {
                    edQuantity.setEnabled(false);
                }


                edSQuantity.setInputType(InputType.TYPE_CLASS_NUMBER);
                edSQuantity.setLayoutParams(lpInner);
                // edSQuantity.setLayoutParams(lpInner);
                edSQuantity.setBackgroundResource(R.drawable.cell_border);
                //   if (stock.getShelfQuantity() == null) {
                edSQuantity.setText("" + tempInvoiceStockController.getShelf(product.getCode()));
                edSQuantity.setSelection(edSQuantity.getText().length());
//            }else{
//                edSQuantity.setText("" + stock.getShelfQuantity());
//            }
                edSQuantity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus == true) {
                            if(edSQuantity.getText().toString().equals("0")){
                                edSQuantity.setText("");
                            }else {

                            }

                        }
                    }
                });
                edSQuantity.setOnKeyListener(new View.OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == 66) {
                            if (edSQuantity.getText().toString().equals("0")) {
                                edSQuantity.setText("");
                            }else {

                            }
                        }
                        return false;
                    }
                });
                edSQuantity.setMinWidth(80);
                edSQuantity.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        //get parent of the edittext which has been changed
                        TableRow row = (TableRow) edSQuantity.getParent();
                        //may need to change inedx value if another cell added to the row
                        TextView tvDynaProNo = (TextView) row.getChildAt(0);
                        TextView tvDynaBatchNo = (TextView) row.getChildAt(2);
                        // tempInvoiceStockController.openWritableDatabase();
                        if ((!s.toString().isEmpty() || !s.toString().equals(""))  ) {
                            if(stockBatchCount.getBatchCount()==1) {
                                tempInvoiceStockController.updateShelfQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), s.toString());
                            }else {

                            }
                        }
                        // tempInvoiceStockController.openWritableDatabase();
                        isChanged = true;
                    }
                });


                edRequest.setInputType(InputType.TYPE_CLASS_NUMBER);
                edRequest.setLayoutParams(lpInner);
                edRequest.setBackgroundResource(R.drawable.cell_border);
                edRequest.setText("" + tempInvoiceStockController.getRequest(product.getCode()));
                edRequest.setSelection(edRequest.getText().length());
                edRequest.setOnKeyListener(new View.OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == 66) {
                            if (edRequest.getText().toString().equals("0")) {
                                edRequest.setText("");
                            }else {

                            }
                        }
                        return false;
                    }
                });
                edRequest.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus == true) {
                            if(edRequest.getText().toString().equals("0")){
                                edRequest.setText("");
                            }else {

                            }

                        }
                    }
                });
                //check whether entered quantity is valid depend on the approval
                edRequest.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {


                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        try {
                            TableRow row = (TableRow) edRequest.getParent();
                            //may need to change inedx value if another cell added to the row
                            TextView tvDynaProNo = (TextView) row.getChildAt(0);
                            TextView tvDynaBatchNo = (TextView) row.getChildAt(2);

                            TextView tvDynaStock = (TextView) row.getChildAt(4);
                            EditText edDynaNormal = (EditText) row.getChildAt(8);
                            EditText edDynadiscount = (EditText) row.getChildAt(10);
                            if (!s.toString().isEmpty() || !s.toString().equals("")) {

                                if(stockBatchCount.getBatchCount()>1) {

                                }else {

                                    tempInvoiceStockController.updateRequestQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), s.toString());


                                    if (iswebApprovalActive == true) {
                                        edDynaNormal.setText(s.toString());

                                        if (Double.parseDouble(tvDynaStock.getText().toString()) < Double.parseDouble(edRequest.getText().toString())) {
                                            Toast toast = Toast.makeText(InvoiceGen1Alternate.this, "Enter valid quantity", Toast.LENGTH_SHORT);
                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                            toast.show();
//                                edDynaNormal.setText(tvDynaStock.getText().toString());
                                            //((EditText) row.getChildAt(7)).setText("0"); //set free quantity to zero
                                        }

                                        Log.i(" b ->", edRequest.getParent().toString());
                                    } else {
                                        if (!s.toString().isEmpty() || !s.toString().equals("")) {
                                            if (Double.parseDouble(s.toString()) > Double.parseDouble(tvDynaStock.getText().toString())) {
                                                Toast toast = Toast.makeText(InvoiceGen1Alternate.this, "Enter valid quantity", Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER, 0, 0);
                                                toast.show();
                                                edDynaNormal.setText(tvDynaStock.getText().toString());
                                                ((EditText) row.getChildAt(9)).setText("0"); //set free quantity to zero
                                            } else {
                                                edDynaNormal.setText(edRequest.getText().toString());
                                            }
                                            int freeIssuesQty = discoutStrac.getFreeIssues(product.getCode(), Integer.parseInt(edRequest.getText().toString()));
                                            edQuantity.setText(String.valueOf(freeIssuesQty));
                                            edDynadiscount.setEnabled(false);

                                            int stock = Integer.parseInt(tvDynaStock.getText().toString());
                                            int request = 0;
                                            if (!edDynaNormal.getText().toString().equals("")) {
                                                request = Integer.parseInt(edDynaNormal.getText().toString());
                                            }
                                            int free = Integer.parseInt(edQuantity.getText().toString());

                                            if (iswebApprovalActive == false) {
                                                if (stock - request >= free) { // check whether entered free quantity is smaller than stock -  requested
                                                    tempInvoiceStockController.updateFreeQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), String.valueOf(free));
                                                    if (Double.parseDouble(s.toString()) > 0) {
                                                        ((EditText) row.getChildAt(9)).setEnabled(false);
                                                        tempInvoiceStockController.updateDiscountAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(false));
                                                    } else {
                                                        ((EditText) row.getChildAt(9)).setEnabled(true);
                                                        tempInvoiceStockController.updateDiscountAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(true));
                                                    }
                                                } else {
                                                    Toast freeToast = Toast.makeText(InvoiceGen1Alternate.this, "Not enough quantity", Toast.LENGTH_SHORT);
                                                    freeToast.setGravity(Gravity.CENTER, 0, 0);
                                                    freeToast.show();
                                                    edQuantity.setText("0");
                                                }
                                            } else {
                                                tempInvoiceStockController.updateFreeQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), String.valueOf(free));
                                                if (Double.parseDouble(s.toString()) > 0) {
                                                    ((EditText) row.getChildAt(9)).setEnabled(false);
                                                    tempInvoiceStockController.updateDiscountAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(false));
                                                } else {
                                                    ((EditText) row.getChildAt(9)).setEnabled(true);
                                                    tempInvoiceStockController.updateDiscountAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(true));
                                                }
                                            }


                                        }
                                    }


                                }

                            } else {
                                edDynaNormal.setText("0");
                            }
                            isChanged = true;
                        } catch (Exception e) {

                        }


                    }

                    @Override
                    public void afterTextChanged(Editable s) {


                    }
                });
                edRequest.setMinWidth(80);
                edQuantity.setInputType(InputType.TYPE_CLASS_NUMBER);
                edQuantity.setLayoutParams(lpInner);
                edQuantity.setBackgroundResource(R.drawable.cell_border);
                edQuantity.setText("" + tempInvoiceStockController.getFree(product.getCode()));
                edQuantity.setMinWidth(80);
              //  edQuantity.setEnabled(false);
                edQuantity.setSelection(edQuantity.getText().length());
                /*if(Boolean.valueOf(stock.getIsFreeAllowed()) == true){
                    edQuantity.setEnabled(true);
                }else{
                    edQuantity.setEnabled(false);
                }*/

                edQuantity.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        //get parent of the edittext which has been changed

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        TableRow row = (TableRow) edQuantity.getParent();
                        //may need to change inedx value if another cell added to the row
                        TextView tvDynaProNo = (TextView) row.getChildAt(0);
                        TextView tvDynaBatchNo = (TextView) row.getChildAt(2);
                        TextView tvDynaStock = (TextView) row.getChildAt(4);
                        EditText edDynaNormal = (EditText) row.getChildAt(8);
                        EditText edDynadiscount = (EditText) row.getChildAt(10);
                        if ((!s.toString().isEmpty() || !s.toString().equals("")) && multipaleBatchStatus == false) { // check whether that entered string is  empty

                            if (stockBatchCount.getBatchCount()>1) {

                            } else {


                                int stock = Integer.parseInt(tvDynaStock.getText().toString());
                                int request = 0;
                                if (!edDynaNormal.getText().toString().equals("")) {
                                    request = Integer.parseInt(edDynaNormal.getText().toString());
                                }
                                int free = Integer.parseInt(edQuantity.getText().toString());

                                if (iswebApprovalActive == false) {
                                    if (stock - request >= free) { // check whether entered free quantity is smaller than stock -  requested


                                        if(manualFreeEnable==true){

                                            tempInvoiceStockController.updateFreeSystemQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), String.valueOf(tempInvoiceStockController.getFreeByBatchAndCode(tvDynaProNo.getText().toString(),tvDynaBatchNo.getText().toString())));
                                            tempInvoiceStockController.updateFreeQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), s.toString());

                                        }else {
                                            tempInvoiceStockController.updateFreeQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), s.toString());

                                        }

                                        if(s.toString().equals("0")){
                                            ((EditText) row.getChildAt(9)).setEnabled(false);
                                            ((EditText) row.getChildAt(10)).setEnabled(true);
                                            tempInvoiceStockController.updateDiscountAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(true));

                                        }else {
                                            ((EditText) row.getChildAt(9)).setEnabled(true);
                                            ((EditText) row.getChildAt(10)).setEnabled(false);
                                            tempInvoiceStockController.updateDiscountAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(false));

                                        }


                                    } else {
                                        Toast freeToast = Toast.makeText(InvoiceGen1Alternate.this, "Not enough quantity", Toast.LENGTH_SHORT);
                                        freeToast.setGravity(Gravity.CENTER, 0, 0);
                                        freeToast.show();
                                        edQuantity.setText("0");
                                    }
                                } else {
                                    tempInvoiceStockController.updateFreeQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), s.toString());
                                    if(s.toString().equals("0")){
                                        ((EditText) row.getChildAt(9)).setEnabled(false);
                                        ((EditText) row.getChildAt(10)).setEnabled(true);
                                        tempInvoiceStockController.updateDiscountAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(true));

                                    }else {
                                        ((EditText) row.getChildAt(9)).setEnabled(true);
                                        ((EditText) row.getChildAt(10)).setEnabled(false);
                                        tempInvoiceStockController.updateDiscountAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(false));

                                    }                                }


                            }

                        }

                        isChanged = true;
                    }
                });


                edNormal.setInputType(InputType.TYPE_CLASS_NUMBER);
                edNormal.setLayoutParams(lpInner);
                edNormal.setBackgroundResource(R.drawable.cell_border);
                edNormal.setText("" + tempInvoiceStockController.getNormal(product.getCode()));
                edNormal.setMinWidth(80);
                edNormal.setSelection(edNormal.getText().length());
                edNormal.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus == true) {
                            if(edNormal.getText().toString().equals("0")){
                                edNormal.setText("");
                            }else {

                            }

                        }
                    }
                });
                edNormal.setOnKeyListener(new View.OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == 66) {
                            if (edNormal.getText().toString().equals("0")) {
                                edNormal.setText("");
                            }
                        }
                        return false;
                    }
                });
                edNormal.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        //get parent of the edittext which has been changed
                        TableRow row = (TableRow) edNormal.getParent();
                        //may need to change inedx value if another cell added to the row
                        TextView tvDynaProNo = (TextView) row.getChildAt(0);
                        TextView tvDynaBatchNo = (TextView) row.getChildAt(2);
                        TextView edDynadiscount = (TextView) row.getChildAt(10);


                        if ((!s.toString().isEmpty() || !s.toString().equals("")) ) {

                            int freeIssuesQty = discoutStrac.getFreeIssues(product.getCode(), Integer.parseInt(edRequest.getText().toString()));
                            edQuantity.setText(String.valueOf(freeIssuesQty));
                            edDynadiscount.setEnabled(false);

                            if(stockBatchCount.getBatchCount()>1) {

                            }else {
                                tempInvoiceStockController.updateNormalQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), s.toString());

                            }
                        }
                        isChanged = true;
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });


                edDiscount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                // edDiscount.setRawInputType(InputType.);
                edDiscount.setLayoutParams(lpInner);
                edDiscount.setBackgroundResource(R.drawable.cell_border);
                Log.i("tVal->", stock.getProductCode() + "_" + stock.getPercentage());
                edDiscount.setText("" + tempInvoiceStockController.getDis(product.getCode()));
                edDiscount.setMinWidth(80);
                edDiscount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus == true) {
                            if(edDiscount.getText().toString().equals("0.0")){
                                edDiscount.setText("");
                            }else {

                            }

                        }
                    }
                });
                edDiscount.setSelection(edQuantity.getText().length());
                if (Boolean.valueOf(stock.getIsDiscountAllowed()) == true) {
                    edDiscount.setEnabled(true);
                } else {
                    edDiscount.setEnabled(false);
                }
                edDiscount.setOnKeyListener(new View.OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == 66) {
                            if (edDiscount.getText().toString().equals("0.0")) {
                                edDiscount.setText("");
                            }
                        }
                        return false;
                    }
                });
                edDiscount.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        //get parent of the edittext which has been changed
                        TableRow row = (TableRow) edDiscount.getParent();
                        //may need to change inedx value if another cell added to the row
                        TextView tvDynaProNo = (TextView) row.getChildAt(0);
                        TextView tvDynaBatchNo = (TextView) row.getChildAt(2);
                        if(stockBatchCount.getBatchCount()>1) {
                        }else {
                        if ((!s.toString().isEmpty() || !s.toString().equals("")) ) {


                            if (s.length() < 4) {
                                if (Double.parseDouble(s.toString()) <= 100) {
                                    tempInvoiceStockController.updateDicount(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), s.toString());
                                    if (s.toString().equals("0")) {
                                        ((EditText) row.getChildAt(9)).setEnabled(true);
                                        ((EditText) row.getChildAt(10)).setEnabled(false);
                                        tempInvoiceStockController.updateFreeAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(true));
                                    } else {
                                        ((EditText) row.getChildAt(9)).setEnabled(false);
                                        ((EditText) row.getChildAt(10)).setEnabled(true);
                                        tempInvoiceStockController.updateFreeAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(false));
                                    }
                                } else {
                                    Toast toast1 = Toast.makeText(InvoiceGen1Alternate.this, "Enter valid discount", Toast.LENGTH_LONG);
                                    toast1.setGravity(Gravity.CENTER, 0, 0);
                                    toast1.show();
                                    edDiscount.setText("0.0");
                                }
                            } else {
                                Toast toast1 = Toast.makeText(InvoiceGen1Alternate.this, "Enter valid amount", Toast.LENGTH_LONG);
                                toast1.setGravity(Gravity.CENTER, 0, 0);
                                toast1.show();
                                edDiscount.setText("0.0");
                            }


                        }
                        }
                        isChanged = true;
                    }
                });
                Log.i("p --------", " -----------");
                Log.i("p ---> 1", preProduct.toString());
                Log.i("p ---> 2", product.getCode());
                Log.i("p --------", " -----------");

                String t = product.getCode();
                if (t.trim().toString().equalsIgnoreCase(preProduct.toString())) {
                    proNumber.setVisibility(View.INVISIBLE);
                    proName.setVisibility(View.INVISIBLE);
                    dataRow.setBackgroundResource(R.drawable.row_border_2);

                } else {
                    proNumber.setVisibility(View.VISIBLE);
                    proName.setVisibility(View.VISIBLE);
                    dataRow.setBackgroundResource(R.drawable.row_border);
                }

                if (iswebApprovalActive == false) {
                    if (stock.getStock() == 0) {
                        edQuantity.setEnabled(false);
                        edNormal.setEnabled(false);
                        edDiscount.setEnabled(false);
                    }
                } else {
                    if (stock.getStock() == 0) {
                        edQuantity.setEnabled(true);
                        edNormal.setEnabled(true);
                        edDiscount.setEnabled(true);
                    }
                }



                dataRow.addView(proNumber, 0);
                dataRow.addView(proName, 1);
                dataRow.addView(proBatch, 2);
                dataRow.addView(proEx, 3);
                dataRow.addView(proStock, 4);
                dataRow.addView(proPrice, 5);

                dataRow.addView(edSQuantity, 6);
                dataRow.addView(edRequest, 7);
                dataRow.addView(edNormal, 8);
                dataRow.addView(edQuantity, 9);
                dataRow.addView(edDiscount, 10);


                preProduct = product.getCode();
                tblTest.addView(dataRow);


                int batchcount = tempInvoiceStockController.checkMultipaleBatch(product.getCode());
                if (batchcount == 1) {
                    multipaleBatchStatus = false;
                    dataRow.setBackgroundColor(getResources().getColor(R.color.white));
                } else {
                    dataRow.setBackgroundColor(getResources().getColor(R.color.yellow));
                    edSQuantity.setEnabled(false);
                    edRequest.setEnabled(false);
                    edNormal.setEnabled(false);
                    edQuantity.setEnabled(false);
                    edDiscount.setEnabled(false);


                    multipaleBatchStatus = true;
                    tempInvoiceStockController.closeDatabase();
                    dataRow.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            tempInvoiceStockController.openReadableDatabase();
                            final Dialog dialogBox = new Dialog(InvoiceGen1Alternate.this);
                            dialogBox.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialogBox.setContentView(R.layout.dialog_product_details_multiplebatch);
                            dialogBox.setCancelable(false);

                            Button done = (Button) dialogBox.findViewById(R.id.button_done);

                            TableLayout tblHeader_batch, tblTest_batch;
                            tblHeader_batch = (TableLayout) dialogBox.findViewById(R.id.tblheader_batch);
                            tblTest_batch = (TableLayout) dialogBox.findViewById(R.id.tblTest_batch);
                            TableRow header = new TableRow(InvoiceGen1Alternate.this);
                            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                            header.setLayoutParams(lp);

                            TextView tvProducCode = new TextView(InvoiceGen1Alternate.this);
                            tvProducCode.setText("Code");
                            tvProducCode.setLayoutParams(lp);
                            tvProducCode.setMinWidth(100);

                            TextView tvProductName = new TextView(InvoiceGen1Alternate.this);
                            tvProductName.setText("Product");
                            tvProductName.setLayoutParams(lp);
                            tvProductName.setMinWidth(200);

                            TextView tvBatch = new TextView(InvoiceGen1Alternate.this);
                            tvBatch.setText("Batch");
                            tvBatch.setLayoutParams(lp);
                            tvBatch.setMinWidth(100);

                            TextView tvEx = new TextView(InvoiceGen1Alternate.this);
                            tvEx.setText("Exp Date");
                            tvEx.setLayoutParams(lp);
                            tvEx.setMinWidth(100);


                            TextView tvStock = new TextView(InvoiceGen1Alternate.this);
                            tvStock.setText("Stock");
                            tvStock.setLayoutParams(lp);
                            tvStock.setMinWidth(80);

                            TextView tvPrice = new TextView(InvoiceGen1Alternate.this);
                            tvPrice.setText("Price");
                            tvPrice.setLayoutParams(lp);
                            tvPrice.setMinWidth(80);

                            TextView tvShelfQuantity = new TextView(InvoiceGen1Alternate.this);
                            tvShelfQuantity.setText("Shelf");
                            tvShelfQuantity.setLayoutParams(lp);
                            tvShelfQuantity.setMinWidth(90);

                            TextView tvRequest = new TextView(InvoiceGen1Alternate.this);
                            tvRequest.setText("Request");
                            tvRequest.setLayoutParams(lp);
                            tvRequest.setMinWidth(100);

                            TextView tvormal = new TextView(InvoiceGen1Alternate.this);
                            tvormal.setText("Order");
                            tvormal.setLayoutParams(lp);
                            tvormal.setMinWidth(90);

                            TextView tvFree = new TextView(InvoiceGen1Alternate.this);
                            tvFree.setText("Free");
                            tvFree.setLayoutParams(lp);
                            tvFree.setMinWidth(100);

                            TextView tvDiscount = new TextView(InvoiceGen1Alternate.this);
                            tvDiscount.setText("Discount");
                            tvDiscount.setLayoutParams(lp);
                            tvDiscount.setMinWidth(100);


                            header.addView(tvProducCode);
                            header.addView(tvProductName);

                            header.addView(tvBatch);
                            header.addView(tvEx);
                            header.addView(tvStock);
                            header.addView(tvPrice);
                            header.addView(tvShelfQuantity);
                            header.addView(tvRequest);
                            header.addView(tvormal);
                            header.addView(tvFree);
                            header.addView(tvDiscount);

                            tblHeader_batch.addView(header, 0);


                            ArrayList<Product> prductList = new ArrayList<>();
                            productController.openReadableDatabase();
                            prductList = productController.getProductsByPricipleAndCategoryandCode(proNumber.getText().toString(), product.getCategory(), product.getPrinciple());
                            productController.closeDatabase();
                            final DiscountStructures discoutStrac = new DiscountStructures(InvoiceGen1Alternate.this);
                            discoutStrac.openReadableDatabase();


                            for (final Product pro : prductList) {


                                TempInvoiceStock stockforBatch = tempInvoiceStockController.getTempData(pro.getCode(), pro.getBatchNumber());

                                TableRow dataRow = new TableRow(InvoiceGen1Alternate.this);
                                TableRow.LayoutParams lpInner = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                                lpInner.weight = 1;
                                dataRow.setLayoutParams(lpInner);

                                final TextView proNumber = new TextView(InvoiceGen1Alternate.this);
                                proNumber.setText(pro.getCode());
                                proNumber.setTextColor(Color.BLACK);
                                proNumber.setMinWidth(100);


                                TextView proName = new TextView(InvoiceGen1Alternate.this);
                                proName.setText(" " + pro.getProDes());
                                proName.setTextColor(Color.BLUE);
                                proName.setSingleLine(false);
                                proName.setMaxLines(3);
                                proName.setMinWidth(200);
                                proName.setWidth(200);
                                proName.setTextSize(12);
                                proName.setLines(3);


                                TextView proBatch = new TextView(InvoiceGen1Alternate.this);
                                proBatch.setText(pro.getBatchNumber());
                                proBatch.setTextColor(Color.BLACK);
                                proName.setTypeface(null, Typeface.BOLD);
                                proBatch.setMinWidth(100);
                                proBatch.setTextSize(12);


                                TextView proExdate = new TextView(InvoiceGen1Alternate.this);
                                proExdate.setText(pro.getExpiryDate().substring(0,10));
                                proExdate.setTextColor(Color.BLACK);
                                proName.setTypeface(null, Typeface.BOLD);
                                proExdate.setMinWidth(100);
                                proExdate.setTextSize(12);


                                TextView proStock = new TextView(InvoiceGen1Alternate.this);
                                proStock.setText("" + pro.getQuantity());
                                proStock.setTextColor(Color.RED);
                                proName.setTypeface(null, Typeface.BOLD);
                                proStock.setMinWidth(80);


                                TextView proPrice = new TextView(InvoiceGen1Alternate.this);
                                proPrice.setText("" + pro.getSellingPrice());
                                proPrice.setTextColor(Color.RED);
                                proPrice.setMinWidth(60);
                                proPrice.setTypeface(null, Typeface.BOLD);


                                final EditText edSQuantityBatch = new EditText(InvoiceGen1Alternate.this);
                                final EditText edNormalBatch = new EditText(InvoiceGen1Alternate.this);
                                final EditText edQuantityBatch = new EditText(InvoiceGen1Alternate.this);
                                final EditText edRequestBatch = new EditText(InvoiceGen1Alternate.this);
                                final EditText edDiscountBatch = new EditText(InvoiceGen1Alternate.this);

                                edSQuantityBatch.setText("" + stockforBatch.getShelfQuantity());
                                edSQuantityBatch.setInputType(InputType.TYPE_CLASS_NUMBER);
                                edSQuantityBatch.setLayoutParams(lpInner);
                                edSQuantityBatch.setBackgroundResource(R.drawable.cell_border);
                                edSQuantityBatch.setSelection(edSQuantityBatch.getText().length());
                                edSQuantityBatch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                    @Override
                                    public void onFocusChange(View v, boolean hasFocus) {

                                        if (edSQuantityBatch.getText().toString().equals("0")) {
                                            edSQuantityBatch.setText("");
                                        } else {

                                        }

                                    }
                                });
                                edSQuantityBatch.setOnKeyListener(new View.OnKeyListener() {
                                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                                        if (keyCode == 66) {
                                            if (edSQuantityBatch.getText().toString().equals("0")) {
                                                edSQuantityBatch.setText("");
                                            }

                                        }
                                        return false;
                                    }
                                });
                                edSQuantityBatch.setMinWidth(80);
                                edSQuantityBatch.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {
                                        TableRow row = (TableRow) edSQuantityBatch.getParent();
                                        TextView tvDynaProNo = (TextView) row.getChildAt(0);
                                        TextView tvDynaBatchNo = (TextView) row.getChildAt(2);

                                        if (!s.toString().isEmpty() || !s.toString().equals("")) {

                                            tempInvoiceStockController.updateShelfQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), s.toString());
                                        }

                                    }
                                });


                                edRequestBatch.setInputType(InputType.TYPE_CLASS_NUMBER);
                                edRequestBatch.setLayoutParams(lpInner);
                                edRequestBatch.setBackgroundResource(R.drawable.cell_border);
                                edRequestBatch.setText("" + stockforBatch.getRequestQuantity());
                                edRequestBatch.setSelection(edRequestBatch.getText().length());
                                edRequestBatch.setOnKeyListener(new View.OnKeyListener() {
                                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                                        if (keyCode == 66) {
                                            if (edRequestBatch.getText().toString().equals("0")) {
                                                edRequestBatch.setText("");
                                            }
                                        }
                                        return false;
                                    }
                                });
                                edRequestBatch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                    @Override
                                    public void onFocusChange(View v, boolean hasFocus) {
                                        if (hasFocus == true) {

                                            if (edRequestBatch.getText().toString().equals("0")) {
                                                edRequestBatch.setText("");
                                            } else {

                                            }

                                        }
                                    }
                                });
                                //check whether entered quantity is valid depend on the approval
                                edRequestBatch.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {


                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                                        try {
                                            TableRow row = (TableRow) edRequestBatch.getParent();
                                            TextView tvDynaProNo = (TextView) row.getChildAt(0);
                                            TextView tvDynaBatchNo = (TextView) row.getChildAt(2);

                                            TextView tvDynaStock = (TextView) row.getChildAt(4);
                                            EditText edDynaNormal = (EditText) row.getChildAt(8);
                                            if (!s.toString().isEmpty() || !s.toString().equals("")) {
                                                tempInvoiceStockController.updateRequestQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), s.toString());

                                                if (iswebApprovalActive == true) {
                                                    edDynaNormal.setText(s.toString());

                                                    if (Double.parseDouble(tvDynaStock.getText().toString()) < Double.parseDouble(edRequestBatch.getText().toString())) {
                                                        Toast toast = Toast.makeText(InvoiceGen1Alternate.this, "Enter valid quantity", Toast.LENGTH_SHORT);
                                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                                        toast.show();

                                                    }

                                                    Log.i(" b ->", edRequestBatch.getParent().toString());
                                                } else {
                                                    if (!s.toString().isEmpty() || !s.toString().equals("")) {
                                                        if (Double.parseDouble(s.toString()) > Double.parseDouble(tvDynaStock.getText().toString())) {
                                                            Toast toast = Toast.makeText(InvoiceGen1Alternate.this, "Enter valid quantity", Toast.LENGTH_SHORT);
                                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                                            toast.show();
                                                            edDynaNormal.setText(tvDynaStock.getText().toString());

                                                            ((EditText) row.getChildAt(9)).setText("0"); //set free quantity to zero
                                                        } else {
                                                            edDynaNormal.setText(edRequestBatch.getText().toString());
                                                        }
                                                        int freeIssuesQty = discoutStrac.getFreeIssues(pro.getCode(), Integer.parseInt(edRequestBatch.getText().toString()));
                                                        edQuantityBatch.setText(String.valueOf(freeIssuesQty));


                                                        int stock = Integer.parseInt(tvDynaStock.getText().toString());
                                                        int request = 0;
                                                        if (!edDynaNormal.getText().toString().equals("")) {
                                                            request = Integer.parseInt(edDynaNormal.getText().toString());
                                                        }
                                                        int free = Integer.parseInt(edQuantityBatch.getText().toString());

                                                        if (iswebApprovalActive == false) {
                                                            if (stock - request >= free) { // check whether entered free quantity is smaller than stock -  requested
                                                                tempInvoiceStockController.updateFreeQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), String.valueOf(free));
                                                                if (Double.parseDouble(s.toString()) > 0) {
                                                                    ((EditText) row.getChildAt(9)).setEnabled(false);
                                                                    tempInvoiceStockController.updateDiscountAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(false));
                                                                } else {
                                                                    ((EditText) row.getChildAt(9)).setEnabled(true);
                                                                    tempInvoiceStockController.updateDiscountAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(true));
                                                                }
                                                            } else {
                                                                Toast freeToast = Toast.makeText(InvoiceGen1Alternate.this, "Not enough quantity", Toast.LENGTH_SHORT);
                                                                freeToast.setGravity(Gravity.CENTER, 0, 0);
                                                                freeToast.show();
                                                                edQuantityBatch.setText("0");
                                                            }
                                                        } else {
                                                            tempInvoiceStockController.updateFreeQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), String.valueOf(free));
                                                            if (Double.parseDouble(s.toString()) > 0) {
                                                                ((EditText) row.getChildAt(9)).setEnabled(false);
                                                                tempInvoiceStockController.updateDiscountAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(false));
                                                            } else {
                                                                ((EditText) row.getChildAt(9)).setEnabled(true);
                                                                tempInvoiceStockController.updateDiscountAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(true));
                                                            }
                                                        }


                                                    }
                                                }
                                            } else {
                                                edDynaNormal.setText("0");
                                            }
                                            isChanged = true;
                                        } catch (Exception e) {

                                        }


                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {

                                    }
                                });
                                edRequestBatch.setMinWidth(80);

                                edQuantityBatch.setInputType(InputType.TYPE_CLASS_NUMBER);
                                edQuantityBatch.setLayoutParams(lpInner);
                                edQuantityBatch.setBackgroundResource(R.drawable.cell_border);
                                edQuantityBatch.setText("" + stockforBatch.getFreeQuantity());
                                edQuantityBatch.setMinWidth(80);
                                edQuantityBatch.setEnabled(false);
                                edQuantityBatch.setSelection(edQuantityBatch.getText().length());
                                edQuantityBatch.setOnKeyListener(new View.OnKeyListener() {
                                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                                        if (keyCode == 66) {
                                            if (edQuantityBatch.getText().toString().equals("0")) {
                                                edQuantityBatch.setText("");
                                            }
                                        }
                                        return false;
                                    }
                                });
                                edQuantityBatch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                    @Override
                                    public void onFocusChange(View v, boolean hasFocus) {
                                        if (hasFocus == true) {

                                            if (edQuantityBatch.getText().toString().equals("0")) {
                                                edQuantityBatch.setText("");
                                            } else {

                                            }

                                        }
                                    }
                                });

                                edQuantityBatch.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                                        //get parent of the edittext which has been changed

                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {
                                        TableRow row = (TableRow) edQuantityBatch.getParent();
                                        TextView tvDynaProNo = (TextView) row.getChildAt(0);
                                        TextView tvDynaBatchNo = (TextView) row.getChildAt(2);
                                        TextView tvDynaStock = (TextView) row.getChildAt(4);
                                        EditText edDynaNormal = (EditText) row.getChildAt(8);
                                        if (!s.toString().isEmpty() || !s.toString().equals("")) { // check whether that entered string is  empty
                                            int stock = Integer.parseInt(tvDynaStock.getText().toString());
                                            int request = 0;
                                            if (!edDynaNormal.getText().toString().equals("")) {
                                                request = Integer.parseInt(edDynaNormal.getText().toString());
                                            }
                                            int free = Integer.parseInt(edQuantityBatch.getText().toString());

                                            if (iswebApprovalActive == false) {
                                                if (stock - request >= free) {

                                                    if(manualFreeEnable==true){
                                                        tempInvoiceStockController.updateFreeSystemQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), String.valueOf(tempInvoiceStockController.getFreeByBatchAndCode(tvDynaProNo.getText().toString(),tvDynaBatchNo.getText().toString())));
                                                        tempInvoiceStockController.updateFreeQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), s.toString());

                                                    }else {
                                                        tempInvoiceStockController.updateFreeQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), s.toString());

                                                    }// check whether entered free quantity is smaller than stock -  requested
                                                    if(s.toString().equals("0")){
                                                        ((EditText) row.getChildAt(9)).setEnabled(false);
                                                        ((EditText) row.getChildAt(10)).setEnabled(true);
                                                        tempInvoiceStockController.updateDiscountAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(true));

                                                    }else {
                                                        ((EditText) row.getChildAt(9)).setEnabled(true);
                                                        ((EditText) row.getChildAt(10)).setEnabled(false);
                                                        tempInvoiceStockController.updateDiscountAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(false));

                                                    }

                                            } else {
                                                    Toast freeToast = Toast.makeText(InvoiceGen1Alternate.this, "Not enough quantity", Toast.LENGTH_SHORT);
                                                    freeToast.setGravity(Gravity.CENTER, 0, 0);
                                                    freeToast.show();
                                                    edQuantityBatch.setText("0");
                                                }
                                            } else {
                                                tempInvoiceStockController.updateFreeQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), s.toString());
                                                if(s.toString().equals("0")){
                                                    ((EditText) row.getChildAt(9)).setEnabled(false);
                                                    ((EditText) row.getChildAt(10)).setEnabled(true);
                                                    tempInvoiceStockController.updateDiscountAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(true));

                                                }else {
                                                    ((EditText) row.getChildAt(9)).setEnabled(true);
                                                    ((EditText) row.getChildAt(10)).setEnabled(false);
                                                    tempInvoiceStockController.updateDiscountAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(false));

                                                }

                                        }


                                        }
                                        isChanged = true;
                                    }
                                });


                                edNormalBatch.setInputType(InputType.TYPE_CLASS_NUMBER);
                                edNormalBatch.setLayoutParams(lpInner);
                                edNormalBatch.setBackgroundResource(R.drawable.cell_border);
                                edNormalBatch.setText("" + stockforBatch.getNormalQuantity());
                                edNormalBatch.setMinWidth(80);
                                edNormalBatch.setSelection(edNormalBatch.getText().length());
                                edNormalBatch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                    @Override
                                    public void onFocusChange(View v, boolean hasFocus) {
                                        if (hasFocus == true) {
                                            if(edNormalBatch.getText().toString().equals("0")){
                                                edNormalBatch.setText("");
                                            }else {

                                            }

                                        }
                                    }
                                });
                                edNormalBatch.setOnKeyListener(new View.OnKeyListener() {
                                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                                        if (keyCode == 66) {
                                            if (edNormalBatch.getText().toString().equals("0")) {
                                                edNormalBatch.setText("");
                                            }
                                        }
                                        return false;
                                    }
                                });
                                edNormalBatch.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                                        TableRow row = (TableRow) edNormalBatch.getParent();
                                        TextView tvDynaProNo = (TextView) row.getChildAt(0);
                                        TextView tvDynaBatchNo = (TextView) row.getChildAt(2);
                                        if (!s.toString().isEmpty() || !s.toString().equals("")) {
                                            tempInvoiceStockController.updateNormalQuantity(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), s.toString());
                                        }
                                        isChanged = true;
                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {

                                    }
                                });


                                edDiscountBatch.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                                edDiscountBatch.setLayoutParams(lpInner);
                                edDiscountBatch.setBackgroundResource(R.drawable.cell_border);
                                edDiscountBatch.setText("" + stockforBatch.getPercentage());
                                edDiscountBatch.setMinWidth(80);
                                edDiscountBatch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                    @Override
                                    public void onFocusChange(View v, boolean hasFocus) {
                                        if (hasFocus == true) {
                                            if(edDiscountBatch.getText().toString().equals("0")){
                                                edDiscountBatch.setText("");
                                            }else {

                                            }

                                        }
                                    }
                                });
                                edDiscountBatch.setSelection(edQuantityBatch.getText().length());
                                if (Boolean.valueOf(stockforBatch.getIsDiscountAllowed()) == true) {
                                    edDiscountBatch.setEnabled(true);
                                } else {
                                    edDiscountBatch.setEnabled(false);
                                }
                                edDiscountBatch.setOnKeyListener(new View.OnKeyListener() {
                                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                                        if (keyCode == 66) {
                                            if (edDiscountBatch.getText().toString().equals("0.0")) {
                                                edDiscountBatch.setText("");
                                            }
                                        }
                                        return false;
                                    }
                                });
                                edDiscountBatch.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {
                                        TableRow row = (TableRow) edDiscountBatch.getParent();
                                        TextView tvDynaProNo = (TextView) row.getChildAt(0);
                                        TextView tvDynaBatchNo = (TextView) row.getChildAt(2);

                                        if (!s.toString().isEmpty() || !s.toString().equals("")) {
                                            if (s.length() < 4) {
                                                if (Double.parseDouble(s.toString()) <= 100) {
                                                    tempInvoiceStockController.updateDicount(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), s.toString());
                                                    if (s.toString().equals("0")) {
                                                        ((EditText) row.getChildAt(9)).setEnabled(true);
                                                        ((EditText) row.getChildAt(10)).setEnabled(false);
                                                        tempInvoiceStockController.updateFreeAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(true));
                                                    } else {
                                                        ((EditText) row.getChildAt(9)).setEnabled(false);
                                                        ((EditText) row.getChildAt(10)).setEnabled(true);
                                                        tempInvoiceStockController.updateFreeAlloed(tvDynaProNo.getText().toString(), tvDynaBatchNo.getText().toString(), Boolean.toString(false));
                                                    }
                                                } else {
                                                    Toast toast1 = Toast.makeText(InvoiceGen1Alternate.this, "Enter valid discount", Toast.LENGTH_LONG);
                                                    toast1.setGravity(Gravity.CENTER, 0, 0);
                                                    toast1.show();
                                                    edDiscountBatch.setText("0.0");
                                                }
                                            } else {
                                                Toast toast1 = Toast.makeText(InvoiceGen1Alternate.this, "Enter valid amount", Toast.LENGTH_LONG);
                                                toast1.setGravity(Gravity.CENTER, 0, 0);
                                                toast1.show();
                                                edDiscountBatch.setText("0.0");
                                            }
                                        }
                                        isChanged = true;
                                    }
                                });


                                dataRow.addView(proNumber, 0);
                                dataRow.addView(proName, 1);
                                dataRow.addView(proBatch, 2);
                                dataRow.addView(proExdate, 3);
                                dataRow.addView(proStock, 4);
                                dataRow.addView(proPrice, 5);
                                dataRow.addView(edSQuantityBatch, 6);
                                dataRow.addView(edRequestBatch, 7);
                                dataRow.addView(edNormalBatch, 8);
                                dataRow.addView(edQuantityBatch, 9);
                                dataRow.addView(edDiscountBatch, 10);

                                tblTest_batch.addView(dataRow);
                            }


                            done.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    edSQuantity.setText(String.valueOf(tempInvoiceStockController.getShelf(product.getCode())));
                                    edRequest.setText(String.valueOf(tempInvoiceStockController.getRequest(product.getCode())));
                                    edNormal.setText(String.valueOf(tempInvoiceStockController.getNormal(product.getCode())));
                                    edQuantity.setText(String.valueOf(tempInvoiceStockController.getFree(product.getCode())));
                                    edDiscount.setText(String.valueOf(tempInvoiceStockController.getDis(product.getCode())));
                                    dialogBox.dismiss();
                                }
                            });


                            dialogBox.show();
                        }
                    });//

                }


            }
        } catch (Exception e) {
            Log.e("loading view error", "task error");
        }
        tempInvoiceStockController.closeDatabase();
    }


    private void refreshViewOnSelection() {
        productController.openReadableDatabase();
        String selected = spPrinciple.getSelectedItem().toString();
        categoryList = productController.getCategoryListForPriciple(selected);
        categoryAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.single_list_item, categoryList);
        spCategory.setAdapter(categoryAdapter);
        categoryAdapter.notifyDataSetChanged();
        String selectedCategory = spCategory.getSelectedItem().toString();
        prductList = productController.getProductsByPricipleAndCategory(selected, selectedCategory);
        sortProductList();
        populateProductTable(prductList);
        productController.closeDatabase();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //new UpdateTempInvoiceStockTask().execute();
        if (parent.getId() == R.id.spPrinciple) {
            Log.i("Called priciple ---->", "1");
            //new UpdateTempInvoiceStockTask().execute();
            // new UpdateTempInvoiceStockTask().execute();
            spPrinciplePopulate();

//            if (isFirstTime){
//
//                spPrinciplePopulate();
//                isFirstTime = false;
//            }else{
//
//                if(isChanged){
//                    Toast.makeText(getApplicationContext(),"Please save Data first",Toast.LENGTH_LONG).show();
//                }else{
//                    spPrinciplePopulate();
//                }
//            }


        } else if (parent.getId() == R.id.spCategory) {

//            if (isFirstTimeCategory){
//
//                spCategoryPopulate();
//                isFirstTimeCategory = false;
//            }else{

//                if(isChanged){
//                    Toast.makeText(getApplicationContext(),"Please save Data first 2",Toast.LENGTH_LONG).show();
//                }else{
//                    spCategoryPopulate();
//                }
            // }
            //new UpdateTempInvoiceStockTask().execute();
            //  spCategoryPopulate();
            new PricipeLoadTask().execute();
        }
    }

    private void spPrinciplePopulate() {
        tblTest.removeAllViews();
        categoryList.clear();
        prductList.clear();
        refreshViewOnSelection();

    }

/*
    private void spCategoryPopulate() {

        tblTest.removeAllViews();
        prductList.clear();
        String selected = spPrinciple.getSelectedItem().toString();
        String selectedCategory = spCategory.getSelectedItem().toString();
        productController.openReadableDatabase();
        prductList = productController.getProductsByPricipleAndCategory(selected, selectedCategory);
        productController.closeDatabase();
        sortProductList();
        populateProductTable(prductList);

    }
*/

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void sortProductList() {

        Collections.sort(prductList, new Comparator<Product>() {
            @Override
            public int compare(Product lhs, Product rhs) {
                return lhs.getCode().compareTo(rhs.getCode());
            }
        });
    }

    @Override
    public void onClick(View v) {


//        if (v.getId() == R.id.btnAddToCart){
//           new UpdateTempInvoiceStockTask().execute();
//        }

        ArrayList<TempInvoiceStock> selectedProductList = new ArrayList<>();
        selectedProductList = temporaryInvoiceController.getProductTempList();
        Log.i("size arr", "" + selectedProductList.size());

        boolean flag = false;
        if (!selectedProductList.isEmpty()) {

            String totalvalSplit[] =totalValue.getText().toString().split(":");

            if(Double.parseDouble(totalvalSplit[1])>10000.00){
                 showDialogSendMessage(InvoiceGen1Alternate.this,2);
            }else {
                Intent invoiceGen2Intent = new Intent("com.Indoscan.channelbridge.INVOICEGEN2ACTIVITY");
                Bundle bundleToView = new Bundle();
                bundleToView.putString("Id", rowId);
                bundleToView.putString("PharmacyId", pharmacyId);
                bundleToView.putString("startTime", startTime);

                bundleToView.putBoolean("ManualFreeEnable", manualFreeEnable);
                if (chequeEnabled) {
                    bundleToView.putString("ChequeNumber", chequeNumber);
                    bundleToView.putString("CollectionDate", collectionDate);
                    bundleToView.putString("ReleaseDate", releaseDate);
                }


                Log.w("invoicegen2", "selectedProductList size : " + selectedProductList.size());

                ArrayList<SelectedProduct> selectedProductsArray = new ArrayList<SelectedProduct>();

                for (TempInvoiceStock stockData : selectedProductList) {
                    SelectedProduct product = new SelectedProduct();

                    product.setRowId(Integer.parseInt(stockData.getRow_ID()));
                    product.setProductId(stockData.getProductId());
                    product.setProductCode(stockData.getProductCode());
                    product.setProductBatch(stockData.getBatchCode());
                    product.setQuantity(stockData.getStock());
                    product.setExpiryDate(stockData.getExpiryDate());
                    product.setTimeStamp(stockData.getTimestamp());

                    product.setRequestedQuantity(Integer.parseInt(stockData.getRequestQuantity()));
                    product.setFree(Integer.parseInt(stockData.getFreeQuantity()));
                    product.setNormal(Integer.parseInt(stockData.getNormalQuantity()));
                    product.setFreeSystem(Integer.parseInt(stockData.getFreeQuantitySystem()));
                    product.setDiscount(stockData.getPercentage());
                    product.setShelfQuantity(Integer.parseInt(stockData.getShelfQuantity()));
//                    Log.w("next Button 090928340283423098", selectedProductData[13]);


//
                    product.setProductDescription(stockData.getProductDes());
                    product.setPrice(Double.parseDouble(stockData.getPrice()));
//

//
                    selectedProductsArray.add(product);
//
                }

                bundleToView.putParcelableArrayList("SelectedProducts", selectedProductsArray);
                bundleToView.putParcelableArrayList("ReturnProducts", returnProductsArray);
                invoiceGen2Intent.putExtras(bundleToView);
                finish();
                startActivity(invoiceGen2Intent);
            }



//            } else {
//                Toast onlyShelfQty = Toast.makeText(InvoiceGen1Alternate.this, "You have not entered shelf quantity for selected products!", Toast.LENGTH_SHORT);
//                onlyShelfQty.setGravity(Gravity.TOP, 100, 100);
//                onlyShelfQty.show();
//            }

        } else {
            mergeList = temporaryInvoiceController.getShelfQuantityTempList();
            if (mergeList.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(InvoiceGen1Alternate.this);
                builder.setTitle("Save");
                builder.setMessage("You just only entered shelf quantity.Are you want to save");
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new ShelfQuantityTask(InvoiceGen1Alternate.this).execute();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //TODO
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
//            Toast selectedProductsEmpty = Toast.makeText(InvoiceGen1Alternate.this, "Please check data that you had entered", Toast.LENGTH_SHORT);
//            selectedProductsEmpty.show();
        }
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        alertCancel = new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Are you sure you want Cancel this Invoice?")
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Intent customerItineraryListIntent = new Intent("com.Indoscan.channelbridge.ITINERARYLIST");
                                tempInvoiceStockController.openWritableDatabase();
                                tempInvoiceStockController.deleteAllRecords();
                                tempInvoiceStockController.closeDatabase();
                                finish();

                                startActivity(customerItineraryListIntent);
                            }
                        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        alertCancel.show();

    }

//    private class UpdateTempInvoiceStockTask extends AsyncTask<Void,Void,Void>{
//
//        ArrayList<TempInvoiceStock> stockArrayList;
//
//        public UpdateTempInvoiceStockTask(){
//
//            stockArrayList = new ArrayList<>();
//        }
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            TempInvoiceStock stock = null;
//            for(int i= 0;i < prductList.size();i++) {
//                TableRow row = (TableRow) tblTest.getChildAt(i);
//                stock = new TempInvoiceStock();
//
//                    for (int j = 0; j < 8; j++) {
//                        switch (j) {
//                            case 0:
//                                TextView tvCode = (TextView) row.getChildAt(j); // get child index on particular row
//                                stock.setProductCode(tvCode.getText().toString());
//                                break;
//                            case 2:
//                                TextView tvBatch = (TextView) row.getChildAt(j); // get child index on particular row
//                                stock.setBatchCode(tvBatch.getText().toString());
//                                break;
//                            case 4:
//                                row.getChildAt(j);
//                                Log.i("k---->", row.getChildAt(j).toString());
//                                EditText shefQuantity = (EditText) row.getChildAt(j);
//                                if( shefQuantity.getText().toString().isEmpty() || shefQuantity.getText().toString().equals("")){
//                                    stock.setShelfQuantity("0");
//                                }else {
//                                    stock.setShelfQuantity(shefQuantity.getText().toString());
//                                }
//                                break;
//                            case 5:
//                                EditText requestQuantity = (EditText) row.getChildAt(j);
//                               if( requestQuantity.getText().toString().isEmpty() || requestQuantity .getText().toString().equals("")){
//                                   stock.setRequestQuantity("0");
//                               }else {
//                                   stock.setRequestQuantity(requestQuantity.getText().toString());
//                               }
//                                break;
//                            case 6:
//                                EditText normalQuantity = (EditText) row.getChildAt(j);
//                                if( normalQuantity.getText().toString().isEmpty() || normalQuantity.getText().toString().equals("")){
//                                    stock.setNormalQuantity("0");
//                                }else {
//                                    stock.setNormalQuantity(normalQuantity.getText().toString());
//                                }
//
//                                break;
//                            case 7:
//                                EditText freeQuantity = (EditText) row.getChildAt(j);
//                                if( freeQuantity.getText().toString().isEmpty() || freeQuantity.getText().toString().equals("")){
//                                    stock.setFreeQuantity("0");
//                                }else {
//                                    stock.setFreeQuantity(freeQuantity.getText().toString());
//                                }
//                                break;
//                        }
//
//
//                        // Log.i("Button index: "+(i+j), buttonText);
//                    }
//                    stockArrayList.add(stock);
//                    stock = null;
//                }
//
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            tempInvoiceStockController.openWritableDatabase();
//            tempInvoiceStockController.updateTempInvoiceStock(stockArrayList);
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            tempInvoiceStockController.closeDatabase();
//            Toast toast =   Toast.makeText(getApplicationContext(),"Added to invoice ",Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 0, 0);
//            toast.show();
//        }
//    }

    private class PricipeLoadTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog = new ProgressDialog(InvoiceGen1Alternate.this);
        String selected = "";
        String selectedCategory = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tblTest.removeAllViews();
            prductList.clear();
            selected = spPrinciple.getSelectedItem().toString();
            selectedCategory = spCategory.getSelectedItem().toString();
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            productController.openReadableDatabase();
            prductList = productController.getProductsByPricipleAndCategory(selected, selectedCategory);
            productController.closeDatabase();
            sortProductList();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            populateProductTable(prductList);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

        }
    }

    protected void updateShelfQuantityDB(ArrayList<SelectedProduct> shelfQuantityList) {
        // TODO Auto-generated method stub
        ShelfQuantity shelfQuantity = new ShelfQuantity(this);
        String timeStamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
                .format(new Date());
//        ArrayList<SelectedProduct> mergeList = tempInvoController.getShelfQuantityTempList();
//        shelfQuantityList.addAll(mergeList);
        shelfQuantity.openWritableDatabase();
        Sequence sequence = new Sequence(this);

        sequence.openReadableDatabase();
        String lastInv = sequence.getLastRowId("invoice");
        sequence.closeDatabase();

        String invNum = String.valueOf(Integer.parseInt(lastInv) + 1);
        invNum = "NOT" + invNum;
        for (SelectedProduct shelfQuantityDetails : shelfQuantityList) {

            // shelfQuantity.insertShelfQuantity(invoiceNo, invoiceDate,
            // customerId, productId, batch, availableStock, timeStamp,
            // isUploaded)
            shelfQuantity.insertShelfQuantity(invNum, timeStamp,
                    pharmacyId, shelfQuantityDetails.getProductCode(),
                    shelfQuantityDetails.getProductBatch(),
                    String.valueOf(shelfQuantityDetails.getShelfQuantity()),
                    timeStamp, "false");


        }
        shelfQuantity.closeDatabase();
    }


    public class ShelfQuantityTask extends AsyncTask<Void, Void, Void> {

        private Context context;
        //        private ProductRepStore productRepStoreController;
//        private TemporaryInvoice temporaryInvoiceController;
//        private ArrayList<Product> repStockList;
        private ProgressDialog dialog;

        public ShelfQuantityTask(Context context) {
            this.context = context;


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = new ProgressDialog(context);
            this.dialog.setMessage("Saving shelf quantities");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            this.dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            updateShelfQuantityDB(mergeList);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            this.dialog.dismiss();
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {


        super.onSaveInstanceState(outState);
        outState.putString("startTime", startTime);
        outState.putString("Id", rowId);
        outState.putString("PharmacyId", pharmacyId);
        outState.putBoolean("ManualFreeEnable", manualFreeEnable);
        outState.putParcelableArrayList("ReturnProducts", returnProductsArray);

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        chequeEnabled = preferences.getBoolean("cbPrefEnableCheckDetails", true);
        if (chequeEnabled) {
            outState.putString("ChequeNumber", chequeNumber);
            outState.putString("CollectionDate", collectionDate);
            outState.putString("ReleaseDate", releaseDate);
        }

    }

    //Himanshu
    public void showDialogSendMessage(Context context, final int status) {

        final Dialog dialogBox = new Dialog(context);
        dialogBox.setTitle("Invoice Approval");
        dialogBox.setContentView(R.layout.dialog_send_approval);
        dialogBox.setCancelable(false);

        String reason = null;

        try {
            CustomersPendingApproval cpa = new CustomersPendingApproval(this);
            cpa.openReadableDatabase();
            cusName = cpa.getPendingCustomerByPharmacyId(pharmacyId);
            cpa.closeDatabase();

        }catch (Exception e){
            Customers cus = new Customers(this);
            cus.openReadableDatabase();
            cusName =  cus.getCustomerByPharmacyId(pharmacyId);
            cus.closeDatabase();
        }


        final TextView txtMessage = (TextView) dialogBox.findViewById(R.id.textView_dialogsendapproval_customername);

        final RelativeLayout layoutResend =(RelativeLayout)dialogBox.findViewById(R.id.layout_dialogsendapproval_resend);
        final TextView txtPhoneNumber = (TextView) dialogBox.findViewById(R.id.textView_dialogsendapproval_phoneNumber);
        final Spinner spinPerson = (Spinner) dialogBox.findViewById(R.id.spinner_approve_person);
        final EditText edtComment = (EditText) dialogBox.findViewById(R.id.editText_dialogsendapproval_comment);
        final EditText edtCode = (EditText) dialogBox.findViewById(R.id.editTextdialogsendapproval_code);
        Button btnContinue = (Button) dialogBox.findViewById(R.id.buttondialogsendapproval_continue);
        final Button btnSende = (Button) dialogBox.findViewById(R.id.buttondialogsendapproval_send);
        Button btnCancel = (Button) dialogBox.findViewById(R.id.buttondialogsendapproval_cancel);


        final Approval_Persons ap = new Approval_Persons(InvoiceGen1Alternate.this);
        ap.openReadableDatabase();

        final Approval_Details aPProDetails = new Approval_Details(InvoiceGen1Alternate.this);
        aPProDetails.openWritableDatabase();

        final Reps rep = new Reps(InvoiceGen1Alternate.this);
        rep.openReadableDatabase();

        final Customers data = new Customers(InvoiceGen1Alternate.this);
        data.openReadableDatabase();

        final ArrayList<String> approvalPersonsList = ap.getAllPerson();
        ArrayAdapter<String> pesronNameAdapter = new ArrayAdapter<String>(InvoiceGen1Alternate.this, android.R.layout.simple_spinner_item, approvalPersonsList);

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
            reason = "Manual free enable allowed";
            txtMessage.setText("Manual free issue not allowed.Do you want to send for approval and proceed ?");
        } else if (status == 2) {
             reason = "maximum credit limit exceeded";
             txtMessage.setText(cusName + ",Maximum credit limit exceeded .Do you want to send for approval and proceed ?");
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
                manualFreeEnable=false;
                freeIssue.setChecked(false);
            }
        });


        final String finalReason = reason;

        btnSende.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String code = nextSessionId();
                Date date = new Date(System.currentTimeMillis());
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(InvoiceGen1Alternate.this);
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

                }else if(edtCode.getText().toString().equals("0000")){
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

                    if (status == 1) {
                        refreshViewOnSelection();
                        manualFreeEnable=true;
                        freeIssue.setEnabled(false);
                    } else if (status == 2) {
                        ArrayList<TempInvoiceStock> selectedProductList = new ArrayList<>();
                        selectedProductList = temporaryInvoiceController.getProductTempList();
                        Intent invoiceGen2Intent = new Intent("com.Indoscan.channelbridge.INVOICEGEN2ACTIVITY");
                        Bundle bundleToView = new Bundle();
                        bundleToView.putString("Id", rowId);
                        bundleToView.putString("PharmacyId", pharmacyId);
                        bundleToView.putString("startTime", startTime);
                        bundleToView.putBoolean("ManualFreeEnable", manualFreeEnable);
                        if (chequeEnabled) {
                            bundleToView.putString("ChequeNumber", chequeNumber);
                            bundleToView.putString("CollectionDate", collectionDate);
                            bundleToView.putString("ReleaseDate", releaseDate);
                        }

                        ArrayList<SelectedProduct> selectedProductsArray = new ArrayList<SelectedProduct>();

                        for (TempInvoiceStock stockData : selectedProductList) {
                            SelectedProduct product = new SelectedProduct();

                            product.setRowId(Integer.parseInt(stockData.getRow_ID()));
                            product.setProductId(stockData.getProductId());
                            product.setProductCode(stockData.getProductCode());
                            product.setProductBatch(stockData.getBatchCode());
                            product.setQuantity(stockData.getStock());
                            product.setExpiryDate(stockData.getExpiryDate());
                            product.setTimeStamp(stockData.getTimestamp());
                            product.setRequestedQuantity(Integer.parseInt(stockData.getRequestQuantity()));
                            product.setFree(Integer.parseInt(stockData.getFreeQuantity()));
                            product.setNormal(Integer.parseInt(stockData.getNormalQuantity()));
                            product.setFreeSystem(Integer.parseInt(stockData.getFreeQuantitySystem()));
                            product.setDiscount(stockData.getPercentage());
                            product.setShelfQuantity(Integer.parseInt(stockData.getShelfQuantity()));
                            product.setPrice(Double.parseDouble(stockData.getPrice()));
                            selectedProductsArray.add(product);
                        }

                        bundleToView.putParcelableArrayList("SelectedProducts", selectedProductsArray);
                        bundleToView.putParcelableArrayList("ReturnProducts", returnProductsArray);
                        invoiceGen2Intent.putExtras(bundleToView);
                        finish();
                        startActivity(invoiceGen2Intent);
                         }



                }
                else if (edtCode.getText().toString().length() == 4) {
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

                        if (status == 1) {
                            refreshViewOnSelection();
                            manualFreeEnable=true;
                        } else if (status == 2) {
                            ArrayList<TempInvoiceStock> selectedProductList = new ArrayList<>();
                            selectedProductList = temporaryInvoiceController.getProductTempList();
                            Intent invoiceGen2Intent = new Intent("com.Indoscan.channelbridge.INVOICEGEN2ACTIVITY");
                            Bundle bundleToView = new Bundle();
                            bundleToView.putString("Id", rowId);
                            bundleToView.putString("PharmacyId", pharmacyId);
                            bundleToView.putBoolean("ManualFreeEnable", manualFreeEnable);
                            bundleToView.putString("startTime", startTime);
                            if (chequeEnabled) {
                                bundleToView.putString("ChequeNumber", chequeNumber);
                                bundleToView.putString("CollectionDate", collectionDate);
                                bundleToView.putString("ReleaseDate", releaseDate);
                            }

                            ArrayList<SelectedProduct> selectedProductsArray = new ArrayList<SelectedProduct>();

                            for (TempInvoiceStock stockData : selectedProductList) {
                                SelectedProduct product = new SelectedProduct();

                                product.setRowId(Integer.parseInt(stockData.getRow_ID()));
                                product.setProductId(stockData.getProductId());
                                product.setProductCode(stockData.getProductCode());
                                product.setProductBatch(stockData.getBatchCode());
                                product.setQuantity(stockData.getStock());
                                product.setExpiryDate(stockData.getExpiryDate());
                                product.setTimeStamp(stockData.getTimestamp());
                                product.setRequestedQuantity(Integer.parseInt(stockData.getRequestQuantity()));
                                product.setFree(Integer.parseInt(stockData.getFreeQuantity()));
                                product.setNormal(Integer.parseInt(stockData.getNormalQuantity()));
                                product.setFreeSystem(Integer.parseInt(stockData.getFreeQuantitySystem()));
                                product.setDiscount(stockData.getPercentage());
                                product.setShelfQuantity(Integer.parseInt(stockData.getShelfQuantity()));
                                product.setPrice(Double.parseDouble(stockData.getPrice()));
                                selectedProductsArray.add(product);
                            }

                            bundleToView.putParcelableArrayList("SelectedProducts", selectedProductsArray);
                            bundleToView.putParcelableArrayList("ReturnProducts", returnProductsArray);
                            invoiceGen2Intent.putExtras(bundleToView);
                            finish();
                            startActivity(invoiceGen2Intent);
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
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(InvoiceGen1Alternate.this);
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
        InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtComment.getWindowToken(), 0);
        // ap.closeDatabase();
        dialogBox.show();
    }
    public void showErrorMessage(String msg) {
        AlertDialog alertDialog = new AlertDialog.Builder(InvoiceGen1Alternate.this).create();
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
        genCode= new BigInteger(20, random).toString(32);
        if(genCode.length()!=4){
            genCode=genCode+0;
        }else {

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
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
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
            approvdetails = new Approval_Details(InvoiceGen1Alternate.this);
            approvdetails.openReadableDatabase();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(InvoiceGen1Alternate.this);
            final String repId = sharedPreferences.getString("RepId", "-1");

            List<String[]> rtnRemarks = approvdetails.getApprovaDetails();

            System.out.println("accesss cout "+rtnRemarks.size());


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
