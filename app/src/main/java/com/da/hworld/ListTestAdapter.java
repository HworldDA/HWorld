package com.da.hworld;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.da.Utils.QuickSortHLocations;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Daniel on 3/16/2015.
 */
public class ListTestAdapter extends ArrayAdapter<HLocation> {

    private Context context;

    public ListTestAdapter(Context context, ArrayList<HLocation> list) {
        super(context, R.layout.main_list_item, list);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder viewHolder = null;
        HLocation item = (HLocation)getItem(position);
        View viewToUse = null;
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if(convertView == null){
            viewToUse = mInflater.inflate(R.layout.main_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView)viewToUse.findViewById(R.id.office_name);
            viewHolder.address = (TextView)viewToUse.findViewById(R.id.address1);
            viewHolder.miles = (TextView)viewToUse.findViewById(R.id.distance);
            viewToUse.setTag(viewHolder);
        }
        else{
            viewToUse = convertView;
            viewHolder = (ViewHolder) viewToUse.getTag();
        }


        viewHolder.name.setText(item.getName());
        viewHolder.address.setText(item.getAddress() + " " + item.getAddress2());
        Log.i("ListTextAdapter", item.getName());
        String miles = getMiles(item.getLat(), item.getLong());
        viewHolder.miles.setText(miles);

        return viewToUse;
    }

    public String getMiles(double lat1, double long1)
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
            return "";
        }

        if(latitude == 300 || longitude == 300)
            return "";
        else {
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.FLOOR);
            return df.format(QuickSortHLocations.distanceMiles(lat1, long1, latitude, longitude)) + " miles";
        }
    }

    /*public double distanceMiles(double lat1, double lng1, double lat2, double lng2){
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        return dist;
     }*/

    private static class ViewHolder{
        TextView name;
        TextView address;
        TextView miles;
    }
}
