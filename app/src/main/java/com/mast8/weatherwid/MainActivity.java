package com.mast8.weatherwid;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppWidgetProvider {

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            setUpAlarm(context, widgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, i);
        }
    }

    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        SharedPreferences.Editor editor = context.getSharedPreferences(SettingsActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(SettingsActivity.WIDGET_TEXT + widgetID);
        }
        editor.commit();
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

   public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetID){


       SharedPreferences sp  = context.getSharedPreferences(SettingsActivity.WIDGET_PREF, Context.MODE_PRIVATE);
       String city = sp.getString(SettingsActivity.WIDGET_TEXT + widgetID, null);
       if (city == null){
           city = "Moscow";
       }

       String appID = "2d3b8b416f5afe220178c1fbeabae9d7";
       String lang = "ru";
       final RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.activity_main);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        WApi api = retrofit.create(WApi.class);
        Call<WeatherList> call = api.call(city, appID, lang);
        String finalCity = city;
        call.enqueue(new Callback<WeatherList>() {
            @Override
            public void onResponse(Call<WeatherList> call, Response<WeatherList> response) {
                WeatherList weatherList = response.body();
                Main main = weatherList.main;
                ArrayList<Weather> weathers = new ArrayList<Weather>();
                weathers.addAll(weatherList.weather);
                for (Weather data : weatherList.weather) {
                    String img = data.icon;
                    String iconUrl = "http://openweathermap.org/img/wn/" + img + ".png";
                    String desc = data.description;
                    AppWidgetTarget image = new AppWidgetTarget(context, R.id.icon, widgetView, widgetID);
                    Glide.with(context).asBitmap().load(iconUrl).into(image);
                    widgetView.setTextViewText(R.id.description, desc);

                }
                double grad = main.temp;
                widgetView.setTextViewText(R.id.city, finalCity);
                widgetView.setTextViewText(R.id.temp, String.valueOf(grad) + " ℃");

                appWidgetManager.updateAppWidget(widgetID, widgetView);
        }

            @Override
            public void onFailure(Call<WeatherList> call, Throwable t) {
                t.printStackTrace();
            }
        });

       AppWidgetManager awm = AppWidgetManager.getInstance(context);
       ComponentName compName = new ComponentName(context, MainActivity.class);
       int[] widgetIds = awm.getAppWidgetIds(compName);
       for (int widgetId : widgetIds) {
           Intent configIntent = new Intent(context, SettingsActivity.class);
           configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
           configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
           PendingIntent pIntent = PendingIntent.getActivity(context, widgetId, configIntent, 0);
           widgetView.setOnClickPendingIntent(R.id.settings, pIntent);
           awm.updateAppWidget(widgetId, widgetView);
       }
       Intent updateIntent = new Intent(context, MainActivity.class);
       updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
       updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { widgetID });
       PendingIntent pIntent = PendingIntent.getBroadcast(context, widgetID, updateIntent, 0);
       widgetView.setOnClickPendingIntent(R.id.renew, pIntent);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case AppWidgetManager.ACTION_APPWIDGET_UPDATE:
                onUpdate(context, AppWidgetManager.getInstance(context), AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, MainActivity.class)));
                break;
            default:
                super.onReceive(context, intent);
        }
    }
    private void setUpAlarm(Context context, int widgetID) {
        final AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        SharedPreferences sp  = context.getSharedPreferences(SettingsActivity.WIDGET_PREF, Context.MODE_PRIVATE);
        long interval = sp.getLong(SettingsActivity.WIDGET_TIME + widgetID, 0);

        PendingIntent alarmPendingIntent = getRefreshWidgetPendingIntent(context, widgetID);

        alarm.cancel(alarmPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, alarmPendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarm.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, alarmPendingIntent);
        } else {
            alarm.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, alarmPendingIntent);
        }
    }
    private PendingIntent getRefreshWidgetPendingIntent(Context context, int widgetId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        return PendingIntent.getBroadcast(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}