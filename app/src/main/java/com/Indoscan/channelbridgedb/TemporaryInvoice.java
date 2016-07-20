package com.Indoscan.channelbridgedb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.Indoscan.Entity.Product;
import com.Indoscan.Entity.TempInvoiceStock;
import com.Indoscan.channelbridge.SelectedProduct;

import java.util.ArrayList;

/**
 * Created by Amila on 11/15/15.
 */
public class TemporaryInvoice {


    private static final String KEY_ROW_ID = "row_id";
    private static final String KEY_PRODUCT_ID = "product_id";
    private static final String KEY_PRODUCT_CODE = "product_code";
    private static final String KEY_BATCH_NO = "batch_number";
    // private static final String INVOICE_NO = "invoice_no";
    private static final String KEY_SHELF_QUANTITY = "shelf_quantity";
    private static final String KEY_REQUEST_QUANTITY = "request_quantity";
    private static final String KEY_FREE_QUANTITY = "free_quantity";
    private static final String KEY_NORMAL_QUANTITY = "normal_quantity";
    private static final String KEY_PRO_DES = "pro_des";
    private static final String KEY_SELLING_PRICE = "selling_price";
    private static final String KEY_DISCOUNT = "discount";
    private static final String KEY_STOCK = "stock";
    private static final String KEY_EXPIRY = "expiryDate";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_IS_FREE_ALLOWED = "isFreeAllowed";
    private static final String KEY_IS_DISCOUNT_ALLOWED = "isDiscountAllowed";
    private static final String KEY_FREE_BY_SYSTEM = "free_system_quantity";


    String[] columns = {KEY_ROW_ID, KEY_PRODUCT_ID, KEY_PRODUCT_CODE, KEY_BATCH_NO, KEY_SHELF_QUANTITY, KEY_REQUEST_QUANTITY, KEY_FREE_QUANTITY, KEY_NORMAL_QUANTITY, KEY_PRO_DES, KEY_SELLING_PRICE, KEY_DISCOUNT, KEY_STOCK, KEY_EXPIRY, KEY_TIMESTAMP, KEY_IS_FREE_ALLOWED, KEY_IS_DISCOUNT_ALLOWED,KEY_FREE_BY_SYSTEM};
    private static final String TABLE_NAME = "invoice_temporary";
    private static final String TEMPORARY_INVOICE_CREATE = "CREATE TABLE " + TABLE_NAME
            + " (" + KEY_ROW_ID + " INTEGER, "
            + KEY_PRODUCT_ID + " TEXT  ,"
            + KEY_PRODUCT_CODE + " TEXT  ,"
            + KEY_BATCH_NO + " TEXT ,"
            + KEY_SHELF_QUANTITY + " TEXT  ,"
            + KEY_REQUEST_QUANTITY + " TEXT ,"
            + KEY_FREE_QUANTITY + " TEXT ,"  //
            + KEY_NORMAL_QUANTITY + " TEXT ,"   //" TEXT );";
            + KEY_PRO_DES + " TEXT ,"
            + KEY_SELLING_PRICE + " TEXT ,"
            + KEY_DISCOUNT + " TEXT ,"
            + KEY_STOCK + " TEXT ,"
            + KEY_EXPIRY + " TEXT ,"
            + KEY_TIMESTAMP + " TEXT,"
            + KEY_IS_FREE_ALLOWED + " TEXT,"
            + KEY_IS_DISCOUNT_ALLOWED + " TEXT,"
            + KEY_FREE_BY_SYSTEM + " TEXT" + " );";


    public final Context context;
    public DatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    public TemporaryInvoice(Context c) {
        context = c;
    }

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(TEMPORARY_INVOICE_CREATE);

