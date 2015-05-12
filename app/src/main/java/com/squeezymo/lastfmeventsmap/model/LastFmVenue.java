package com.squeezymo.lastfmeventsmap.model;

import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import tools.serialization.MapSerializable;

public final class LastFmVenue implements MapSerializable {
    @SerializedName("id") private long id;
    @SerializedName("name") private String name;
    @SerializedName("location") private Location location;
    @SerializedName("url") private String url;
    @SerializedName("website") private String website;
    @SerializedName("phonenumber") private String phoneNumber;
    @SerializedName("image") private List<LastFmImage> images;

    public static final class Location implements MapSerializable {
        @SerializedName("geo:point") private GeoPoint geoPoint;
        @SerializedName("city") private String city;
        @SerializedName("country") private String coutry;
        @SerializedName("street") private String street;
        @SerializedName("postalcode") private String postalcode;

        public static final class GeoPoint implements MapSerializable {
            @SerializedName("geo:lat") private double lat;
            @SerializedName("geo:long") private double lng;

            private LatLng getLatLng() {
                return new LatLng(lat, lng);
            }
        }
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getWebsite() { return website; }
    public String getPhoneNumber() { return phoneNumber; }
    public List<LastFmImage> getImages() { return images; }

    public LatLng getLatLng() {
        return location == null ? null : location.geoPoint.getLatLng();
    }
    public String getCountry() {
        return location == null ? null : location.coutry;
    }
    public String getCity() {
        return location == null ? null : location.city;
    }
}
