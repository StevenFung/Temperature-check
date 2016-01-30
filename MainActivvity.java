package edu.umb.cs443;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.lang.String;
import java.net.URLConnection;
import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    public final static String DEBUG_TAG = "edu.umb.cs443.MYMSG";

    private GoogleMap mMap;
    String input;
    JSONObject jobj;
    JSONArray jarray;
    double temp;
    double lat;
    double lng;
    String icon;
    TextView textView;
    EditText editText;
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);

        MapFragment mFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        mFragment.getMapAsync(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void getWeatherInfo(View v) throws JSONException {



        editText = (EditText) findViewById(R.id.editText);
        input = String.valueOf(editText.getText());


        //check internet connection and call the first thread.
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService((Context.CONNECTIVITY_SERVICE));
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            input = input.replaceAll(" ","");
            String dataURL = "http://api.openweathermap.org/data/2.5/weather?q=" + input + "&APPID=f9a0da7858696d1453d0faa23006c2d9";
            new DownloadWebpageTask().execute(dataURL);


        } else {
            textView.setText("NO INTERNET ACCESS");
        }
        editText.setEnabled(false);
        editText.setEnabled(true);

    }


    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadtext(urls[0]);
            } catch (IOException e) {
                return null;
            }
        }


    protected void onPostExecute(String result) {
        if (result != null) {
            try {
                jobj = new JSONObject(result);
                temp = jobj.getJSONObject("main").getDouble("temp");
                lng = jobj.getJSONObject("coord").getDouble("lon");
                lat = jobj.getJSONObject("coord").getDouble("lat");
                temp = (temp -273.15);
                textView.setText(String.format("%.1f", temp) + "C");

                jarray = jobj.getJSONArray("weather");
                jobj = jarray.getJSONObject(0);
                icon = jobj.getString("icon");
                String iconURL = "http://openweathermap.org/img/w/" + icon +".png";
                new DownloadimageTask().execute(iconURL);


                CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(lat, lng));
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);
                mMap.moveCamera(center);
                mMap.animateCamera(zoom);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(DEBUG_TAG, "returned bitmap is null");
        }
    }
}

    private String downloadtext(String myurl) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.i(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            Reader reader = new InputStreamReader(is, "UTF-8");
            char[] buffer = new char[1000];
            reader.read(buffer);
            String infoText =  new String(buffer);
            return infoText;

        } finally {
            if (is != null) {
                is.close();
            }
        }
    }


    private class DownloadimageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadimage(urls[0]);
            } catch (IOException e) {
                return null;
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Bitmap result) {
            if(result!=null) imageView.setImageBitmap(result);
            else{
                Log.i(DEBUG_TAG, "returned bitmap is null");}
        }
    }

    private Bitmap downloadimage(String myurl) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.i(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }






    @Override
    public void onMapReady(GoogleMap map) {
        this.mMap=map;
    }
}
