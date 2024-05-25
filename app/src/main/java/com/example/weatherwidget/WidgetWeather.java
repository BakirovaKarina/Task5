package com.example.weatherwidget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.json.JSONException;
import org.json.JSONObject;

public class WidgetWeather extends AppWidgetProvider {



    public static String city_name = "Казань";
    private static final String SYNC_CLICKED    = "automaticWidgetSyncButtonClick";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weather);





        Log.d("widget_test", "updateAppWidget вызван");


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        Log.d("widget_test", "onUpdate");


        RemoteViews remoteViews;
        ComponentName watchWidget;

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_weather);
        watchWidget = new ComponentName(context, WidgetWeather.class);

        remoteViews.setOnClickPendingIntent(R.id.update_btn, getPendingSelfIntent(context, SYNC_CLICKED));
        appWidgetManager.updateAppWidget(watchWidget, remoteViews);

        WeatherData weatherdata = new WeatherData(context);

        for (int appWidgetId : appWidgetIds) {

            weatherdata.setOnDownloadedWeather(new OnDownloadedWeather() {
                @Override
                public void onDownload(String jsonString) throws JSONException {
                    setWeather(jsonString, context);
                }
            });
            //weatherHelper.getWeatherByGPS();
            weatherdata.getWeather(city_name);
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        super.onReceive(context, intent);

        if (SYNC_CLICKED.equals(intent.getAction())) {
            WeatherData weatherdata = new WeatherData(context);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            RemoteViews remoteViews;
            ComponentName watchWidget;

            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_weather);
            watchWidget = new ComponentName(context, WidgetWeather.class);


            Log.d("widget_test", "Click");
            weatherdata.setOnDownloadedWeather(new OnDownloadedWeather() {
                @Override
                public void onDownload(String jsonString) throws JSONException {
                    setWeather(jsonString, context.getApplicationContext());
                }
            });

            weatherdata.getWeather(city_name);

            appWidgetManager.updateAppWidget(watchWidget, remoteViews);

        }
    }
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

        Log.d("widget_test", "onEnabled");

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }
    public void setWeather(String jsonString, Context context) throws JSONException {



        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_weather);
        ComponentName thisWidget = new ComponentName(context, WidgetWeather.class);


        Log.d("widget_test", "setWeather вызван");

        JSONObject jsonObject = new JSONObject(jsonString);

        String city = jsonObject.getString("name");
        remoteViews.setTextViewText(R.id.widget_city, city);

        Log.d("widget_test", "city " + city);

        int temp_value = (int)Math.round(jsonObject.getJSONObject("main").getDouble("temp"));
        String temp = (temp_value < 0 ? "-" : "") + temp_value + "°C";
        remoteViews.setTextViewText(R.id.widget_temp, temp);

        String like = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
        like = like.substring(0, 1).toUpperCase() + like.substring(1);
        remoteViews.setTextViewText(R.id.widget_like, like);

        String icon = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");

        String iconUrl = "https://openweathermap.org/img/wn/" + icon +"@4x.png";

// Загружаем изображение с помощью Glide
        Glide.with(context)
                .load(iconUrl)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        // Устанавливаем загруженное изображение в виджет
                        remoteViews.setImageViewBitmap(R.id.image_icon, ((BitmapDrawable) resource).getBitmap());
                    }
                });




        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

    public static void updateAllWidgets(final Context context,
                                        final int layoutResourceId,
                                        final Class< ? extends AppWidgetProvider> appWidgetClass)
    {
        Log.d("widget_test", "updateAllWidgets");
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutResourceId);

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        final int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(context, appWidgetClass));

        for (int i = 0; i < appWidgetIds.length; ++i)
        {

            manager.updateAppWidget(appWidgetIds[i], remoteViews);
        }
    }
}
