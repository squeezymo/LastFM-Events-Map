package com.squeezymo.lastfmeventsmap.ui.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

import com.squeezymo.lastfmeventsmap.R;
import com.squeezymo.lastfmeventsmap.ui.fragments.EventLookupFragment;

public class EventLookupActivity extends Activity {
    private static final String LOG_TAG = EventLookupActivity.class.getCanonicalName();
    public static final String EXTRA_EVENT = EventLookupActivity.class.getSimpleName() + "." + "event";

    private FragmentManager mFragmentManager;
    private EventLookupFragment mEventFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_lookup);

        /* Fragments setting */
        mFragmentManager = getFragmentManager();
        mEventFragment = (EventLookupFragment) mFragmentManager.findFragmentById(R.id.event_container);
        if (mEventFragment == null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(EventLookupFragment.EXTRA_EVENT, getIntent().getParcelableExtra(EXTRA_EVENT));

            mEventFragment = EventLookupFragment.instantiate(bundle);
            mFragmentManager.beginTransaction().replace(R.id.event_container, mEventFragment).commit();
        }
    }


}
