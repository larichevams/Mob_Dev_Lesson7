package ru.mirea.laricheva.httpurlconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.mirea.laricheva.httpurlconnection.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonGetIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkinfo = null;
                if (connectivityManager != null)
                {
                    networkinfo = connectivityManager.getActiveNetworkInfo();
                }

                if (networkinfo != null && networkinfo.isConnected())
                {
                    new DownloadPageTask().execute("https://ipinfo.io/json");
                }
                else {
                    binding.textViewIP.setText("Нет интернета");
                }
            }
        });

        binding.buttonGetWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkinfo = null;
                if (connectivityManager != null) {
                    networkinfo = connectivityManager.getActiveNetworkInfo();
                }
                if (networkinfo != null && networkinfo.isConnected()) {
                    String latitude = binding.textViewLatitude.getText().toString();
                    String longitude = binding.textViewLongitude.getText().toString();
                    String ask = "https://api.open-meteo.com/v1/forecast?latitude="+
                            latitude+"&longitude="+longitude+"&current_weather=true";
                    new DownloadPageTaskWeather().execute(ask);
                }
                else {
                    binding.textViewWeather.setText("Нет интернета");
                }
            }
        });
    }


    private class DownloadPageTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            binding.textViewIP.setText("IP: Ожидайте");
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return getDataFromAPI(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "error";
            }
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject responseJson = new JSONObject(result);
                Log.d(MainActivity.class.getSimpleName(), "Response: " + responseJson);
                String ip = responseJson.getString("ip");
                String city = responseJson.getString("city");
                String region = responseJson.getString("country");
                String loc = responseJson.getString("loc");
                String[] coordinates = loc.split(",");
                binding.textViewIP.setText("IP: " + ip);
                binding.textViewCountry.setText("Регион: " + region);
                binding.textViewCity.setText("Город: " + city);
                binding.textViewLatitude.setText(coordinates[0]);
                binding.textViewLongitude.setText(coordinates[1]);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(result);
        }
    }

    private String getDataFromAPI(String address) throws IOException {
        InputStream inputStream = null;
        String data = "";
        try {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(100000);
            connection.setConnectTimeout(100000);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                inputStream = connection.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int read = 0;
                while ((read = inputStream.read()) != -1)
                {
                    bos.write(read);
                }
                bos.close();
                data = bos.toString();
            }
            else
            {
                data = connection.getResponseMessage() + ". Error Code: " + responseCode;
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return data;
    }

    private class DownloadPageTaskWeather extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            binding.textViewWeather.setText("Температура: Ожидайте");
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return getDataFromAPI(urls[0]);
            } catch (IOException e) {
                e.printStackTrace();
                return "error";
            }
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject responseJson = new JSONObject(result);
                Log.d(MainActivity.class.getSimpleName(), "Response: " + responseJson);
                String weather= responseJson.getString("current_weather");
                JSONObject responseJson2 = new JSONObject(weather);
                String temp = responseJson2.getString("temperature");
                binding.textViewWeather.setText("Температура: " + temp + "°C");
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onPostExecute(result);
        }
    }
}