        Log.i("iii-->", TEMPORARY_INVOICE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

    public TemporaryInvoice openWritableDatabase() throws SQLException {
        databaseHelper = new DatabaseHelper(context);
        database = databaseHelper.getWritableDatabase();
        return this;

    }

    public TemporaryInvoice openReadableDatabase() throws SQLException {
        databaseHelper = new DatabaseHelper(context);
        database = databaseHelper.getReadableDatabase();
        return this;

    }

    public void closeDatabase() throws SQLException {
        databaseHelper.close();
    }


    public void insertTempInvoStock(Product pro) {

        ContentValues cv = new ContentValues();
        int result = 0;
        try {
            cv.put(KEY_ROW_ID, pro.getRowId());
            cv.put(KEY_PRODUCT_CODE, pro.getCode());
            cv.put(KEY_PRODUCT_ID, pro.getId());
            cv.put(KEY_BATCH_NO, pro.getBatchNumber());
            cv.put(KEY_SHELF_QUANTITY, "0");
            cv.put(KEY_REQUEST_QUANTITY, "0");
            cv.put(KEY_FREE_QUANTITY, "0");
            cv.put(KEY_NORMAL_QUANTITY, "0");
            cv.put(KEY_PRO_DES, pro.getProDes());
            cv.put(KEY_STOCK, "" + pro.getQuantity());
            cv.put(KEY_EXPIRY, pro.getExpiryDate());
            cv.put(KEY_TIMESTAMP, pro.getTimeStamp());
            cv.put(KEY_DISCOUNT, "0");
            cv.put(KEY_SELLING_PRICE, Double.toString(pro.getSellingPrice()));
            cv.put(KEY_IS_FREE_ALLOWED, Boolean.toString(true));
            cv.put(KEY_IS_DISCOUNT_ALLOWED, Boolean.toString(true));
            cv.put(KEY_FREE_BY_SYSTEM, "0");

            database.insert(TABLE_NAME, null, cv);
        } catch (SQLException e) {
            Log.e("temp invo error - >", e.toString());
            cv.put(KEY_SELLING_PRICE, pro.getSellingPrice());
        } catch (Exception e) {
            Log.e("temp invo error - >", e.toString());
        }


    }

    public void updateTempInvoiceStock(ArrayList<TempInvoiceStock> stockList) {
        //  String strqu = "UPDATE " +TABLE_NAME+" SET "+KEY_CreditAmount+" = "+creditAmount+" WHERE "+KEY_CustomerNo+" = "+customerNo+" AND "+KEY_InvoiceNo+" = "+invoiceNo+";";
        Log.i("update called", "----------------->");
        ContentValues cv = null;
        try {
            for (TempInvoiceStock stock : stockList) {
                cv = new ContentValues();
                cv.put(KEY_SHELF_QUANTITY, stock.getShelfQuantity());
                cv.put(KEY_REQUEST_QUANTITY, stock.getRequestQuantity());
                cv.put(KEY_FREE_QUANTITY, stock.getFreeQuantity());
                cv.put(KEY_NORMAL_QUANTITY, stock.getNormalQuantity());
                database.update(TABLE_NAME, cv, KEY_PRODUCT_CODE + " = ? AND " + KEY_BATCH_NO + " = ?", new String[]{stock.getProductCode(), stock.getBatchCode()});
                Log.i("updated successfully", stock.getProductCode() + "_" + stock.getBatchCode());
            }
        } catch (SQLException e) {
            Log.e("Temp invoice ---->", "Error updating temp invoice stock");
        }


    }


    public void updateDicount(String productCode, String batchNO, String dicount) {
        //  String strqu = "UPDATE " +TABLE_NAME+" SET "+KEY_CreditAmount+" = "+creditAmount+" WHERE "+KEY_CustomerNo+" = "+customerNo+" AND "+KEY_InvoiceNo+" = "+invoiceNo+";";
        Log.i("update called", "----------------->");
        ContentValues cv = null;
        try {
            openWritableDatabase();
            cv = new ContentValues();
            cv.put(KEY_DISCOUNT, dicount);

            database.update(TABLE_NAME, cv, KEY_PRODUCT_CODE + " = ? AND " + KEY_BATCH_NO + " = ?", new String[]{productCode, batchNO});
            Log.i("updated successfully", productCode + "_" + batchNO);
            closeDatabase();

        } catch (SQLException e) {
            Log.e("Temp invoice ---->", "Error updating temp invoice stock");
        }


    }

    public void deleteAllRecords() {

        try {
            database.execSQL("delete from " + TABLE_NAME);
        } catch (SQLException e) {
            Log.e("Temp invoice ---->", "Error deleting temp invoice stock");
        }
    }


    public TempInvoiceStock getTempData(String prodCode, String batchNo) {

        // ArrayList<String> principleList = new  ArrayList<String>();

        Cursor cursor = database.rawQuery("select * from invoice_temporary where product_code = ?  and batch_number = ?", new String[]{prodCode, batchNo});

        cursor.moveToFirst();
        TempInvoiceStock temp = null;
        while (!cursor.isAfterLast()) {
            temp = new TempInvoiceStock();
            temp.setShelfQuantity(cursor.getString(cursor.getColumnIndex("shelf_quantity")));
            temp.setRequestQuantity(cursor.getString(cursor.getColumnIndex("request_quantity")));
            temp.setFreeQuantity(cursor.getString(cursor.getColumnIndex("free_quantity")));
            temp.setNormalQuantity(cursor.getString(cursor.getColumnIndex("normal_quantity")));
            temp.setStock(Integer.parseInt(cursor.getString(cursor.getColumnIndex("stock"))));
            temp.setPercentage(Double.parseDouble(cursor.getString(cursor.getColumnIndex("discount"))));
            temp.setIsFreeAllowed(cursor.getString(cursor.getColumnIndex("isFreeAllowed")));
            temp.setIsDiscountAllowed(cursor.getString(cursor.getColumnIndex("isDiscountAllowed")));
            // principleList.add(principleName);
            cursor.moveToNext();
        }

        cursor.close();

        return temp;
    }

    public void updateShelfQuantity(String productCode, String batchNO, String quantity) {
        //  String strqu = "UPDATE " +TABLE_NAME+" SET "+KEY_CreditAmount+" = "+creditAmount+" WHERE "+KEY_CustomerNo+" = "+customerNo+" AND "+KEY_InvoiceNo+" = "+invoiceNo+";";
        Log.i("update called", "----------------->");
        ContentValues cv = null;
        try {
            openWritableDatabase();
            cv = new ContentValues();
            cv.put(KEY_SHELF_QUANTITY, quantity);

            database.update(TABLE_NAME, cv, KEY_PRODUCT_CODE + " = ? AND " + KEY_BATCH_NO + " = ?", new String[]{productCode, batchNO});
            Log.i("updated successfully", productCode + "_" + batchNO);
            closeDatabase();

        } catch (SQLException e) {
            Log.e("Temp invoice ---->", "Error updating temp shelf stock");
        }


    }


    public void updateRequestQuantity(String productCode, String batchNO, String quantity) {
        //  String strqu = "UPDATE " +TABLE_NAME+" SET "+KEY_CreditAmount+" = "+creditAmount+" WHERE "+KEY_CustomerNo+" = "+customerNo+" AND "+KEY_InvoiceNo+" = "+invoiceNo+";";
        Log.i("update called", "----------------->");
        ContentValues cv = null;
        try {
            openWritableDatabase();
            cv = new ContentValues();
            cv.put(KEY_REQUEST_QUANTITY, quantity);

            database.update(TABLE_NAME, cv, KEY_PRODUCT_CODE + " = ? AND " + KEY_BATCH_NO + " = ?", new String[]{productCode, batchNO});
            Log.i("updated successfully", productCode + "_" + batchNO);
            closeDatabase();
        } catch (SQLException e) {
            Log.e("Temp invoice ---->", "Error updating temp request stock");
        }


    }


    public void updateFreeQuantity(String productCode, String batchNO, String quantity) {
        //  String strqu = "UPDATE " +TABLE_NAME+" SET "+KEY_CreditAmount+" = "+creditAmount+" WHERE "+KEY_CustomerNo+" = "+customerNo+" AND "+KEY_InvoiceNo+" = "+invoiceNo+";";
        Log.i("update called", "----------------->");
        ContentValues cv = null;
        try {
            openWritableDatabase();
            cv = new ContentValues();
            cv.put(KEY_FREE_QUANTITY, quantity);

            database.update(TABLE_NAME, cv, KEY_PRODUCT_CODE + " = ? AND " + KEY_BATCH_NO + " = ?", new String[]{productCode, batchNO});
            Log.i("updated successfully", productCode + "_" + batchNO);
            closeDatabase();
        } catch (SQLException e) {
            Log.e("Temp invoice ---->", "Error updating temp request stock");
        }


    }

    public void updateFreeSystemQuantity(String productCode, String batchNO, String quantity) {
        //  String strqu = "UPDATE " +TABLE_NAME+" SET "+KEY_CreditAmount+" = "+creditAmount+" WHERE "+KEY_CustomerNo+" = "+customerNo+" AND "+KEY_InvoiceNo+" = "+invoiceNo+";";
        Log.i("update called", "----------------->");
        ContentValues cv = null;
        try {
            openWritableDatabase();
            cv = new ContentValues();
            cv.put(KEY_FREE_BY_SYSTEM, quantity);

            database.update(TABLE_NAME, cv, KEY_PRODUCT_CODE + " = ? AND " + KEY_BATCH_NO + " = ?", new String[]{productCode, batchNO});
            Log.i("updated successfully", productCode + "_" + batchNO);
            closeDatabase();
        } catch (SQLException e) {
            Log.e("Temp invoice ---->", "Error updating temp request stock");
        }


    }


    public void updateNormalQuantity(String productCode, String batchNO, String quantity) {
        //  String strqu = "UPDATE " +TABLE_NAME+" SET "+KEY_CreditAmount+" = "+creditAmount+" WHERE "+KEY_CustomerNo+" = "+customerNo+" AND "+KEY_InvoiceNo+" = "+invoiceNo+";";
        Log.i("update called", "----------------->");
        ContentValues cv = null;
        try {
            openWritableDatabase();
            cv = new ContentValues();
            cv.put(KEY_NORMAL_QUANTITY, quantity);

            database.update(TABLE_NAME, cv, KEY_PRODUCT_CODE + " = ? AND " + KEY_BATCH_NO + " = ?", new String[]{productCode, batchNO});
            Log.i("updated successfully", productCode + "_" + batchNO);
            closeDatabase();
        } catch (SQLException e) {
            Log.e("Temp invoice ---->", "Error updating temp request stock");
        }


    }

    public void updateFreeAlloed(String productCode, String batchNO, String allowed) {
        //  String strqu = "UPDATE " +TABLE_NAME+" SET "+KEY_CreditAmount+" = "+creditAmount+" WHERE "+KEY_CustomerNo+" = "+customerNo+" AND "+KEY_InvoiceNo+" = "+invoiceNo+";";
        Log.i("update called", "----------------->");
        ContentValues cv = null;
        try {
            openWritableDatabase();
            cv = new ContentValues();
            cv.put(KEY_IS_FREE_ALLOWED, allowed);

            database.update(TABLE_NAME, cv, KEY_PRODUCT_CODE + " = ? AND " + KEY_BATCH_NO + " = ?", new String[]{productCode, batchNO});
            Log.i("updated successfully", productCode + "_" + batchNO);
            closeDatabase();
        } catch (SQLException e) {
            Log.e("Temp invoice ---->", "Error updating temp request stock");
        }


    }

    public void updateDiscountAlloed(String productCode, String batchNO, String allowed) {
        //  String strqu = "UPDATE " +TABLE_NAME+" SET "+KEY_CreditAmount+" = "+creditAmount+" WHERE "+KEY_CustomerNo+" = "+customerNo+" AND "+KEY_InvoiceNo+" = "+invoiceNo+";";
        Log.i("update called", "----------------->");
        ContentValues cv = null;
        try {
            openWritableDatabase();
            cv = new ContentValues();
            cv.put(KEY_IS_DISCOUNT_ALLOWED, allowed);

            database.update(TABLE_NAME, cv, KEY_PRODUCT_CODE + " = ? AND " + KEY_BATCH_NO + " = ?", new String[]{productCode, batchNO});
            Log.i("updated successfully", productCode + "_" + batchNO);
            closeDatabase();
        } catch (SQLException e) {
            Log.e("Temp invoice ---->", "Error updating temp request stock");
        }


    }

    public ArrayList<TempInvoiceStock> getProductTempList() {

        ArrayList<TempInvoiceStock> tempList = new ArrayList<>();
        openReadableDatabase();
        Cursor cursor = database.rawQuery("select * from invoice_temporary where normal_quantity > 0", null);
        TempInvoiceStock temp = null;
        try {


            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                temp = new TempInvoiceStock();
                temp.setRow_ID(cursor.getString(cursor.getColumnIndex("row_id")));
                temp.setProductId(cursor.getString(cursor.getColumnIndex("product_id")));
                temp.setProductCode(cursor.getString(cursor.getColumnIndex("product_code")));
                temp.setBatchCode(cursor.getString(cursor.getColumnIndex("batch_number")));
                temp.setShelfQuantity(cursor.getString(cursor.getColumnIndex("shelf_quantity")));
                temp.setRequestQuantity(cursor.getString(cursor.getColumnIndex("request_quantity")));
                temp.setFreeQuantity(cursor.getString(cursor.getColumnIndex("free_quantity")));
                temp.setFreeQuantitySystem(cursor.getString(cursor.getColumnIndex("free_system_quantity")));
                temp.setNormalQuantity(cursor.getString(cursor.getColumnIndex("normal_quantity")));
                temp.setProductDes(cursor.getString(cursor.getColumnIndex("pro_des")));
                temp.setPrice(cursor.getString(cursor.getColumnIndex("selling_price")));
                temp.setPercentage(Double.parseDouble(cursor.getString(cursor.getColumnIndex("discount"))));
                temp.setStock(Integer.parseInt(cursor.getString(cursor.getColumnIndex("stock"))));
                temp.setExpiryDate(cursor.getString(cursor.getColumnIndex("expiryDate")));
                temp.setTimestamp(cursor.getString(cursor.getColumnIndex("timestamp")));

                System.out.println("sjsjsdjsidjsidjsidisisdisdjsjdi : " +cursor.getColumnIndex("free_system_quantity"));

                tempList.add(temp);
                cursor.moveToNext();
            }
        } catch (Exception e) {
            cursor.close();
            closeDatabase();
        }


        return tempList;
    }

