package com.Indoscan.channelbridge;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.Indoscan.channelbridge.R;
import com.Indoscan.channelbridgedb.CustomerProductAvg;
import com.Indoscan.channelbridgedb.Products;

public class InvoiceHistoryActivity extends Activity {

    AutoCompleteTextView txtSearchProducts;
    ImageButton iBtnClearSearch;
    ProgressDialog progDialog;
    private List<String[]> invoicedProductsList;
    private ListView lViewInvoiceHistory;
    private Dialog dialog;
    private String pharmacyId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.Indoscan.channelbridge.R.layout.invoice_history_activity);

        if (getIntent().getExtras() != null) {
            pharmacyId = getIntent().getExtras().getString("PharmacyId");
        }


        lViewInvoiceHistory = (ListView) findViewById(R.id.lvCustomerInvoiceHistory);
        txtSearchProducts = (AutoCompleteTextView) findViewById(R.id.ihSearchProducts);

        iBtnClearSearch = (ImageButton) findViewById(R.id.ihClearSearch);

        setSearchAdapter();

//		checkDataBaseForNewProducts();
//		
//		setInvoiceList();

        new DownloadFilesTask(InvoiceHistoryActivity.this)
                .execute("");


//		if (!invoicedProductsList.isEmpty()) {
//			lViewInvoiceHistory.setAdapter(new InvoiceHistoryListAdapter());
//		}


        lViewInvoiceHistory.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                createAlertDialog(invoicedProductsList.get(arg2));
                dialog.show();
            }
        });


        txtSearchProducts.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

                String searchString = s.toString();
                if (!searchString.isEmpty()) {
                    CustomerProductAvg customerProductAvg = new CustomerProductAvg(InvoiceHistoryActivity.this);
                    customerProductAvg.openReadableDatabase();
                    invoicedProductsList = customerProductAvg.getSearchedProductList(pharmacyId, searchString);
                    customerProductAvg.closeDatabase();
                    lViewInvoiceHistory.setAdapter(new InvoiceHistoryListAdapter());
                } else {

                    setInvoiceList();
                    lViewInvoiceHistory.setAdapter(new InvoiceHistoryListAdapter());

                }

            }


            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }
        });

        iBtnClearSearch.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                txtSearchProducts.setText(null);
            }
        });
    }

    private void createAlertDialog(final String[] selectedItem) {
        dialog = new Dialog(this);
        dialog.setTitle("Alert");
        dialog.setContentView(R.layout.extra_customer_reason_popup);
        TextView tViewMessage = (TextView) dialog.findViewById(R.id.tvMessage);
        final EditText txtNewAverage = (EditText) dialog.findViewById(R.id.etReason);
        Button btnCancel = (Button) dialog.findViewById(R.id.bCancel);
        Button btnSave = (Button) dialog.findViewById(R.id.bSave);

        txtNewAverage.setInputType(InputType.TYPE_CLASS_NUMBER);
        tViewMessage.setText("Set the new average value");

        btnSave.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                try {
                    if (!pharmacyId.contentEquals("")) {
                        int newAvg = Integer.parseInt(txtNewAverage.getText().toString());
                        CustomerProductAvg customerProductAvg = new CustomerProductAvg(InvoiceHistoryActivity.this);
                        customerProductAvg.openWritableDatabase();
                        customerProductAvg.updateCustomerProductAverage(pharmacyId, selectedItem[4], newAvg, 1);
                        customerProductAvg.closeDatabase();
                        setInvoiceList();
                        lViewInvoiceHistory.setAdapter(new InvoiceHistoryListAdapter());

                    } else {
                        Toast.makeText(InvoiceHistoryActivity.this, "Unable to set new average", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Log.e("InvoiceHistoryActivity", e.toString());
                    Toast.makeText(InvoiceHistoryActivity.this, "Invalid Value", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("InvoiceHistoryActivity", e.toString());
                }

            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                dialog.dismiss();

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void setInvoiceList() {
        CustomerProductAvg customerProductAvg = new CustomerProductAvg(InvoiceHistoryActivity.this);
        customerProductAvg.openReadableDatabase();
        invoicedProductsList = customerProductAvg.getProductList(pharmacyId);
        customerProductAvg.closeDatabase();
    }

    private void setSearchAdapter() {
        // TODO Auto-generated method stub
        Products productsObject = new Products(this);
        productsObject.openReadableDatabase();
        String[] productNames = productsObject.getProductNames();
        productsObject.closeDatabase();

        ArrayAdapter<String> productNameListAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_dropdown_item_1line, productNames);
        ((AutoCompleteTextView) txtSearchProducts)
                .setAdapter(productNameListAdapter);

    }

    public static class ViewHolder {
        TextView tViewProductName;
        TextView tViewAverage;
    }

    private class InvoiceHistoryListAdapter extends BaseAdapter {

        public InvoiceHistoryListAdapter() {

        }

        public int getCount() {
            return invoicedProductsList.size();
        }

        public Object getItem(int position) {
            return invoicedProductsList.get(position);
        }

        public long getItemId(int position) {
            return Long.parseLong(invoicedProductsList.get(position)[0]);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.invoice_history_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.tViewProductName = (TextView) convertView.findViewById(R.id.tvProductName);
                viewHolder.tViewAverage = (TextView) convertView.findViewById(R.id.tvInvoiceAverage);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.tViewProductName.setText(invoicedProductsList.get(position)[1]);
            int average = 0;
            try {
                int qty = Integer.parseInt(invoicedProductsList.get(position)[2]);
                int count = Integer.parseInt(invoicedProductsList.get(position)[3]);
                average = Math.round(qty / count);
            } catch (NumberFormatException e) {
                Log.e("InvoiceHistoryActivity", e.toString());
            } catch (Exception e) {
                Log.e("InvoiceHistoryActivity", e.toString());
            }
            viewHolder.tViewAverage.setText(String.valueOf(average));
            return convertView;
        }

    }
    @Override
    public void onPause(){

        super.onPause();
        if(progDialog  != null)
            progDialog.dismiss();
    }
    private class DownloadFilesTask extends AsyncTask<String, Integer, Integer> {

        private final Context context;

        public DownloadFilesTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {

            progDialog = new ProgressDialog(context);
            progDialog.setCancelable(false);
            progDialog.setMessage("Checking the database for new products...");
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setProgress(0);

            progDialog.setMax(100);
            progDialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {
            setProgress(progress[0]);
            switch (progress[0]) {
                case 1:
                    Log.w("Log", "yyyyyyyyyy: ");
                    progDialog.setMessage("Fetching products data...");
                    break;
                case 2:
                    progDialog.setMessage("Fetching products average data...");
                    break;

                case 3:
                    progDialog.setMessage("Checking for new products...");
                    break;
                case 4:
                    progDialog.setMessage("Adding new products to average...");
                    break;

                case 5:
                    progDialog.setMessage("Loading average products...");
                    break;

                default:
                    break;
            }
        }

        protected void onPostExecute(Integer returnCode) {
            progDialog.dismiss();
//			showDialog(returnCode);

            if (!invoicedProductsList.isEmpty()) {
                lViewInvoiceHistory.setAdapter(new InvoiceHistoryListAdapter());
            }

        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub
            int returnValue = 1;

            Looper.prepare();
            publishProgress(1);


            Products products = new Products(InvoiceHistoryActivity.this);
            products.openReadableDatabase();
            List<String[]> allProducts = products.getAllProducts();
            products.closeDatabase();

            publishProgress(2);

            CustomerProductAvg customerProductAvg = new CustomerProductAvg(InvoiceHistoryActivity.this);
            customerProductAvg.openReadableDatabase();
            List<String[]> invoicedProductList = customerProductAvg.getProductList(pharmacyId);
            customerProductAvg.closeDatabase();

            publishProgress(3);

            if (allProducts.size() > invoicedProductList.size()) {

                CustomerProductAvg productAvg = new CustomerProductAvg(InvoiceHistoryActivity.this);
                productAvg.openWritableDatabase();

                for (String[] data : allProducts) {

                    publishProgress(3);

                    boolean flag = true;

                    for (String[] invoicedData : invoicedProductList) {
                        if (invoicedData[4].equals(data[2])) {
                            flag = false;
                            break;
                        }
                    }

                    if (flag) {
                        publishProgress(4);
                        productAvg.insertCustomerProductAvg(pharmacyId, data[2], 0, 1);
                    }
                }

                productAvg.closeDatabase();
            }

            publishProgress(5);
            setInvoiceList();


            return returnValue;
        }


    }


}
