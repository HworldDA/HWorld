package com.da.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.da.hworld.HLocation;

/**
 * Created by Daniel on 3/17/2015.
 */
public class MyDB {
    private MyDatabaseHelper dbHelper;

    private SQLiteDatabase database;

    public final static String EMP_TABLE="MyLocations"; // name of table

    public final static String LOC_ADDRESS="address"; // address of locatio
    public final static String LOC_NAME="name";  // name of location
    public final static String LOC_ADDRESS2="address2";  // name of location
    public final static String LOC_CITY="city";  // name of location
    public final static String LOC_STATE="state";  // name of location
    public final static String LOC_ZIP="zip_postal_code";  // name of location
    public final static String LOC_PHONE="phone";  // name of location
    public final static String LOC_IMG="office_image";  // name of location
    public final static String LOC_FAX="fax";  // name of location
    public final static String LOC_LAT="latitude";  // name of location
    public final static String LOC_LONG="longitude";  // name of location
    /**
     *
     * @param context
     */
    public MyDB(Context context){
        dbHelper = new MyDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }


    public long createRecords(HLocation item){
        ContentValues values = new ContentValues();
        values.put(LOC_NAME, item.getName());
        values.put(LOC_ADDRESS, item.getAddress());
        values.put(LOC_ADDRESS2, item.getAddress2());
        values.put(LOC_CITY, item.getCity());
        values.put(LOC_STATE, item.getState());
        values.put(LOC_ZIP, item.getZip());
        values.put(LOC_PHONE, item.getPhone());
        values.put(LOC_FAX, item.getFax());
        values.put(LOC_LAT, Double.toString(item.getLat()));
        values.put(LOC_LONG, Double.toString(item.getLong()));
        values.put(LOC_IMG, item.getOffImage());
        return database.insert(EMP_TABLE, null, values);
    }

    public Cursor selectRecords() {
        String[] cols = new String[] {LOC_NAME, LOC_ADDRESS, LOC_ADDRESS2, LOC_CITY, LOC_STATE, LOC_ZIP,
        LOC_PHONE, LOC_FAX, LOC_LAT, LOC_LONG, LOC_IMG};
        Cursor mCursor = database.query(true, EMP_TABLE,cols,null
                , null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor; // iterate to get each value.
    }
}
