package com.squeezymo.lastfmeventsmap.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class LastFmEventsResponse {

    @SerializedName("event") private List<LastFmEvent> events;
    @SerializedName("@attr") private LastFmEventsResponse.Info info;

    public List<LastFmEvent> getEvents() { return events; }
    public Info getInfo() { return info; }

    public static final class Info {
        @SerializedName("location") private String location;
        @SerializedName("page") private String page;
        @SerializedName("perPage") private String perPage;
        @SerializedName("totalPages") private String totalPages;
        @SerializedName("total") private String total;
        @SerializedName("festivalsonly") private String festivalsOnly;
        @SerializedName("tag") private String tag;

        public String getLocation() {
            return location;
        }
        public int getPage() {
            return TextUtils.isEmpty(page) ? 0 : Integer.parseInt(page);
        }
        public int getPerPage() {
            return TextUtils.isEmpty(perPage) ? 0 : Integer.parseInt(perPage);
        }
        public int getTotalPages() {
            return TextUtils.isEmpty(totalPages) ? 0 : Integer.parseInt(totalPages);
        }
        public int getTotal() {
            return TextUtils.isEmpty(total) ? 0 : Integer.parseInt(total);
        }
        public boolean isFestivalsOnly() {
            return TextUtils.isEmpty(festivalsOnly) ? false : Boolean.parseBoolean(festivalsOnly);
        }
        public String getTag() {
            return tag;
        }
    }

}
