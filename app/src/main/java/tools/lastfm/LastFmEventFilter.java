package tools.lastfm;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class LastFmEventFilter {
    private LatLng mLatLng;
    private int mDistance;
    private String mTag;
    private boolean mFestivalsOnly;
    private int mLimit;
    private int mPage;
    private Date mStartDate;
    private Date mEndDate;

    public static class Builder {
        private LatLng mLatLng;
        private int mDistance = 0;
        private String mTag;
        private boolean mFestivalsOnly;
        private int mLimit;
        private int mPage;
        private Date mStartDate;
        private Date mEndDate;

        public Builder() {
        }

        public Builder setLatLng(LatLng latLng) {
            this.mLatLng = latLng;
            return this;
        }

        public Builder setDistance(int distance) {
            this.mDistance = distance;
            return this;
        }

        public Builder setTag(String tag) {
            this.mTag = tag;
            return this;
        }

        public Builder setFestivalsOnly(boolean festivalsOnly) {
            this.mFestivalsOnly = festivalsOnly;
            return this;
        }

        public Builder setLimit(int limit) {
            this.mLimit = limit;
            return this;
        }

        public Builder setPage(int page) {
            this.mPage = page;
            return this;
        }

        public Builder setStartDate(Date startDate) {
            this.mStartDate = startDate;
            return this;
        }

        public Builder setEndDate(Date endDate) {
            this.mEndDate = endDate;
            return this;
        }

        public LastFmEventFilter build() {
            if ( mLatLng == null ) throw new IllegalStateException("Location must be set");
            if ( mDistance == 0 ) mDistance = 50;
            if ( mLimit == 0 ) mLimit = 100;
            if ( mPage == 0 ) mPage = 1;
            if ( mStartDate == null ) mStartDate = new Date(0);
            if ( mEndDate == null ) mEndDate = new Date(0);

            return new LastFmEventFilter(this);
        }
    }

    private LastFmEventFilter(Builder builder) {
        this.mLatLng = builder.mLatLng;
        this.mDistance = builder.mDistance;
        if (builder.mTag != null) {
            this.mTag = new String(builder.mTag);
        }
        this.mFestivalsOnly = builder.mFestivalsOnly;
        this.mLimit= builder.mLimit;
        this.mStartDate = builder.mStartDate;
        this.mEndDate = builder.mEndDate;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }

    public int getDistance() {
        return mDistance;
    }

    public String getTag() {
        return mTag;
    }

    public boolean isFestivalsOnly() {
        return mFestivalsOnly;
    }

    public int getLimit() {
        return mLimit;
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public Date getEndDate() {
        return mEndDate;
    }

    /* MUTABLE */
    public void turnPage() {
        mPage++;
    }
    public int getPage() {
        return mPage;
    }

    public void setStartDate(Date startDate) {
        if ( startDate == null ) {
            this.mStartDate = new Date(0);
        }
        else {
            this.mStartDate = startDate;
        }
    }

    public void setEndDate(Date endDate) {
        if ( endDate == null ) {
            this.mEndDate = new Date(0);
        }
        else {
            this.mEndDate = endDate;
        }
    }
}
