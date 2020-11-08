package com.supremosolutions.wimp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: darren
 * Date: 6/8/12
 * Time: 1:38 PM
 */
public class NearestParking extends Activity {
    // Progress Dialog
    private ProgressDialog pDialog;

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
    String ORI_LAT = "";
    String ORI_LNG = "";

    // ALL JSON node names
    private static final String TAG_OBJECTS = "objects";
    private static final String TAG_ID = "id";
    private static final String TAG_AUTHOR = "author";
    private static final String TAG_NAME = "name";
    private static final String TAG_LAT = "latitude";
    private static final String TAG_LNG = "longitude";

    // Loading MSG
    String LOADING_MSG = "Loading spots around you...";

    LocationManager locMgr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        Utilities.setAb(this);

        locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        RES_USERNAME = preferences.getString(Utilities.username_key, "");
        RES_APIKEY = preferences.getString(Utilities.api_key, "");
        SPOT_URL = preferences.getString(Utilities.website_key, "");

        // Hashmap for ListView
        inboxList = new ArrayList<HashMap<String, String>>();

        pDialog = new ProgressDialog(NearestParking.this);
        pDialog.setMessage(LOADING_MSG);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
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
                    ORI_LAT));
            params.add(new BasicNameValuePair("longitude",
                    ORI_LNG));
            params.add(new BasicNameValuePair("nearest",
                    "1"));

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
//                Log.d("NEAREST PARKING SPOT JSON: ", json.toString());

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

                        // Results
                        RES_LAT = c.getString(TAG_LAT);
                        RES_LNG = c.getString(TAG_LNG);
                        RES_NAME = c.getString(TAG_NAME);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_ID, id);
                        map.put(TAG_NAME, name);
                        map.put(TAG_LAT, latitude);
                        map.put(TAG_LNG, longitude);

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
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            if (result == 201 || result == 200) {
                // updating UI from Background Thread
                runOnUiThread(new Runnable() {
                    public void run() {
                        /**
                         * Updating parsed JSON data into ListView
                         * */
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                //  Uri.parse(String.format("http://maps.google.com/maps?daddr=%s,%s", Double.toString(helper.getLatitude(c)), Double.toString(helper.getLongitude(c)))));
                                Uri.parse(String.format("geo:0,0?q=%s,%s (%s)", RES_LAT, RES_LNG, RES_NAME)));
                        // Close all views before launching google maps
                        // This is so user can't go back, hit the activity and get sent back to google maps
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            } else {
                Toast.makeText(NearestParking.this, "failed", Toast.LENGTH_LONG).show();
            }

        }
    }

    LocationListener onLocationChange = new LocationListener() {
        public void onLocationChanged(Location fix) {
            ORI_LAT = String.valueOf(fix.getLatitude());
            ORI_LNG = String.valueOf(fix.getLongitude());
            locMgr.removeUpdates(onLocationChange);
            Toast
                    .makeText(NearestParking.this, "Coordinates locked",
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