    public ArrayList<SelectedProduct> getShelfQuantityTempList() {

        ArrayList<SelectedProduct> tempList = new ArrayList<>();
        openReadableDatabase();
        Cursor cursor = database.rawQuery("select * from invoice_temporary where normal_quantity = 0 AND shelf_quantity > 0 ", null);
        // TempInvoiceStock temp = null;
        try {


            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {


                SelectedProduct product = new SelectedProduct();

                product.setRowId(Integer.parseInt(cursor.getString(0)));
                product.setProductId(cursor.getString(1));
                product.setProductCode(cursor.getString(2));
                product.setProductBatch(cursor.getString(3));
                product.setQuantity(Integer.parseInt(cursor.getString(11)));
                product.setExpiryDate(cursor.getString(12));
                product.setTimeStamp(cursor.getString(13));

                product.setRequestedQuantity(Integer.parseInt(cursor.getString(5)));
                product.setFree(Integer.parseInt(cursor.getString(6)));
                product.setNormal(Integer.parseInt(cursor.getString(7)));

                product.setDiscount(Double.parseDouble(cursor.getString(10)));
                product.setShelfQuantity(Integer.parseInt(cursor.getString(4)));
//                    Log.w("next Button 090928340283423098", selectedProductData[13]);
//
                product.setProductDescription(cursor.getString(8));
                product.setPrice(Double.parseDouble(cursor.getString(9)));
                tempList.add(product);
                cursor.moveToNext();
            }
        } catch (Exception e) {
            cursor.close();
            closeDatabase();
        }


        return tempList;
    }

