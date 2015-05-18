package com.squeezymo.lastfmeventsmap.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tools.serialization.MapSerializable;

public final class LastFmVenue implements MapSerializable, Parcelable {
    @SerializedName("id") private long id;
    @SerializedName("name") private String name;
    @SerializedName("location") private Location location;
    @SerializedName("url") private String url;
    @SerializedName("website") private String website;
    @SerializedName("phonenumber") private String phoneNumber;
    @SerializedName("image") private List<LastFmImage> images;

    public LastFmVenue() {}

    private LastFmVenue(long id, String name, Location location, String url, String website, String phoneNumber, List<LastFmImage> images) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.url = url;
        this.website = website;
        this.phoneNumber = phoneNumber;
        this.images = images;
    }

    public static final class Location implements MapSerializable, Parcelable {
        @SerializedName("geo:point") private GeoPoint geoPoint;
        @SerializedName("city") private String city;
        @SerializedName("country") private String country;
        @SerializedName("street") private String street;
        @SerializedName("postalcode") private String postalCode;

        public Location() {}

        private Location(GeoPoint geoPoint, String city, String country, String street, String postalcode) {
            this.geoPoint = geoPoint;
            this.city = city;
            this.country = country;
            this.street = street;
            this.postalCode = postalcode;
        }

        public static final class GeoPoint implements MapSerializable, Parcelable {
            @SerializedName("geo:lat") private double lat;
            @SerializedName("geo:long") private double lng;

            public GeoPoint() {}

            private GeoPoint(double lat, double lng) {
                this.lat = lat;
                this.lng = lng;
            }

            private LatLng getLatLng() {
                return new LatLng(lat, lng);
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel parcel, int flags) {
                parcel.writeDouble(lat);
                parcel.writeDouble(lng);
            }

            public static final Parcelable.Creator<GeoPoint> CREATOR = new Parcelable.Creator<GeoPoint>() {
                public GeoPoint createFromParcel(Parcel in) {
                    return new GeoPoint(in.readDouble(), in.readDouble());
                }

                public GeoPoint[] newArray(int size) {
                    return new GeoPoint[size];
                }
            };
        }

        public String getCity() { return TextUtils.isEmpty(city) ? "" : city; }
        public String getCountry() { return TextUtils.isEmpty(country) ? "" : country; }
        public String getStreet() { return TextUtils.isEmpty(street) ? "" : street; }
        public String getPostalCode() { return TextUtils.isEmpty(postalCode) ? "" : postalCode; }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeParcelable(geoPoint, flags);
            parcel.writeString(getCity());
            parcel.writeString(getCountry());
            parcel.writeString(getStreet());
            parcel.writeString(getPostalCode());
        }

        public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
            public Location createFromParcel(Parcel in) {
                return new Location(
                        (GeoPoint) in.readParcelable(GeoPoint.class.getClassLoader()),
                        in.readString(),
                        in.readString(),
                        in.readString(),
                        in.readString()
                );
            }

            public Location[] newArray(int size) {
                return new Location[size];
            }
        };
    }

    public long getId() { return id; }
    public String getName() { return TextUtils.isEmpty(name) ? "" : name; }
    public String getUrl() { return TextUtils.isEmpty(url) ? "" : url; }
    public String getWebsite() { return TextUtils.isEmpty(website) ? "" : website; }
    public String getPhoneNumber() { return TextUtils.isEmpty(phoneNumber) ? "" : phoneNumber; }
    public List<LastFmImage> getImages() { return images == null ? new ArrayList<LastFmImage>() : images; }

    public LatLng getLatLng() {
        return location == null ? null : location.geoPoint.getLatLng();
    }
    public String getCountry() {
        return location == null ? null : location.country;
    }
    public String getCity() {
        return location == null ? null : location.city;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(id);
        parcel.writeString(getName());
        parcel.writeParcelable(location, flags);
        parcel.writeString(getUrl());
        parcel.writeString(getWebsite());
        parcel.writeString(getPhoneNumber());
        parcel.writeTypedList(getImages());
    }

    public static final Parcelable.Creator<LastFmVenue> CREATOR = new Parcelable.Creator<LastFmVenue>() {
        public LastFmVenue createFromParcel(Parcel in) {
            long id = in.readLong();
            String name = in.readString();
            Location location = in.readParcelable(Location.class.getClassLoader());
            String url = in.readString();
            String website = in.readString();
            String phoneNumber = in.readString();
            List<LastFmImage> images = new ArrayList<>();
            in.readTypedList(images, LastFmImage.CREATOR);


            return new LastFmVenue(
                    id, name, location, url, website, phoneNumber, images
            );
        }

        public LastFmVenue[] newArray(int size) {
            return new LastFmVenue[size];
        }
    };
}
