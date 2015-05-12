package com.squeezymo.lastfmeventsmap.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.astuetz.PagerSlidingTabStrip;
import com.couchbase.lite.CouchbaseLiteException;
import com.gc.materialdesign.views.ProgressBarIndeterminate;
import com.gc.materialdesign.widgets.SnackBar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.squeezymo.lastfmeventsmap.R;
import com.squeezymo.lastfmeventsmap.db.CouchbaseManager;
import com.squeezymo.lastfmeventsmap.model.LastFmEvent;
import com.squeezymo.lastfmeventsmap.prefs.Global;
import com.squeezymo.lastfmeventsmap.prefs.Preferences;
import com.squeezymo.lastfmeventsmap.ui.adapters.EventsPagerAdapter;
import com.squeezymo.lastfmeventsmap.ui.fragments.EventsListFragment;
import com.squeezymo.lastfmeventsmap.ui.fragments.EventsMapFragment;
import com.squeezymo.lastfmeventsmap.ui.fragments.LogInFragment;
import com.squeezymo.lastfmeventsmap.ui.fragments.WorkspaceFragment;

import java.util.Date;
import java.util.List;

import tools.lastfm.LastFmClient;
import tools.lastfm.LastFmEventFilter;


public class MainActivity extends ActionBarActivity {
    private static final String LOG_TAG = MainActivity.class.getCanonicalName();

    private GoogleApiClient mGoogleApiClient;
    private FragmentManager mFragmentManager;
    private BroadcastReceiver mReceiver;
    private EventsMapFragment mMapFragment;
    private EventsListFragment mListFragment;
    private LastFmEventFilter mEventFilter;
    private ProgressBarIndeterminate mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Progress bar setting */
        mProgressBar = (ProgressBarIndeterminate) findViewById(R.id.progressBar);

        /* Fragments setting */
        mFragmentManager = getFragmentManager();
        mMapFragment = (EventsMapFragment) mFragmentManager.findFragmentById(R.id.page_map);
        mListFragment = (EventsListFragment) mFragmentManager.findFragmentById(R.id.page_list);

        mMapFragment.getMap().setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mMapFragment.recluster();
            }
        });

        /* Tab area setting */
        ViewPager pager = (ViewPager) findViewById(R.id.events_pager);
        EventsPagerAdapter adapter = new EventsPagerAdapter(this);
        pager.setAdapter(adapter);

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);
        tabs.setTextColor(getResources().getColor(R.color.material_drawer_primary_text));

        /* Broadcast receiver setting */
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case Global.LOG_IN_SUCCESS:
                        showProgressBar(false);

                        SharedPreferences prefs = MainActivity.this.getSharedPreferences(Preferences.USER_PREFS, Context.MODE_PRIVATE);
                        String welcomeMessage = MainActivity.this.getResources().getString(R.string.greeting, prefs.getString(Preferences.LOGIN_PREF, "listener"));

                        new SnackBar(MainActivity.this, welcomeMessage, null, null).show();

                        mFragmentManager
                                .beginTransaction()
                                .setCustomAnimations(R.animator.slide_in_top, R.animator.slide_out_bottom)
                                .replace(R.id.mid_pane, WorkspaceFragment.instantiate())
                                .commit();

                        break;
                    case Global.LOG_IN_FAILURE:
                        showProgressBar(false);

                        String errorMessage = "";
                        switch ( intent.getIntExtra(Global.EXTRA_ERR, -1) ) {
                            case 4:
                                errorMessage = MainActivity.this.getResources().getString(R.string.err_invalid_credentials);
                                break;
                            case 10:
                                errorMessage = MainActivity.this.getResources().getString(R.string.err_invalid_api);
                                break;
                            case 11:
                                errorMessage = MainActivity.this.getResources().getString(R.string.err_service_offline);
                                break;
                            case 16:
                                errorMessage = MainActivity.this.getResources().getString(R.string.err_temporary);
                                break;
                            case 26:
                                errorMessage = MainActivity.this.getResources().getString(R.string.err_api_suspended);
                                break;
                            default:
                                errorMessage = MainActivity.this.getResources().getString(R.string.err_unknown);
                        }

                        Fragment fragment = mFragmentManager.findFragmentById(R.id.mid_pane);
                        if ( fragment instanceof LogInFragment ) {
                            LogInFragment logInFragment = (LogInFragment) fragment;
                            logInFragment.setErrorMessage(errorMessage);
                        }
                        else {
                            new SnackBar(MainActivity.this, errorMessage, null, null).show();
                        }

                        break;
                    case Global.EVENTS_UPDATED:
                        onEventsUpdated();
                        showProgressBar(false);
                        break;
                    default:
                }
            }
        };

        /* HTTP(S) client setting */
        LastFmClient.initialize(this);

        /* Google API client setting */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        mMapFragment.initialize(mGoogleApiClient);
                        //updateEvents();
                        onEventsUpdated();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        new SnackBar(
                                MainActivity.this,
                                MainActivity.this.getResources().getString(R.string.err_google_api_failed),
                                null, null
                        ).show();
                    }
                })
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

        /* Couchbase setting */
        CouchbaseManager.createDB(this);

        /* Navigation drawer setting */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withHeader(R.layout.drawer_header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_home).withBadge("99").withIdentifier(1),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_free_play).withIcon(FontAwesome.Icon.faw_gamepad),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_custom).withIcon(FontAwesome.Icon.faw_eye).withBadge("6").withIdentifier(2),
                        new SectionDrawerItem().withName(R.string.drawer_item_settings),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_help).withIcon(FontAwesome.Icon.faw_cog),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_open_source).withIcon(FontAwesome.Icon.faw_question).setEnabled(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_contact).withIcon(FontAwesome.Icon.faw_github).withBadge("12+").withIdentifier(1)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                    }
                })
                .build();

        mFragmentManager.beginTransaction().replace(R.id.mid_pane, LogInFragment.instantiate()).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Global.LOG_IN_SUCCESS);
        intentFilter.addAction(Global.LOG_IN_FAILURE);
        intentFilter.addAction(Global.EVENTS_UPDATED);

        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        showProgressBar(false);

        if (mReceiver != null)
            unregisterReceiver(mReceiver);
    }

    private void updateEvents() {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        mEventFilter = new LastFmEventFilter.Builder()
                .setLatLng(new LatLng(location.getLatitude(), location.getLongitude()))
                .setDistance(40)
                .build();

        showProgressBar(true);
        LastFmClient.retrieveEvents(mEventFilter);
    }

    private void onEventsUpdated() {
        new AsyncTask<Void, Void, List<LastFmEvent>>() {
            @Override
            protected List<LastFmEvent> doInBackground(Void... voids) {
                try {
                    //mMapFragment.getMap().getProjection().getVisibleRegion();
                    Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    mEventFilter = new LastFmEventFilter.Builder()
                            .setLatLng(new LatLng(location.getLatitude(), location.getLongitude()))
                            //.setDistance(50)
                            .build();

                    mEventFilter.setStartDate(new Date(115, 3, 23));
                    mEventFilter.setEndDate(new Date(115, 4, 28));

                    return CouchbaseManager.getAllEvents(mEventFilter);
                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<LastFmEvent> events) {
                setEvents(events);
            }
        }.execute();
    }

    private void setEvents(List<LastFmEvent> events) {
        mMapFragment.setEvents(events);
        mListFragment.setEvents(events);
    }

    public void showProgressBar(boolean show) {
        if (mProgressBar == null)
            return;

        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
