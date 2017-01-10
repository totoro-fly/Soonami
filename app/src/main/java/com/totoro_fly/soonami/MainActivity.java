package com.totoro_fly.soonami;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final String USGS_REQUEST_URL = "http://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2014-01-01&endtime=2014-12-01&minmagnitude=6";
    @Bind(R.id.title_textview)
    TextView titleTextview;
    @Bind(R.id.date_textview)
    TextView dateTextview;
    @Bind(R.id.tsunami_alert_textview)
    TextView tsunamiAlertTextview;
    @Bind(R.id.activity_main)
    LinearLayout activityMain;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        progress = new ProgressDialog(this);
        TsunamiAsyncTask task = new TsunamiAsyncTask();
        task.execute();
    }

    private void updateUi(Event earthquake) {
        titleTextview.setText(earthquake.getTitle());
        dateTextview.setText(getDateString(earthquake.getTime()));
        tsunamiAlertTextview.setText(getTsunamiAlertString(earthquake.getTsunamiAlert()));
    }

    private String getDateString(Long time) {
        SimpleDateFormat format = new SimpleDateFormat("EEE,d MMM yyyy HH:mm:ss z");
        return format.format(time);
    }

    private String getTsunamiAlertString(int tsunmiAlert) {
        switch (tsunmiAlert) {
            case 0:
                return getString(R.string.alert_no);
            case 1:
                return getString(R.string.alert_yes);
            default:
                return getString(R.string.alert_not_available);
        }
    }


    private class TsunamiAsyncTask extends AsyncTask<URL, Integer, Event> {
        @Override
        protected void onPreExecute() {
            progress.show();
            progress.setMessage("刷新中...");
        }

        @Override
        protected Event doInBackground(URL... urls) {
            URL url = createUrl(USGS_REQUEST_URL);
            String jsonResponse = "";
            jsonResponse = makeHttpRequest(url);
            Event earthquake = extractFeatureFromJson(jsonResponse);
            return earthquake;
        }


        @Override
        protected void onPostExecute(Event event) {
            if (event == null) {
                return;
            }
            updateUi(event);
            progress.dismiss();
        }
    }


    private URL createUrl(String stringUrl) {
        URL ur1 = null;
        try {
            ur1 = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "createUrl", e);
            e.printStackTrace();
        }
        return ur1;
    }

    private String makeHttpRequest(URL url) {
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            jsonResponse = readFromStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "makeHttpRequest_1", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "makeHttpRequest_2", e);
                    e.printStackTrace();
                }
            }
        }
        return jsonResponse;
    }

    private String readFromStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            try {
                String line = bufferedReader.readLine();
                while (line != null) {
                    stringBuilder.append(line);
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "readFromStream ", e);
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    private Event extractFeatureFromJson(String earthquakeJSON) {
        try {
            JSONObject baseJsonResponse = new JSONObject(earthquakeJSON);
            JSONArray featureArray = baseJsonResponse.getJSONArray("features");
            if (featureArray.length() > 0) {
                JSONObject firstFeature = featureArray.getJSONObject(0);
                JSONObject properties = firstFeature.getJSONObject("properties");
                String title = properties.getString("title");
                long time = properties.getLong("time");
                int tsunamiAlert = properties.getInt("tsunami");
                return new Event(title, time, tsunamiAlert);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "extractFeatureFromJson ", e);
            e.printStackTrace();
        }
        return null;
    }

}
