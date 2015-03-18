package com.da.hworld;

import android.graphics.Bitmap;

/**
 * Created by Daniel on 3/15/2015.
 */
public class HLocation {
    String name;
    String address;
    String address2;
    String city;
    String state;
    String zip_postal_code;
    String phone;
    String fax;
    String latitude;
    String longitude;
    String office_image;
    Bitmap image;
    Bitmap static_map;

    public HLocation()
    {}

    public String getName() {return name;}
    public String getAddress() {return address;}
    public String getAddress2() {return address2;}
    public String getCity() {return city;}
    public String getState() {return state;}
    public String getZip() {return zip_postal_code;}
    public String getPhone() {return phone;}
    public String getFax() {return fax;}
    public double getLat() {return Double.parseDouble(latitude);}
    public double getLong() {return Double.parseDouble(longitude);}
    public String getOffImage() {return office_image;}
    public Bitmap getImage(){return image;}
    public Bitmap getStatic_map(){return static_map;}

    public void setName(String name){
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setZip(String zip_postal_code) {
        this.zip_postal_code = zip_postal_code;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public void setLat(String latitude) {
        this.latitude = latitude;
    }
    public void setLong(String longitude) {
        this.longitude = longitude;
    }

    public void setOffImage(String office_image) {
        this.office_image = office_image;
    }

    public void setImage(Bitmap image){this.image = image;}

    public void setStatic_map(Bitmap static_map){this.static_map = static_map;}

}
