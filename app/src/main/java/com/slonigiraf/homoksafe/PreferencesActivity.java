package com.slonigiraf.homoksafe;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;


public class PreferencesActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private TextView textViewTermsUse;
    private Button btShowMoreDown;
    private Button btContinue;
    private static final String USER_AGREE = "user_agree";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        textViewTermsUse = findViewById(R.id.tvTermsOfUse);
        btShowMoreDown = findViewById(R.id.bt2);
        TextView textViewAgree = findViewById(R.id.tvAgree);
        TextView titleTermsUse = findViewById(R.id.titleTermsUse);
        btContinue = findViewById(R.id.btContinue);
        titleTermsUse.setText(getString(R.string.preferences_termsUse_title));
        btShowMoreDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btShowMoreDown.getText().toString().equalsIgnoreCase(getString(R.string.preferences_show_more))) {
                    Analytics.logFirebaseAnalytics(getApplicationContext(), Analytics.TERMS_AND_POLITICS_SEE_ALL,
                            Analytics.TERMS_AND_POLITICS_SEE_ALL_DESCRIPTION, Analytics.TERMS_AND_POLITICS_ACTION);
                    textViewTermsUse.setMaxLines(Integer.MAX_VALUE);
                    btShowMoreDown.setText(getString(R.string.preferences_show_less));
                } else {
                    textViewTermsUse.setMaxLines(3);
                    btShowMoreDown.setText(getString(R.string.preferences_show_more));
                }
            }
        });
        sharedPreferences = getSharedPreferences(USER_AGREE, Context.MODE_PRIVATE);
        boolean hasAgree = sharedPreferences.getBoolean("hasAgree", false);
        if (!hasAgree) {
            btContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (btContinue.isClickable()) {
                        Analytics.logFirebaseAnalytics(getApplicationContext(), Analytics.TERMS_AND_POLITICS_AGREE,
                                Analytics.TERMS_AND_POLITICS_AGREE_DESCRIPTION, Analytics.TERMS_AND_POLITICS_ACTION);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("hasAgree", true);
                        editor.apply();
                        Intent continueUse = new Intent(PreferencesActivity.this, MainActivity.class);
                        startActivity(continueUse);
                        finish();
                    }
                }
            });
        } else {
            btContinue.setVisibility(View.GONE);
            textViewAgree.setVisibility(View.GONE);
        }

    }
}
