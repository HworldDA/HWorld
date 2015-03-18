package com.da.hworld;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.da.Utils.CacheStore;
import com.da.Utils.MyDB;
import com.da.Utils.QuickSortHLocations;
import com.da.json.OnLoadDataListener;
import com.da.json.ServiceHandler;

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
public class LocationFragment extends ListFragment implements OnLoadDataListener {
    private ArrayList<HLocation> locations;
    ListTestAdapter mAdapter;
    private CacheStore store;

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        HLocation item = this.locations.get(position);
        Toast.makeText(getActivity(), item.getName(), Toast.LENGTH_LONG).show();

        if(item.getStatic_map() == null)
            Log.i("onListItemClick", "static map is missing ");
        else
            Log.i("onListItemClick", "static map byte count: " + item.getStatic_map().getByteCount());

        Bundle bundle = new Bundle();
        bundle.putString("Name", item.getName());
        bundle.putString("Phone", item.getPhone());
        bundle.putDouble("Lat", item.getLat());
        bundle.putDouble("Long", item.getLong());
        bundle.putString("Address", item.getAddress() + "\n" + item.getAddress2()
                + "\n" + item.getCity() + ", " + item.getState() + "\n"
                + item.getZip());

        bundle.putParcelable("Image", item.getImage());
        bundle.putParcelable("StaticMap", item.getStatic_map());

        Intent intent = new Intent(getActivity(),DetailsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onLoadComplete(ArrayList<HLocation> list) {
        Log.i("onLoadComplete", list.get(0).getName());
        locations = list;
        mAdapter = new ListTestAdapter(getActivity(),locations);
        setListAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        Log.i("onCreate", "in fragment list");
        super.onActivityCreated(savedInstanceState);
        GetLocations locationlist = new GetLocations(getActivity());
        store = CacheStore.getInstance();
        locationlist.setListener(this);
        locationlist.execute();

    }


    private class GetLocations extends AsyncTask<Void,Void,Void> {
        private OnLoadDataListener mListener;
        private static final String URL = "http://www.helloworld.com/helloworld_locations.json";
        JSONArray locs;
        private Context context;
        MyDB myDB;
        ArrayList<HLocation> locations;
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

        public void setListener(OnLoadDataListener listener)
        {
            mListener = listener;
        }

        public GetLocations(Context context)
        {
            this.context = context;
            myDB = new MyDB(context);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Creating service handler class instance
            locations = new ArrayList<HLocation>();

            if(isNetworkAvailable()) {
                ServiceHandler sh = new ServiceHandler();

                // Making a request to url and getting response
                String jsonStr = sh.makeServiceCall(URL, ServiceHandler.GET);

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

        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(mListener != null){
                reOrder();
                mListener.onLoadComplete(locations);
            }

        }

        private void reOrder()
        {
            boolean isGPSEnabled = false;
            boolean isNetworkEnable = false;
            boolean canGetLocation = false;

            Location location = null;
            double latitude = 300;
            double longitude = 300;

            LocationManager lm;
            try {
                lm = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
                isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnable = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if(!isGPSEnabled && !isNetworkEnable){

                }else{
                    canGetLocation = true;
                    if(isNetworkEnable){
                        location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if(location != null){
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                    if(isGPSEnabled){
                        if(location == null){
                            location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if(location != null){
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                return;
            }
            if(!(longitude == 300 || latitude == 300))
            {
                QuickSortHLocations qSortList = new QuickSortHLocations(latitude, longitude);
                ArrayList<HLocation> holder = new ArrayList<HLocation>();
                holder.addAll(locations);
                locations.clear();
                locations.addAll(qSortList.sort(holder));
                holder.clear();

            }
        }

        private Bitmap getImage(String key,String url)
        {
            Bitmap b = store.getCacheFile(key);
            if(b == null) {
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
