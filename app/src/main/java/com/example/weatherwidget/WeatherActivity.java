package com.example.weatherwidget;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

import com.bumptech.glide.Glide;

public class WeatherActivity extends AppCompatActivity {

    private EditText ed_city;
    private Button btn_def_location;
    private Button btn_find;
    private Button btn_exit;
    private TextView city_name;
    private TextView temp;
    private TextView humidity;
    private TextView wind_speed;
    private ImageView image_icon;
    private final String key = "45a7e2ea57c3e459c61bdd9a81b37719";

    private LocationManager locationManager;
    private LocationListener locationListener;
    private WeatherData weatherData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        ed_city = findViewById(R.id.ed_city);
        btn_find = findViewById(R.id.btn_find);
        btn_def_location = findViewById(R.id.btn_def_location);
        city_name = findViewById(R.id.city_name);
        temp = findViewById(R.id.temp);
        humidity = findViewById(R.id.humidity);
        wind_speed = findViewById(R.id.wind);
        image_icon = findViewById(R.id.image_icon);
        btn_exit = findViewById(R.id.btn_exit);

        weatherData = new WeatherData(this);

        weatherData.setOnDownloadedWeather(new OnDownloadedWeather() {
            @Override
            public void onDownload(String result) throws JSONException {
                try {
                    onDownloadWeather(result);
                } catch (Exception ex) {
                    Toast.makeText(getBaseContext(), "Неправильный ввод", Toast.LENGTH_SHORT).show();
                    Log.d("widget_test", ex.toString());
                }
            }
        });

        checkPermission();

        btn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            //поиск погоды по названию города
            public void onClick(View view) {
                try {
                    clickFind();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        // выход из приложения
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickExit();
            }
        });
        //получение погоды по GPS
        btn_def_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWeatherByGPS();
            }
        });
    }

    private void getWeatherByGPS() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                getWeather(latitude, longitude, key);
                locationManager.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        if (checkPermission()) {
            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getWeatherByGPS();
            }
        }
    }

        //Эти методы отвечают за загрузку данных о погоде из API OpenWeatherMap.
        // Они выполняют запросы к API, получают ответы в формате JSON, и
        // обрабатывают полученные данные для отображения на экране.
    private void clickFind() throws IOException, JSONException {
        if (ed_city.getText().toString().trim().equals("")) {
            Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show();
        } else {
            getWeather(ed_city.getText().toString(), key);
        }
    }

    private void getWeather(double latitude, double longitude, String key) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + key + "&units=metric&lang=ru";
        new DownloadWeatherTask().execute(url);
    }

    private void getWeather(String city, String key) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + key + "&units=metric&lang=ru";
        new DownloadWeatherTask().execute(url);
    }

    @SuppressLint("SetTextI18n")
    private void onDownloadWeather(String result) throws JSONException {
        JSONObject jsonObject = new JSONObject(result);
        String value_city = jsonObject.getString("name");
        city_name.setText(value_city);

        int temp_value = (int) Math.round(jsonObject.getJSONObject("main").getDouble("temp"));
        temp.setText((temp_value < 0 ? "-" : "+") + temp_value + "°С");

        double wind_speed_value = jsonObject.getJSONObject("wind").getDouble("speed");
        wind_speed.setText("Скорость ветра: " + wind_speed_value + "м/с ");

        String humidity_value = Integer.toString(jsonObject.getJSONObject("main").getInt("humidity"));
        humidity.setText("Влажность воздуха: " + humidity_value + "%");

        String icon = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");
        String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@4x.png";

        Glide.with(this)
                .load(iconUrl)
                .into(image_icon);
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
            try {
                onDownloadWeather(result);
            } catch (Exception ex) {
                Toast.makeText(getBaseContext(), "Неправильный ввод", Toast.LENGTH_SHORT).show();
                Log.d("widget_test", ex.toString());
            }
        }
    }

    public void onClickExit() {
        finish(); // Закрыть текущую активность
    }
}
