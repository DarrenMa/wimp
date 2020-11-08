package com.supremosolutions.wimp;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
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
 * Date: 6/4/12
 * Time: 11:55 AM
 */
public class AroundMeParking extends ListActivity {
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

    // ALL JSON node names
    private static final String TAG_OBJECTS = "objects";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_LAT = "latitude";
    private static final String TAG_LNG = "longitude";

    // Loading MSG
    String LOADING_MSG = "Loading spots around you...";

    LocationManager locMgr = null;

    @Override
    public void onDestroy() {
        locMgr.removeUpdates(onLocationChange);
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.around_me);
        Utilities.setAb(this);

        locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        RES_USERNAME = preferences.getString(Utilities.username_key, "");
        RES_APIKEY = preferences.getString(Utilities.api_key, "");
        SPOT_URL = preferences.getString(Utilities.website_key, "");

        // Hashmap for ListView
        inboxList = new ArrayList<HashMap<String, String>>();

        pDialog = new ProgressDialog(AroundMeParking.this);
        pDialog.setMessage(LOADING_MSG);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
        locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, onLocationChange);


        // selecting single ListView item
        ListView lv = getListView();

        // Launching new screen on Selecting Single ListItem
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String name = ((TextView) view.findViewById(R.id.name)).getText().toString();
                String latitude = ((TextView) view.findViewById(R.id.latitude)).getText().toString();
                String longitude = ((TextView) view.findViewById(R.id.longitude)).getText().toString();

                // Starting new intent
                Intent in = new Intent(getApplicationContext(), ParkingDetail.class);
                in.putExtra(TAG_NAME, name);
                in.putExtra(TAG_LAT, latitude);
                in.putExtra(TAG_LNG, longitude);
                startActivity(in);
            }
        });
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
                    RES_LAT));
            params.add(new BasicNameValuePair("longitude",
                    RES_LNG));


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
                        ListAdapter adapter = new SimpleAdapter(
                                AroundMeParking.this, inboxList,
                                R.layout.list_item, new String[]{TAG_NAME, TAG_LAT, TAG_LNG},
                                new int[]{R.id.name, R.id.latitude, R.id.longitude});
                        // updating listview
                        setListAdapter(adapter);
                        locMgr.removeUpdates(onLocationChange);
                    }
                });
            } else {
                Toast.makeText(AroundMeParking.this, "failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    LocationListener onLocationChange = new LocationListener() {
        public void onLocationChanged(Location fix) {
            RES_LAT = String.valueOf(fix.getLatitude());
            RES_LNG = String.valueOf(fix.getLongitude());
            locMgr.removeUpdates(onLocationChange);
            Toast
                    .makeText(AroundMeParking.this, "Coordinates locked",
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