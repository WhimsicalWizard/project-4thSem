package com.example.weather;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    LocationManager locationManager;
    LocationListener locationListener;
    private String apiKey = "9a8fc21a793ac7be98bfc2f385336e27";

    private EditText inputText;
    double latitude;
    double longitude;
    private Button search;

    final String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    final int all = 1;
    private TextView cityTextView, gpsOutTextView;


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
        cityTextView = findViewById(R.id.city_out);

gpsOutTextView = findViewById(R.id.gps_out);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMISSIONS, all);
        } else {
            requestLocation();
        }
        inputText = findViewById(R.id.city_input);
        search = findViewById(R.id.search_button);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = String.valueOf(inputText.getText());
                if (!city.isEmpty()) {
                    temperature(city);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                }

            }
        });
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
    }

    public void temperature(String city) {
        String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" +
                city + "&units=metric&appid=" + apiKey;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {


                    //create connection to api
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    //send get request
                    connection.setRequestMethod("GET");

                    //read response
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String response = reader.readLine();
                    reader.close();


                    JSONObject obj = new JSONObject(response);
                    JSONObject main = obj.getJSONObject("main");
                    JSONObject wind = obj.getJSONObject("wind");


                    double temperature = main.getDouble("temp");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cityTextView.setText(String.valueOf(temperature) + "C");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }


    public void getTemperature(double latitude, double longitude) {
        String urlString = "https://api.openweathermap.org/data/2.5/weather?units=metric&lat=" +
                latitude + "&lon=" + longitude + "&appid=" + apiKey;


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
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
        gpsOutTextView.setText(String.valueOf(temperature) + "C");
    }
});

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

}