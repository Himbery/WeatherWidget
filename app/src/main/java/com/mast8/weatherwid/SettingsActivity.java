package com.mast8.weatherwid;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    public final static String WIDGET_PREF = "widget_PREF";
    public final static String WIDGET_TEXT = "widget_text_";
    public final static String WIDGET_TIME = "widget_time_";
    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent result;
    SharedPreferences sp;
    EditText etCity;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);

        result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        setResult(RESULT_CANCELED, result);

        setContentView(R.layout.activity_settings);

    }

    public void onClick(View v){

        int time = ((RadioGroup) findViewById(R.id.rgTime)).getCheckedRadioButtonId();
        long timeRenew = 1800000;
        switch (time) {
            case R.id.radioOne:
                timeRenew = 1800000;
                break;
            case R.id.radioTwo:
                timeRenew = 3600000;
                break;
            case R.id.radioThree:
                timeRenew = 5400000;
                break;
            case R.id.radioFour:
                timeRenew = 7200000;
                break;
            case R.id.radioFive:
                timeRenew = 86400000;
                break;
        }
        sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        etCity = (EditText) findViewById(R.id.city_etext);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(WIDGET_TEXT + widgetID, etCity.getText().toString());
        editor.putLong(WIDGET_TIME + widgetID, timeRenew);
        editor.commit();

        setResult(RESULT_OK, result);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        MainActivity.updateWidget(this, appWidgetManager, widgetID);

        finish();
    }
}

