package com.squeezymo.lastfmeventsmap.model;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import tools.TimeConverter;
import tools.serialization.MapSerializable;

public final class LastFmEvent implements MapSerializable, Parcelable {
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

    public LastFmEvent() {}

    private LastFmEvent(
            long id,
            String title,
            Artists artists,
            LastFmVenue venue,
            String startDate,
            String htmlDesciption,
            List<LastFmImage> images,
            String attendance,
            String reviews,
            String tag,
            String url,
            String website,
            String cancelled,
            Tags tags) {
        this.id = id;
        this.title = title;
        this.artists = artists;
        this.venue = venue;
        this.startDate = startDate;
        this.htmlDesciption = htmlDesciption;
        this.images = images;
        this.attendance = attendance;
        this.reviews = reviews;
        this.tag = tag;
        this.url = url;
        this.website = website;
        this.cancelled = cancelled;
        this.tags = tags;
    }

    public static final class Artists implements MapSerializable, Parcelable {
        @SerializedName("headliner") private String headliner;
        @SerializedName("artist") private List<String> artists;

        public Artists() {}

        private Artists(String headliner, List<String> artists) {
            this.headliner = headliner;
            this.artists = artists;
        }

        public String getHeadliner() {
            return TextUtils.isEmpty(headliner) ? "" : headliner;
        }

        public List<String> getExtras() {
            return artists == null ? new LinkedList<String>() : artists;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(getHeadliner());
            parcel.writeStringList(getExtras());
        }

        public static final Parcelable.Creator<Artists> CREATOR = new Parcelable.Creator<Artists>() {
            public Artists createFromParcel(Parcel in) {
                String parcelHeadliner;
                List<String> parcelArtists = new ArrayList<>();

                parcelHeadliner = in.readString();
                in.readStringList(parcelArtists);

                return new Artists(parcelHeadliner, parcelArtists);
            }

            public Artists[] newArray(int size) {
                return new Artists[size];
            }
        };

    }

    public static final class Tags implements MapSerializable, Parcelable {
        @SerializedName("tag") private List<String> tags;

        public Tags() {}

        private Tags(List<String> tags) {
            this.tags = tags;
        }

        public List<String> getTags() {
            return tags == null ? new LinkedList<String>() : tags;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeStringList(getTags());
        }

        public static final Parcelable.Creator<Tags> CREATOR = new Parcelable.Creator<Tags>() {
            public Tags createFromParcel(Parcel in) {
                List<String> parcelTags = new ArrayList<>();
                in.readStringList(parcelTags);

                return new Tags(parcelTags);
            }

            public Tags[] newArray(int size) {
                return new Tags[size];
            }
        };
    }

    public long getId() { return id; }
    public String getTitle() { return TextUtils.isEmpty(title) ? "" : title; }
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
    public String getStartDateString() { return TextUtils.isEmpty(startDate) ? "" : startDate; }
    public String getHtmlDesciption() { return TextUtils.isEmpty(htmlDesciption) ? "" : htmlDesciption; }
    public List<LastFmImage> getImages() { return images == null ? new ArrayList<LastFmImage>() : images; }
    public LastFmImage getBestFitImage(LastFmImage.Size size, LastFmImage.Filter fit) {
        List<LastFmImage> images = getImages();

        for (LastFmImage image : images) {
            if (image.getSizeAsObject() == size)
                return image;
        }

        if (images.size() == 0 || fit == LastFmImage.Filter.MATCH_EXACT)
            return null;

        LastFmImage.Size[] sizes = LastFmImage.Size.values();
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i] == size) {
                switch (fit) {
                    case MATCH_CLOSEST_LARGEST:
                        return (i+1 < sizes.length) ? getBestFitImage(sizes[i+1], fit) : null;
                    case MATCH_CLOSEST_SMALLEST:
                        return (i-1 >= 0) ? getBestFitImage(sizes[i-1], fit) : null;
                }
            }
        }

        return null;
    }
    public int getAttendance() {
        return TextUtils.isEmpty(attendance) ? 0 : Integer.parseInt(attendance);
    }
    public int getReviews() {
        return TextUtils.isEmpty(reviews) ? 0 : Integer.parseInt(reviews);
    }
    public String getTag() { return TextUtils.isEmpty(tag) ? "" : tag; }
    public String getUrl() { return TextUtils.isEmpty(url) ? "" : url; }
    public String getWebsite() { return TextUtils.isEmpty(website) ? "" : website; }
    public boolean isCancelled() {
        return TextUtils.isEmpty(cancelled) ? false : Boolean.parseBoolean(cancelled);
    }
    public Tags getTags() {
        return tags;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(getId());
        parcel.writeString(getTitle());
        parcel.writeParcelable(getArtists(), flags);
        parcel.writeParcelable(getVenue(), flags);
        parcel.writeString(getStartDateString());
        parcel.writeString(getHtmlDesciption());
        parcel.writeTypedList(getImages());
        parcel.writeString(Integer.toString(getAttendance()));
        parcel.writeString(Integer.toString(getReviews()));
        parcel.writeString(getTag());
        parcel.writeString(getUrl());
        parcel.writeString(getWebsite());
        parcel.writeString(isCancelled() ? "1" : "0");
        parcel.writeParcelable(getTags(), flags);
    }

    public static final Parcelable.Creator<LastFmEvent> CREATOR = new Parcelable.Creator<LastFmEvent>() {
        public LastFmEvent createFromParcel(Parcel in) {
            long id = in.readLong();
            String title = in.readString();
            Artists artists = in.readParcelable(Artists.class.getClassLoader());
            LastFmVenue venue = in.readParcelable(LastFmVenue.class.getClassLoader());
            String startDate = in.readString();
            String htmlDesciption = in.readString();
            List<LastFmImage> images = new ArrayList<>();
            in.readTypedList(images, LastFmImage.CREATOR);
            String attendance = in.readString();
            String reviews = in.readString();
            String tag = in.readString();
            String url = in.readString();
            String website = in.readString();
            String cancelled = in.readString();
            Tags tags = in.readParcelable(Tags.class.getClassLoader());

            return new LastFmEvent(
                    id,
                    title,
                    artists,
                    venue,
                    startDate,
                    htmlDesciption,
                    images,
                    attendance,
                    reviews,
                    tag,
                    url,
                    website,
                    cancelled,
                    tags
            );
        }

        public LastFmEvent[] newArray(int size) {
            return new LastFmEvent[size];
        }
    };
}