    //Himanshu
    public int checkMultipaleBatch(String code) {
        openReadableDatabase();
        Cursor cursor = database.rawQuery("select * from invoice_temporary where product_code = ? ", new String[]{code});
        int res = cursor.getCount();
        //  closeDatabase();
        return res;
    }

    public TempInvoiceStock checkMultipaleBatchcount(String code) {
        openReadableDatabase();
        Cursor cursor = database.rawQuery("select * from invoice_temporary where product_code = ? ", new String[]{code});
        TempInvoiceStock temp = null;
        temp = new TempInvoiceStock();
        temp.setBatchCount(cursor.getCount());
        cursor.close();
        return temp;
    }

    public TempInvoiceStock getStockcountMultipaleBatch(String code) {
        openReadableDatabase();
        TempInvoiceStock temp = null;
        Cursor cursor = database.rawQuery("select SUM(CAST(stock as decimal)) from invoice_temporary where product_code = ? ", new String[]{code});
        int res = 0;
        temp = new TempInvoiceStock();
        if (cursor.getCount() == 0) {
            res = 0;
            temp.setStockFull(0);
        } else {

            cursor.moveToFirst();
            temp.setStockFull(cursor.getInt(0));

        }


        cursor.close();


        //  closeDatabase();
        return temp;
    }

