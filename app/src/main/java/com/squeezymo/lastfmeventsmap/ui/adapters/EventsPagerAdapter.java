package com.squeezymo.lastfmeventsmap.ui.adapters;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import com.squeezymo.lastfmeventsmap.R;

public class EventsPagerAdapter extends PagerAdapter {
    private Activity mActivity;

    public EventsPagerAdapter(Activity activity) {
        mActivity = activity;
    }

    @Override
    public Object instantiateItem(View collection, int position) {

        int resId = 0;
        switch (position) {
            case 0:
                resId = R.id.page_map;
                break;
            case 1:
                resId = R.id.page_list;
                break;
            default:
                throw new IllegalStateException("No tab defined for position == " + position);
        }
        return mActivity.findViewById(resId);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == ((View) arg1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mActivity.getResources().getString(R.string.tab_map);
            case 1:
                return mActivity.getResources().getString(R.string.tab_list);
            default:
                throw new IllegalStateException("No tab defined for position == " + position);
        }
    }
}
