package com.squeezymo.lastfmeventsmap.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;

public class WorkspaceFragment extends Fragment {
    private static final String LOG_TAG = WorkspaceFragment.class.getCanonicalName();

    public static WorkspaceFragment instantiate(Bundle args) {
        WorkspaceFragment fragment = new WorkspaceFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static WorkspaceFragment instantiate() {
        return WorkspaceFragment.instantiate(null);
    }
}
