package com.da.hworld;


import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.da.Utils.QuickSortHLocations;


public class DetailsFragment extends Fragment {

    TextView details;
    ImageView map, officeImage;
    String phone;
    double latitude, longitude, latitude1, longitude1;
    boolean mileage;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mileage = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detail_screen, container, false);
        Bundle bundle = getActivity().getIntent().getExtras();

        details = (TextView) view.findViewById(R.id.off_det);
        map = (ImageView) view.findViewById(R.id.s_map);
        officeImage = (ImageView) view.findViewById(R.id.off_img);
        //mileage = false;


        details.setText(bundle.getString("Name") + "\n" +bundle.getString("Address"));
        officeImage.setImageBitmap((Bitmap)bundle.getParcelable("Image"));
        map.setImageBitmap((Bitmap)bundle.getParcelable("StaticMap"));
        phone = bundle.getString("Phone");
        latitude = bundle.getDouble("Lat");
        longitude = bundle.getDouble("Long");

        Button butt = (Button) view.findViewById(R.id.call_bt);
        butt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+ phone));
                startActivity(callIntent);
            }
        });

        Button butt2 = (Button) view.findViewById(R.id.dir_bt);

        butt2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(userLocation()) {
                    String geoUriString = "http://maps.google.com/maps?" +
                            "saddr=" + latitude1 +"," + longitude1 +"&daddr=" + latitude + "," + longitude;
                    Intent mapCall = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUriString));
                    startActivity(mapCall);
                }
                else
                    Toast.makeText(getActivity(), "Cannot resolve User Location", Toast.LENGTH_LONG).show();
            }
        });


        return view;
    }

    public boolean userLocation()
    {
        boolean isGPSEnabled = false;
        boolean isNetworkEnable = false;
        boolean canGetLocation = false;

        Location location = null;
        latitude1 = 300;
        longitude1 = 300;

        LocationManager lm;
        try {
            lm = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
            isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnable = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(!isGPSEnabled && !isNetworkEnable){

            }else{
                canGetLocation = true;
                if(isNetworkEnable){
                    location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(location != null){
                        latitude1 = location.getLatitude();
                        longitude1 = location.getLongitude();
                    }
                }
                if(isGPSEnabled){
                    if(location == null){
                        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(location != null){
                            latitude1 = location.getLatitude();
                            longitude1 = location.getLongitude();
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            return false;
        }
        if(!(longitude1 == 300 || latitude1 == 300))
        {
            Log.i("Lati-Longi", latitude1 +" "+ longitude1);
            if(mileage == false) {
                details.append("\n" + QuickSortHLocations.decimalFormat(QuickSortHLocations.distanceMiles(latitude, longitude, latitude1, longitude1)) + " miles");
                mileage = true;
            }
            return true;
        }
        return false;
    }


}
