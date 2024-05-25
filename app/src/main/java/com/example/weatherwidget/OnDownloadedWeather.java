package com.example.weatherwidget;
import org.json.JSONException;

interface OnDownloadedWeather{
    void onDownload(String result) throws JSONException;
}