package com.squeezymo.lastfmeventsmap.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tools.serialization.MapSerializable;

public class LastFmImage implements MapSerializable, Parcelable {
    public enum Size {
        SMALL("small", 5, 5),
        MEDIUM("medium", 48, 48),
        LARGE("large", 150, 150),
        XLARGE("extralarge", 500, 500),
        XXLARGE("mega", 750, 750);

        private String attr;
        private int width, height;
        private Size(String attr, int width, int height) {
            this.attr = attr;
            this.width = width;
            this.height = height;
        }

        public String getTxt() { return attr; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }

    public enum Filter {
        MATCH_EXACT,
        MATCH_CLOSEST_SMALLEST,
        MATCH_CLOSEST_LARGEST
    }

    @SerializedName("#text") private String url;
    @SerializedName("size") private String size;

    public LastFmImage() {}

    private LastFmImage(String url, String size) {
        this.url = url;
        this.size = size;
    }

    public String getUrl() { return TextUtils.isEmpty(url) ? "" : url; }
    public String getSize() { return TextUtils.isEmpty(size) ? "" : size; }
    public Size getSizeAsObject() {
        for (Size size : Size.values()) {
            if (size.getTxt().equals(getSize()))
                return size;
        }

        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(getUrl());
        parcel.writeString(getSize());
    }

    public static final Parcelable.Creator<LastFmImage> CREATOR = new Parcelable.Creator<LastFmImage>() {
        public LastFmImage createFromParcel(Parcel in) {
            return new LastFmImage(in.readString(), in.readString());
        }

        public LastFmImage[] newArray(int size) {
            return new LastFmImage[size];
        }
    };
}
