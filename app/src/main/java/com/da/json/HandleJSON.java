package com.da.json;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.da.Utils.CacheStore;
import com.da.Utils.MyDB;
import com.da.hworld.HLocation;
import com.da.hworld.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Daniel on 3/15/2015.
 */
public class HandleJSON {

    String url;
    JSONArray locs;
    ArrayList<HLocation> locations;
    CacheStore store;
    Context context;
    MyDB myDB;
    // JSON Node names
    private static final String TAG_LOCATION = "locations";
    private static final String TAG_NAME = "name";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_ADDRESS2 = "address2";
    private static final String TAG_CITY = "city";
    private static final String TAG_STATE = "state";
    private static final String TAG_ZIP = "zip_postal_code";
    private static final String TAG_PHONE = "phone";
    private static final String TAG_FAX = "fax";
    private static final String TAG_LAT = "latitude";
    private static final String TAG_LONG = "longitude";
    private static final String TAG_OFFIMAGE = "office_image";


    public volatile boolean parsingComplete = true;
    GoogleMap map;

    public HandleJSON(String url, GoogleMap map, Context context)
    {
        this.url = url;
        this.map = map;
        locations = new ArrayList<HLocation>();
        locs = null;
        this.context = context;
        myDB = new MyDB(context);
        store = CacheStore.getInstance();
        new GetLocations().execute();
    }

    public ArrayList<HLocation> getList()
    {
        return locations;
    }

    private class GetLocations extends AsyncTask<Void,Void,Void>{
        private OnLoadDataListener mListener;

        public void setListener(OnLoadDataListener listener)
        {
            mListener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {

            if(isNetworkAvailable()) {
                // Creating service handler class instance
                ServiceHandler sh = new ServiceHandler();

                // Making a request to url and getting response
                String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);

                        // Getting JSON Array node
                        locs = jsonObj.getJSONArray(TAG_LOCATION);

                        // looping through All Contacts
                        for (int i = 0; i < locs.length(); i++) {
                            HLocation item = new HLocation();
                            JSONObject c = locs.getJSONObject(i);

                            item.setName(c.getString(TAG_NAME));
                            item.setAddress(c.getString(TAG_ADDRESS));
                            item.setAddress2(c.getString(TAG_ADDRESS2));
                            item.setCity(c.getString(TAG_CITY));
                            item.setState(c.getString(TAG_STATE));
                            item.setZip(c.getString(TAG_ZIP));
                            item.setPhone(c.getString(TAG_PHONE));
                            item.setFax(c.getString(TAG_FAX));
                            item.setLat(c.getString(TAG_LAT));
                            item.setLong(c.getString(TAG_LONG));
                            item.setOffImage(c.getString(TAG_OFFIMAGE));
                            myDB.createRecords(item);
                            String key = item.getName().toLowerCase().replaceAll("\\W", "");
                            item.setImage(getImage(key, item.getOffImage()));

                            String static_map = "http://maps.google.com/maps/api/staticmap?center=" + item.getLat()
                                    + "," + item.getLong() + "&zoom=15&size=200x200&sensor=false";
                            String key1 = item.getName() + item.getCity();
                            key1.toLowerCase().replaceAll("\\W", "");
                            item.setStatic_map(getImage(key1, static_map));
                            // adding location to location list
                            locations.add(item);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("ServiceHandler", "Couldn't get any data from the url");
                }

                return null;
            }
            else
            {
                Cursor cursor = myDB.selectRecords();
                if(cursor!=null)
                {
                    if (cursor.moveToFirst()){
                        while(!cursor.isAfterLast()){
                            HLocation item = new HLocation();
                            item.setName(cursor.getString(cursor.getColumnIndex("name")));
                            item.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                            item.setAddress2(cursor.getString(cursor.getColumnIndex("address2")));
                            item.setCity(cursor.getString(cursor.getColumnIndex("city")));
                            item.setFax(cursor.getString(cursor.getColumnIndex("fax")));
                            item.setZip(cursor.getString(cursor.getColumnIndex("zip_postal_code")));
                            item.setOffImage(cursor.getString(cursor.getColumnIndex("office_image")));
                            item.setLat(cursor.getString(cursor.getColumnIndex("latitude")));
                            item.setLong(cursor.getString(cursor.getColumnIndex("longitude")));
                            item.setPhone(cursor.getString(cursor.getColumnIndex("phone")));
                            item.setState(cursor.getString(cursor.getColumnIndex("state")));
                            // do what ever you want here
                            String key = item.getName().toLowerCase().replaceAll("\\W", "");
                            item.setImage(getImage(key, item.getOffImage()));

                            String static_map = "http://maps.google.com/maps/api/staticmap?center=" + item.getLat()
                                    + "," + item.getLong() + "&zoom=15&size=200x200&sensor=false";
                            String key1 = item.getName() + item.getCity();
                            key1.toLowerCase().replaceAll("\\W", "");
                            item.setStatic_map(getImage(key1, static_map));
                            // adding location to location list
                            locations.add(item);

                            cursor.moveToNext();
                        }
                    }
                    cursor.close();
                }
                return null;
            }
        }

        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            for(HLocation loc : locations)
            {
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(loc.getLat(), loc.getLong()))
                        .title(loc.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(199)));

            }

            if(mListener != null){
                mListener.onLoadComplete(locations);
            }

        }

        private Bitmap getImage(String key,String url) {
            Bitmap b = store.getCacheFile(key);
            if (b == null) {
                final HttpClient client = new DefaultHttpClient();
                final HttpGet getRequest = new HttpGet(url);
                try {
                    HttpResponse response = client.execute(getRequest);

                    //check 200 OK for success
                    final int statusCode = response.getStatusLine().getStatusCode();

                    if (statusCode != HttpStatus.SC_OK) {
                        Log.w("ImageDownloader", "Error " + statusCode +
                                " while retrieving bitmap from " + url);
                        return null;
                    }

                    final HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        InputStream inputStream = null;
                        try {
                            // getting contents from the stream
                            inputStream = entity.getContent();

                            // decoding stream data back into image Bitmap that android understands
                            final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            store.saveCacheFile(key, bitmap);
                            return bitmap;
                        } finally {
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            entity.consumeContent();
                        }
                    }
                } catch (Exception e) {
                    // You Could provide a more explicit error message for IOException
                    getRequest.abort();
                    Log.e("ImageDownloader", "Something went wrong while" +
                            " retrieving bitmap from " + url + e.toString());
                }
                return null;
            }
            return b;
        }

    }

}
