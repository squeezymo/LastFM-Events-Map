package com.squeezymo.lastfmeventsmap.model;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import tools.TimeConverter;
import tools.serialization.MapSerializable;

public final class LastFmEvent implements MapSerializable {
    @SerializedName("id") private long id;
    @SerializedName("title") private String title;
    @SerializedName("artists") private Artists artists;
    @SerializedName("venue") private LastFmVenue venue;
    @SerializedName("startDate") private String startDate;
    @SerializedName("description") private String htmlDesciption;
    @SerializedName("image") private List<LastFmImage> images;
    @SerializedName("attendance") private String attendance;
    @SerializedName("reviews") private String reviews;
    @SerializedName("tag") private String tag;
    @SerializedName("url") private String url;
    @SerializedName("website") private String website;
    @SerializedName("cancelled") private String cancelled;
    @SerializedName("tags") private Tags tags;

    public static final class Artists implements MapSerializable {
        @SerializedName("headliner") private String headliner;
        @SerializedName("artist") private List<String> artists;

        public String getHeadliner() {
            return TextUtils.isEmpty(headliner) ? "" : headliner;
        }

        public List<String> getExtras() {
            return artists == null ? new LinkedList<String>() : artists;
        }
    }

    public static final class Tags implements MapSerializable {
        @SerializedName("tag") private List<String> tags;

        public List<String> getTags() {
            return tags == null ? new LinkedList<String>() : tags;
        }
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public Artists getArtists() { return artists; }
    public LastFmVenue getVenue() { return venue; }
    public Date getStartDate() {
        try {
            return TimeConverter.getDateFromLastFmResponse(startDate);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public long getStartDateStamp() {
        Date date = getStartDate();
        if (date == null)
            return 0;

        return date.getTime();
    }
    public String getHtmlDesciption() { return htmlDesciption; }
    public List<LastFmImage> getImages() { return images; }
    public int getAttendance() {
        return TextUtils.isEmpty(attendance) ? 0 : Integer.parseInt(attendance);
    }
    public int getReviews() {
        return TextUtils.isEmpty(reviews) ? 0 : Integer.parseInt(reviews);
    }
    public String getTag() { return tag; }
    public String getUrl() { return url; }
    public String getWebsite() { return website; }
    public boolean isCancelled() {
        return TextUtils.isEmpty(cancelled) ? false : Boolean.parseBoolean(cancelled);
    }
    public Tags getTags() {
        return tags;
    }
}
