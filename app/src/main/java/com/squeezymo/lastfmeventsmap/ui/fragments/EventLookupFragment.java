package com.squeezymo.lastfmeventsmap.ui.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.couchbase.lite.CouchbaseLiteException;
import com.squeezymo.lastfmeventsmap.R;
import com.squeezymo.lastfmeventsmap.db.CouchbaseManager;
import com.squeezymo.lastfmeventsmap.model.LastFmEvent;
import com.squeezymo.lastfmeventsmap.model.LastFmImage;
import com.squeezymo.lastfmeventsmap.prefs.Globals;

public class EventLookupFragment extends Fragment {
    private static final String LOG_TAG = EventLookupFragment.class.getCanonicalName();
    public static final String EXTRA_EVENT = EventLookupFragment.class.getSimpleName() + "." + "event";

    private LastFmEvent mEvent;
    private Handler mHandler;

    private ImageView mEventImageView;

    public static EventLookupFragment instantiate(Bundle args) {
        EventLookupFragment fragment = new EventLookupFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static EventLookupFragment instantiate() {
        return EventLookupFragment.instantiate(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case Globals.IMAGE_DOWNLOADED:
                        try {
                            Bitmap bitmap = CouchbaseManager.retrieveImage(mEvent, LastFmImage.Size.LARGE, mHandler);
                        } catch (CouchbaseLiteException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_event_lookup, container, false);

        mEventImageView = (ImageView) view.findViewById(R.id.event_image);

        if (getArguments() != null &&
            getArguments().getParcelable(EXTRA_EVENT) != null &&
            getArguments().getParcelable(EXTRA_EVENT) instanceof LastFmEvent) {

            mEvent = getArguments().getParcelable(EXTRA_EVENT);


        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (mEvent == null)
            return;

        try {
            LastFmImage imageMeta = mEvent.getBestFitImage(LastFmImage.Size.XXLARGE, LastFmImage.Filter.MATCH_CLOSEST_SMALLEST);
            Bitmap bitmap = CouchbaseManager.retrieveImage(mEvent, imageMeta.getSizeAsObject(), mHandler);

            if (bitmap == null) {
                bitmap = Bitmap.createScaledBitmap(
                        BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.default_artist_medium),
                        imageMeta.getSizeAsObject().getWidth(),
                        imageMeta.getSizeAsObject().getHeight(),
                        false
                );
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

    }
}
