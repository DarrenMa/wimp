package com.supremosolutions.wimp;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: darren
 * Date: 6/4/12
 * Time: 11:54 AM
 */
public class SaveParking extends Activity {
    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jsonParser = new JSONParser();

    ArrayList<HashMap<String, String>> inboxList;

    // JSONArray
    JSONArray inbox = null;

    // JSON url
    String SPOT_URL = "http://wimp.supremosolutions.com/api/v1/spot/";
//    String SPOT_URL = "http://10.0.2.2:8000/api/v1/spot/";


    // ALL JSON node names
    private static final String TAG_NAME = "name";
    private static final String TAG_LAT = "latitude";
    private static final String TAG_LNG = "longitude";

    // Result variables
    int result = 500;
    String RES_USERNAME = "";
    String RES_APIKEY = "";
    String RES_LAT = "";
    String RES_LNG = "";
    String RES_NAME = "";

    String JSON_TYPE_REQUEST = "POST";


    private static final String TAG_SUCCESS = "success";

    // Loading MSG
    String LOADING_MSG = "Saving spot...";

    LocationManager locMgr = null;

    @Override
    public void onDestroy() {
        locMgr.removeUpdates(onLocationChange);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_spot);

        Utilities.setAb(this);

        locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        RES_USERNAME = preferences.getString(Utilities.username_key, "");
        RES_APIKEY = preferences.getString(Utilities.api_key, "");
        SPOT_URL = preferences.getString(Utilities.website_key, "");

        Button saveSpot = (Button) findViewById(R.id.save);
        saveSpot.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText title = (EditText) findViewById(R.id.title);
                String value = title.getText().toString();

                pDialog = new ProgressDialog(SaveParking.this);
                pDialog.setMessage(LOADING_MSG);
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();
                locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, onLocationChange);
                // Got coordinates so build the URL and load the spots.

            }
        });
    }

    /**
     * Background Async Task to Create new spot
     */
    class CreateNewSpot extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * Creating product
         */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("format", "json"));
            params.add(new BasicNameValuePair("name", RES_NAME));
            params.add(new BasicNameValuePair("latitude", RES_LAT));
            params.add(new BasicNameValuePair("longitude", RES_LNG));

            List<NameValuePair> authParams = new ArrayList<NameValuePair>();
            authParams.add(new BasicNameValuePair("username", RES_USERNAME));
            authParams.add(new BasicNameValuePair("api_key", RES_APIKEY));

            result = JSONParser.postJsonData(SPOT_URL, params, authParams);

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * *
         */
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();

            if (result == 201 || result == 200) {
                Toast.makeText(SaveParking.this, "Saved", Toast.LENGTH_LONG).show();

                // Go to detail view of the saved spot
                // Starting new intent
                Intent in = new Intent(getApplicationContext(), ParkingDetail.class);
                in.putExtra(TAG_NAME, RES_NAME);
                in.putExtra(TAG_LAT, RES_LAT);
                in.putExtra(TAG_LNG, RES_LNG);
                startActivity(in);
            } else {
                Toast.makeText(SaveParking.this, "failed", Toast.LENGTH_LONG).show();
            }
        }
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

    LocationListener onLocationChange = new LocationListener() {
        public void onLocationChanged(Location fix) {
            RES_LAT = String.valueOf(fix.getLatitude());
            RES_LNG = String.valueOf(fix.getLongitude());
            EditText tmp_RES_NAME = (EditText) findViewById(R.id.title);
            RES_NAME = tmp_RES_NAME.getText().toString();

            locMgr.removeUpdates(onLocationChange);
            Toast.makeText(SaveParking.this, "Coordinates locked",
                    Toast.LENGTH_LONG).show();

            // Got coordinates so build the URL and load the spots.
            new CreateNewSpot().execute();
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