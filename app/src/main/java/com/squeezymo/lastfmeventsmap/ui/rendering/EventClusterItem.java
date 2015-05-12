package com.squeezymo.lastfmeventsmap.ui.rendering;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.squeezymo.lastfmeventsmap.model.LastFmEvent;

public class EventClusterItem implements ClusterItem {
    private LastFmEvent mEvent;

    public EventClusterItem(LastFmEvent event) {
        mEvent = event;
    }

    public LastFmEvent getEvent() {
        return mEvent;
    }

    @Override
    public LatLng getPosition() {
        if (mEvent == null || mEvent.getVenue() == null)
            return null;

        return mEvent.getVenue().getLatLng();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof EventClusterItem))
            return false;

        EventClusterItem castObj = (EventClusterItem) obj;
        if (castObj.getEvent() == null || getEvent() == null)
            return false;

        return getEvent().getId() == castObj.getEvent().getId();
    }
}
