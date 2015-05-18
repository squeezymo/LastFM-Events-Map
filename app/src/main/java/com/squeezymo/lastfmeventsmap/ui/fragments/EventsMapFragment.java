package com.squeezymo.lastfmeventsmap.ui.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.squeezymo.lastfmeventsmap.R;
import com.squeezymo.lastfmeventsmap.db.CouchbaseManager;
import com.squeezymo.lastfmeventsmap.model.LastFmEvent;
import com.squeezymo.lastfmeventsmap.model.LastFmImage;
import com.squeezymo.lastfmeventsmap.model.LastFmVenue;
import com.squeezymo.lastfmeventsmap.prefs.Globals;
import com.squeezymo.lastfmeventsmap.ui.rendering.EventClusterItem;
import com.squeezymo.lastfmeventsmap.ui.rendering.EventClusterRenderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventsMapFragment extends MapFragment {
    private static final String LOG_TAG = EventsMapFragment.class.getCanonicalName();

    private ClusterManager<EventClusterItem> mClusterManager;
    private Map<Long, Marker> mMarkerByEventId;
    private boolean initialized;
    private EventClusterItem mChosenItem;
    private Handler mHandler;

    public static EventsMapFragment instantiate(Bundle args) {
        EventsMapFragment fragment = new EventsMapFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static EventsMapFragment instantiate() {
        return EventsMapFragment.instantiate(null);
    }

    public void initialize(GoogleApiClient googleApiClient) {
        /* Handler setting */
        final Bitmap defaultMarkerImage = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(EventsMapFragment.this.getActivity().getResources(), R.drawable.default_artist_medium),
                LastFmImage.Size.MEDIUM.getWidth(),
                LastFmImage.Size.MEDIUM.getHeight(),
                false
        );
/*
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case Globals.IMAGE_DOWNLOADED:
                        final LastFmEvent event = (LastFmEvent) inputMessage.obj;
                        final Marker marker = mMarkerByEventId.get(event.getId());

                        if (marker != null) {
                            try {
                                Bitmap image = CouchbaseManager.retrieveImage(event, LastFmImage.Size.MEDIUM, null);

                                if (image == null) {
                                    image = defaultMarkerImage;
                                }

                                marker.setIcon(BitmapDescriptorFactory.fromBitmap(image));
                            } catch (CouchbaseLiteException e) {
                                e.printStackTrace();
                            }
                        }

                        break;
                }
            }
        };
*/
        /* Clustering setting */
        mMarkerByEventId = new HashMap<>();

        mClusterManager = new ClusterManager<>(getActivity().getApplicationContext(), getMap());
        mClusterManager.setRenderer(
                new EventClusterRenderer<>(
                    getActivity().getApplicationContext(),
                    getMap(),
                    mClusterManager,
                    defaultMarkerImage
                )
        );
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<EventClusterItem>() {
            @Override
            public boolean onClusterItemClick(EventClusterItem eventItem) {
                if (eventItem.equals(mChosenItem)) {
                    // second click
                    Log.d(LOG_TAG, "SECOND CLICK " + eventItem.getEvent().getTitle());
                }
                mChosenItem = eventItem;
                return false;
            }
        });
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<EventClusterItem>() {
            @Override
            public boolean onClusterClick(Cluster<EventClusterItem> eventCluster) {

                return false;
            }
        });

        getMap().setOnMarkerClickListener(mClusterManager);

        /* Actions on startup setting */
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
            getMap().moveCamera(center);
        }

        initialized = true;
    }

    public void setEvents(List<LastFmEvent> events) {
        if (!isInitialized())
            return;

        mClusterManager.clearItems();

        for (LastFmEvent event : events) {
            mClusterManager.addItem(new EventClusterItem(event));
        }

        mClusterManager.cluster();
    }

    public void recluster() {
        if (!isInitialized())
            return;

        mClusterManager.cluster();
    }

    public boolean isInitialized() {
        return initialized;
    }
}
