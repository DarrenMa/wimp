package com.supremosolutions.wimp;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AroundMeParkingMap extends MapActivity {
    // Progress Dialog
    //private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jsonParser = new JSONParser();

    ArrayList<HashMap<String, String>> inboxList;

    // JSONArray
    JSONArray inbox = null;

    // JSON url
    String SPOT_URL = "";

    // Result variables
    int result = 500;
    String RES_USERNAME = "";
    String RES_APIKEY = "";
    String RES_LAT = "";
    String RES_LNG = "";
    String RES_NAME = "";
    String RES_DIS = "";

    String COORD_RES_LAT = "";
    String COORD_RES_LNG = "";

    // ALL JSON node names
    private static final String TAG_OBJECTS = "objects";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_LAT = "latitude";
    private static final String TAG_LNG = "longitude";
    private static final String TAG_DIS = "distance";

    // Loading MSG
    String LOADING_MSG = "Loading spots around you...";

    private MapView gMap = null;
    private Drawable marker;
    private GeoPoint gPoint;

    LocationManager locMgr = null;

    @Override
    public void onDestroy() {
        locMgr.removeUpdates(onLocationChange);
        super.onDestroy();
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        Utilities.setAb(this);

        locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        RES_USERNAME = preferences.getString(Utilities.username_key, "");
        RES_APIKEY = preferences.getString(Utilities.api_key, "");
        SPOT_URL = preferences.getString(Utilities.website_key, "");

        // Hashmap for ListView
        inboxList = new ArrayList<HashMap<String, String>>();

        gMap = (MapView) findViewById(R.id.map);
        marker = getResources().getDrawable(R.drawable.marker);
        marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());

        locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, onLocationChange);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utilities.setMainMenu(menu, this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Utilities.myOptionItemSelected(item, this);
        return true;
    }

    /**
     * Background Async Task to Load all spots by making an HTTP Request
     */
    class LoadSpots extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * getting spots in JSON from web
         */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("format",
                    "json"));
            params.add(new BasicNameValuePair("latitude",
                    COORD_RES_LAT));
            params.add(new BasicNameValuePair("longitude",
                    COORD_RES_LNG));


            List<NameValuePair> authParams = new ArrayList<NameValuePair>();
            authParams.add(new BasicNameValuePair("username", RES_USERNAME));
            authParams.add(new BasicNameValuePair("api_key", RES_APIKEY));

            // getting JSON string from URL
            HttpResponse httpResponse = jsonParser.makeHttpRequest(SPOT_URL, "GET", params, authParams);
            if (httpResponse != null) {
                result = httpResponse.getStatusLine().getStatusCode();
            }
            if (result == 201 || result == 200) {
                JSONObject json = jsonParser.convertStreamToJsonString(httpResponse);

                // Check your log cat for JSON response
//                Log.d("AROUND ME PARKING SPOTS JSON: ", json.toString());

                try {
                    inbox = json.getJSONArray(TAG_OBJECTS);
                    // looping through all spots
                    for (int i = 0; i < inbox.length(); i++) {
                        JSONObject c = inbox.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_ID);
                        String name = c.getString(TAG_NAME);
                        String latitude = c.getString(TAG_LAT);
                        String longitude = c.getString(TAG_LNG);
                        String distance = c.getString(TAG_DIS);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_ID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_LAT, latitude);
                        map.put(TAG_LNG, longitude);
                        map.put(TAG_DIS, distance);

                        // add points to map overlay
                        RES_NAME = name;
                        RES_LAT = latitude;
                        RES_LNG = longitude;
                        gPoint = new GeoPoint((int) (Double.valueOf(RES_LAT) * 1000000.0), (int) (Double.valueOf(RES_LNG) * 1000000.0));
                        gMap.getOverlays().add(new ParkingOverlay(marker, gPoint, RES_NAME, getApplicationContext(), distance));


                        // adding HashList to ArrayList
                        inboxList.add(map);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * *
         */
        protected void onPostExecute(String file_url) {            // dismiss the dialog once done

            if (result == 201 || result == 200) {
                gMap.getController().setZoom(17);
                gPoint = new GeoPoint((int) (Double.valueOf(COORD_RES_LAT) * 1000000.0), (int) (Double.valueOf(COORD_RES_LNG) * 1000000.0));
                gMap.getController().setCenter(gPoint);
                gMap.setBuiltInZoomControls(true);
                Toast.makeText(AroundMeParkingMap.this, "Success", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(AroundMeParkingMap.this, "Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    LocationListener onLocationChange = new LocationListener() {
        public void onLocationChanged(Location fix) {
            COORD_RES_LAT = String.valueOf(fix.getLatitude());
            COORD_RES_LNG = String.valueOf(fix.getLongitude());
            locMgr.removeUpdates(onLocationChange);
            Toast
                    .makeText(AroundMeParkingMap.this, "Coordinates locked",
                            Toast.LENGTH_LONG)
                    .show();

            // Got coordinates so build the URL and load the spots.
            new LoadSpots().execute();
        }

        public void onProviderDisabled(String provider) {
            // required for interface, not used
        }

        public void onProviderEnabled(String provider) {
            // required for interface, not used
        }

        public void onStatusChanged(String provider, int status,
                                    Bundle extras) {
            // required for interface, not used
        }
    };
}