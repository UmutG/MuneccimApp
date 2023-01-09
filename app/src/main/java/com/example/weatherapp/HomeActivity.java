package com.example.weatherapp;

// Import related libraries and classes

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    // Create global variables of related elements from activity_home.xml
    private TextView txtLocation, txtDate, txtHour, txtTemp, txtHighTemp, txtLowTemp, txtWeatherHour, txtHumid, txtHourlyTemp, txtRecommend;
    private ImageView presentWeather, imgWeather;
    private ViewGroup scrollWeather;
    public JSONObject weatherData = new JSONObject();
    public int min = 0, max = 0;

    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
    SimpleDateFormat formatter2 = new SimpleDateFormat("MM-dd-yyyy");
    SimpleDateFormat formatter3 = new SimpleDateFormat("HH");

    // Create arrays for hour, humidity and weather information
    String hours[] = new String[24];
    int humidity[] = new int[24];
    int weathers[] = new int[24];
    String weathersImage[] = new String[24];

    public HomeActivity() throws JSONException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize TextView elements by interested IDs
        txtLocation = findViewById(R.id.cityLocation);
        txtDate = findViewById(R.id.presentDate);
        txtHour = findViewById(R.id.presentTime);
        txtTemp = findViewById(R.id.currentTemp);
        txtHighTemp = findViewById(R.id.highTemp);
        txtLowTemp = findViewById(R.id.lowTemp);
        txtRecommend = findViewById(R.id.recommend);

        // Initialize ImageView and ViewGroup elements by interested IDs
        scrollWeather = findViewById(R.id.weatherGroup);
        imgWeather = findViewById(R.id.presentWeather);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            txtDate.setText(LocalDate.now().toString());
        txtDate.setText(formatter2.format(date));
        txtHour.setText(formatter.format(date));
        String hour = String.valueOf((Integer.parseInt(formatter3.format(date)) + 1));

        // Since it gets data from end-point of server via APIs, it has to run
        // with another thread. Create a getJSONObject object and send it to a thread object.

        getJSONObject getJSONObject = new getJSONObject();
        Thread thread = new Thread(getJSONObject);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Here, let's set every component in the slider via setWeathers function.
        try {
            setWeathers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // In order to get JSON output from the server, program needs to create and establish
    // a connection. Therefore, run function will take care of the JSON output.
    public class getJSONObject implements Runnable {
        private String jsonString = "";

        @Override
        public synchronized void run() {
            StringBuilder sb = new StringBuilder();
            try {
                HttpURLConnection urlConnection = null;
                URL url = new URL("http://18.193.123.33:3000/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(5000);
                urlConnection.setConnectTimeout(3000);
                urlConnection.setDoOutput(true);
                urlConnection.connect();
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                urlConnection.disconnect();
                jsonString = sb.toString();
                weatherData = new JSONObject(jsonString);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setWeathers() throws JSONException {
        // Here, it creates slider with hour, humidity and given image
        // Min and max variables will be shown as minimum and maximum temperature of the day.
        min = (int) Math.round((double) weatherData.getJSONObject("hourlyWeatherData").getJSONObject("0").get("temp"));
        max = (int)Math.round((double) weatherData.getJSONObject("hourlyWeatherData").getJSONObject("0").get("temp"));
        for (int len = 0; len < hours.length; len++) {
            // Create hour stamps for slider. Add zero if it is one digit number
            if (len < 10) hours[len] = "0" + len + ":00";
            else hours[len] = len + ":00";

            // Get temperature and humidity to related hour
            double temp = Math.round((double) weatherData.getJSONObject("hourlyWeatherData").getJSONObject(String.valueOf(len)).get("temp"));

            // Set min and max temp values
            if (temp >= max) max = (int)temp;
            if (min >= temp) min = (int)temp;

            weathers[len] = (int) temp;
            humidity[len] = (int) weatherData.getJSONObject("hourlyWeatherData").getJSONObject(String.valueOf(len)).get("humidity");

            // Set image related to the weather information
            weathersImage[len] = (String) weatherData.getJSONObject("hourlyWeatherData").getJSONObject(String.valueOf(len)).get("skytext");

            // Set frame for every single weather and initialize its attributes with given information
            final View singleWeather = getLayoutInflater().inflate(R.layout.daily_weather_frame, null);
            singleWeather.setId(len);

            // Initialize the components
            txtWeatherHour = singleWeather.findViewById(R.id.hour);
            presentWeather = singleWeather.findViewById(R.id.weather);
            txtHourlyTemp = singleWeather.findViewById(R.id.hourlyTemp);
            txtHumid = singleWeather.findViewById(R.id.humidity);

            // Set related data taken from JSON
            txtWeatherHour.setText(hours[len]);
            txtHourlyTemp.setText(weathers[len] + "°|");
            txtHumid.setText(humidity[len] + "%");
            presentWeather.setImageResource(setImage(weathersImage[len]));

            // Add single frame to ViewGroup
            scrollWeather.addView(singleWeather);
        }
        // Set main weather image on the screen with current hour's weather image
        imgWeather.setImageResource(setImage(weathersImage[Integer.parseInt(formatter3.format(date)) + 1]));
        txtLowTemp.setText(min + "°");
        txtHighTemp.setText(max + "°");

    }

    // It is a helper method to set images with related drawable IDs. Also, it sets recommendation text.
    public int setImage(String weatherInfo){
        int imageID = 0;

        if (weatherInfo.equals("Clouds")) {
            imageID = getResources().getIdentifier("cloudy", "drawable", getPackageName());
            txtRecommend.setText("It could be rainy!\nYou may want to wear warmer clothes.");
        }
        if (weatherInfo.equals("Thunderstorm")) {
            imageID = getResources().getIdentifier("lightning", "drawable", getPackageName());
            txtRecommend.setText("Be careful! Lightning strikes!\nDo not go outside if not necessary.");
        }
        if (weatherInfo.equals("Rain")) {
            imageID = getResources().getIdentifier("heavy_rainy", "drawable", getPackageName());
            txtRecommend.setText("Do not forget to take umbrella with you.");
        }
        if (weatherInfo.equals("Snow")) {
            imageID = getResources().getIdentifier("snowy", "drawable", getPackageName());
            txtRecommend.setText("Better to wear warm clothes and layers.\nBe careful to icing!");
        }
        if (weatherInfo.equals("Mist") || weatherInfo.equals("Smoke") || weatherInfo.equals("Haze") || weatherInfo.equals("Dust") ||
                weatherInfo.equals("Fog") || weatherInfo.equals("Sand") || weatherInfo.equals("Ash") ||
                weatherInfo.equals("Squall") || weatherInfo.equals("Tornado")) {
            imageID = getResources().getIdentifier("foggy", "drawable", getPackageName());
            txtRecommend.setText("If you are going to drive, open headlights and maybe wear reflectives.");
        }
        if (weatherInfo.equals("Drizzle")) {
            imageID = getResources().getIdentifier("clear_rainy", "drawable", getPackageName());
            txtRecommend.setText("Do not forget to take raincoat with you.");
        }
        if (weatherInfo.equals("Clear")) {
            imageID = getResources().getIdentifier("sunny", "drawable", getPackageName());
            txtRecommend.setText("It is time to wear sunglasses!");
        }

        return imageID;
    }
}