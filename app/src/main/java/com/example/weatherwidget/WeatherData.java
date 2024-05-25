package com.example.weatherwidget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WeatherData {
    private final String key = "45a7e2ea57c3e459c61bdd9a81b37719";
    OnDownloadedWeather onDownloadedWeather;


    private Context context;

    public WeatherData(Context context) {
        this.context = context;
    }

    public void setOnDownloadedWeather(OnDownloadedWeather onDownloadedWeather){
        this.onDownloadedWeather = onDownloadedWeather;
    }
    //getWeatherByGPS: этот метод запрашивает местоположение устройства и вызывает метод getWeather
    // для получения данных о погоде по координатам GPS.
    //getWeather: этот метод запрашивает данные о погоде по координатам или названию города.
    public void getWeatherByGPS() {
        Log.d("weather_data", "Вызван getWeatherByGPS");

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Получение разрешений на получение местоположения
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.d("weather_data", "latitude: " + latitude);
                Log.d("weather_data", "longitude: " + longitude);
                getWeather(latitude, longitude);
                locationManager.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        });
    }

    public void getWeather(double latitudem, double longitude){
        String url = "https://api.openweathermap.org/data/2.5/weather?lat="+ latitudem+"&lon="+longitude+"&appid=" + key + "&units=metric&lang=ru";
        new DownloadWeatherTask().execute(url);
    }
    public void getWeather(String city){
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + key + "&units=metric&lang=ru";
        new DownloadWeatherTask().execute(url);
    }

    private class DownloadWeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String surl = params[0];
            URL url;
            StringBuilder builder = new StringBuilder();

            try {
                url = new URL(surl);
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                    String str;
                    while ((str = bufferedReader.readLine()) != null) {
                        builder.append(str);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return builder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            try{
                onDownloadedWeather.onDownload(result);

            } catch (Exception ex){
                ex.printStackTrace();
            }
        }


    }

}

