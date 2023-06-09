package com.example.weather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.*;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {
    LocationManager locationManager;

    public String apiKey;
    private EditText inputText;
    double latitude;
    double longitude;
    final String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    final int all = 1;
    private TextView placeName, gpsOutTextView;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            requestLocation();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        placeName = findViewById(R.id.city_name);
        gpsOutTextView = findViewById(R.id.current_weather);

        Properties properties = new Properties();

        try {

            InputStream inputStream = getAssets().open("config.properties");
            properties.load(inputStream);
            apiKey = properties.getProperty("API_KEY", "");
            inputStream.close();
        } catch (IOException e) {
            Toast.makeText(this, String.valueOf(e), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        if (!isNetworkConnected()) {
            placeName.setText("Connect to Internet");
        } else {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(PERMISSIONS, all);
            } else {
                requestLocation();


            }
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();

    }


    private void requestLocation() {


        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 1000, new LocationListener() {

                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        Log.d("mylog", "Location" + location.getLongitude() + ", " + location.getLatitude());

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        getTemperature(latitude, longitude);


                    }
                });
            }
        } else {
                gpsOutTextView.setText("Turn on the Location");

        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {

                        Log.d("mylog", "Location" + location.getLongitude() + ", " + location.getLatitude());

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        getTemperature(latitude, longitude);

                    }


                });
            }
        }
        else {
            gpsOutTextView.setText("Turn on the Location");

        }
    }


    public void getTemperature(double latitude, double longitude) {
        String urlString = "https://api.openweathermap.org/data/2.5/weather?units=metric&lat=" +
                latitude + "&lon=" + longitude + "&appid=" + apiKey;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    //convert coordinate into address
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    String cityName = addresses.get(0).getLocality();
                    String countryName = addresses.get(0).getCountryName();
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line).append('\n');
                    }
                    reader.close();

                    JSONObject obj = new JSONObject(response.toString());
                    JSONObject main = obj.getJSONObject("main");
                    double temperature = main.getDouble("temp");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            placeName.setText(cityName + ", " + countryName);
                            gpsOutTextView.setText(String.valueOf(temperature) + "C");
                        }
                    });

                } catch (Exception e) {
                }
            }
        });
        thread.start();
    }

}