    public int getShelf(String code) {
        openReadableDatabase();
        Cursor cursor = database.rawQuery("select shelf_quantity from invoice_temporary where product_code = ? ", new String[]{code});

        int shelfCount = 0;

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            shelfCount = shelfCount + cursor.getInt(cursor.getColumnIndex(KEY_SHELF_QUANTITY));

        }
        closeDatabase();
        return shelfCount;
    }

    public int getRequest(String code) {
        openReadableDatabase();
        Cursor cursor = database.rawQuery("select request_quantity from invoice_temporary where product_code = ? ", new String[]{code});

        int requestCount = 0;

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            requestCount = requestCount + cursor.getInt(cursor.getColumnIndex(KEY_REQUEST_QUANTITY));

        }
        closeDatabase();
        return requestCount;
    }

    public int getNormal(String code) {
        openReadableDatabase();
        Cursor cursor = database.rawQuery("select normal_quantity from invoice_temporary where product_code = ? ", new String[]{code});

        int normalCount = 0;

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            normalCount = normalCount + cursor.getInt(cursor.getColumnIndex(KEY_NORMAL_QUANTITY));

        }
        closeDatabase();
        return normalCount;
    }

    public int getFree(String code) {
        openReadableDatabase();
        Cursor cursor = database.rawQuery("select free_quantity from invoice_temporary where product_code = ? ", new String[]{code});

        int freeCount = 0;

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            freeCount = freeCount + cursor.getInt(cursor.getColumnIndex(KEY_FREE_QUANTITY));

        }
        closeDatabase();
        return freeCount;
    }
    public int getFreeByBatchAndCode(String code,String batch) {
        openReadableDatabase();
        Cursor cursor = database.rawQuery("select free_quantity from invoice_temporary where product_code = ? and batch_number = ?", new String[]{code,batch});

        cursor.moveToFirst();
        int freeCount = cursor.getInt(0);

        closeDatabase();

        return freeCount;






    }
    public int getDis(String code) {
        openReadableDatabase();
        Cursor cursor = database.rawQuery("select discount from invoice_temporary where product_code = ? ", new String[]{code});

        int disCount = 0;

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            disCount = disCount + cursor.getInt(cursor.getColumnIndex(KEY_DISCOUNT));

        }
        closeDatabase();
        return disCount;
    }

    public double getCurrentTotal() {
        openReadableDatabase();
        Double tot = 0.0;
        Cursor cursor = database.rawQuery("SELECT normal_quantity,selling_price  FROM invoice_temporary where  normal_quantity > 0", null);
        //c = database.rawQuery("SELECT SUM(CAST(free_quantity as decimal)) FROM invoice_temporary it inner join products p on it.product_code = p.code  And p.principle = '" + principle + "' ", null);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            tot = tot + ((cursor.getDouble(cursor.getColumnIndex(KEY_NORMAL_QUANTITY)))*(cursor.getDouble(cursor.getColumnIndex(KEY_SELLING_PRICE))));

        }
        return tot;
    }


}
