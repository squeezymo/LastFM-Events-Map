package com.squeezymo.lastfmeventsmap.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonRectangle;
import com.squeezymo.lastfmeventsmap.R;
import com.squeezymo.lastfmeventsmap.prefs.Preferences;
import com.squeezymo.lastfmeventsmap.ui.activities.MainActivity;

import tools.lastfm.LastFmAuthenticator;
import tools.lastfm.LastFmClient;
import tools.Obfuscator;

public class LogInFragment extends Fragment {
    private static final String LOG_TAG = LogInFragment.class.getCanonicalName();

    private ButtonRectangle logInBtn;
    private EditText loginFld;
    private EditText passwordFld;
    private TextView errorFld;

    public static LogInFragment instantiate(Bundle args) {
        LogInFragment fragment = new LogInFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static LogInFragment instantiate() {
        return LogInFragment.instantiate(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        logInBtn = (ButtonRectangle) getView().findViewById(R.id.btn_log_in);
        loginFld = (EditText) getView().findViewById(R.id.field_login);
        passwordFld = (EditText) getView().findViewById(R.id.field_password);
        errorFld = (TextView) getView().findViewById(R.id.field_err_login);

        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = getActivity().getSharedPreferences(Preferences.USER_PREFS, Context.MODE_PRIVATE);
        loginFld.setText(prefs.getString(Preferences.LOGIN_PREF, ""));
        passwordFld.setText(Obfuscator.decode(prefs.getString(Preferences.PASSWORD_PREF, "")));
        errorFld.setVisibility(View.GONE);

        loginFld.setSelection(loginFld.getText().length(), loginFld.getText().length());
    }

    @Override
    public void onPause() {
        super.onPause();

        if (errorFld != null) {
            errorFld.setVisibility(View.GONE);
        }
    }

    private void logIn() {
        if (errorFld != null) {
            errorFld.setVisibility(View.GONE);
        }

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showProgressBar(true);
        }

        LastFmClient.authenticate(
                new LastFmAuthenticator.Builder()
                        .setUsername(loginFld.getText().toString())
                        .setPassword(passwordFld.getText().toString())
                        .setApiKey(getResources().getString(R.string.api_key))
                        .setApiSecret(Obfuscator.decode(getResources().getString(R.string.api_secret_enc)))
                        .build()
        );
    }

    public void setErrorMessage(String errorMessage) {
        if (errorFld != null) {
            errorFld.setText(errorMessage);
            errorFld.setVisibility(View.VISIBLE);
        }
    }

}
