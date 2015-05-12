package com.squeezymo.lastfmeventsmap.model;

import com.google.gson.annotations.SerializedName;

import tools.serialization.MapSerializable;

public class LastFmImage implements MapSerializable {
    public enum Size {
        SMALL("small"),
        MEDIUM("medium"),
        LARGE("large"),
        XLARGE("extralarge"),
        XXLARGE("mega");

        private String attr;
        private Size(String attr) {
            this.attr = attr;
        }

        public String getTxt() {
            return attr;
        }
    }

    @SerializedName("#text") private String url;
    @SerializedName("size") private String size;

    public String getUrl() { return url; }
    public String getSize() { return size; }
}
