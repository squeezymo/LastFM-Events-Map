package com.squeezymo.lastfmeventsmap.ui.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.couchbase.lite.CouchbaseLiteException;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.squeezymo.lastfmeventsmap.R;
import com.squeezymo.lastfmeventsmap.db.CouchbaseManager;
import com.squeezymo.lastfmeventsmap.model.LastFmEvent;
import com.squeezymo.lastfmeventsmap.model.LastFmImage;
import com.squeezymo.lastfmeventsmap.prefs.Global;
import com.squeezymo.lastfmeventsmap.ui.adapters.EventsListAdapter;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.ArrayList;
import java.util.List;

public class EventsListFragment extends Fragment {
    private static final String LOG_TAG = EventsListFragment.class.getCanonicalName();

    private RecyclerView mRecyclerView;
    private EventsListAdapter mViewAdapter;
    private StickyRecyclerHeadersDecoration mHeadersDecor;

    public static EventsListFragment instantiate(Bundle args) {
        EventsListFragment fragment = new EventsListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static EventsListFragment instantiate() {
        return EventsListFragment.instantiate(null);
    }

    public void setEvents(List<LastFmEvent> events) {
        mViewAdapter.setEvents(events);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        mRecyclerView = new RecyclerView(getActivity());
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list_view);
        mRecyclerView.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        mViewAdapter = new EventsListAdapter(getActivity(), new ArrayList<LastFmEvent>());
        mViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mHeadersDecor.invalidateHeaders();
            }
        });

        mHeadersDecor = new StickyRecyclerHeadersDecoration(mViewAdapter);

        mRecyclerView.setAdapter(mViewAdapter);
        mRecyclerView.addItemDecoration(mHeadersDecor);
    }
}
