package com.da.Utils;

import com.da.hworld.HLocation;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Daniel on 3/17/2015.
 */
public class QuickSortHLocations {

    double longitude, latitude;
    public QuickSortHLocations(double latitude, double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public ArrayList<HLocation> sort(ArrayList<HLocation> list){
        if(list.size() <= 1)
        return list;

        int ro_placement = list.size()/2;
        HLocation rotation = list.get(ro_placement);
        list.remove(ro_placement);
        ArrayList<HLocation> lower = new ArrayList<HLocation>();
        ArrayList<HLocation> higher = new ArrayList<HLocation>();

        for(HLocation item: list)
        {
            if(distanceMiles(latitude, longitude, item.getLat(), item.getLong()) <= distanceMiles(latitude, longitude, rotation.getLat(), rotation.getLong()))
            {    lower.add(item);}
            else
            {    higher.add(item);}
        }
        sort(lower);
        sort(higher);
        //double holder = distanceMiles(latitude, longitude, list.get(0).getLat(), list.get(0).getLong());

        list.clear();
        list.addAll(lower);
        list.add(rotation);
        list.addAll(higher);
        return list;
    }

    public static double distanceMiles(double lat1, double lng1, double lat2, double lng2){
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
    }

    public static String decimalFormat(double num)
    {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.FLOOR);
        return df.format(num);
    }
}
