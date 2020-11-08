package com.supremosolutions.wimp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: darren
 * Date: 6/5/12
 * Time: 1:19 PM
 */
public class ParkingDetail extends Activity {
    // ALL JSON node names
    private static final String TAG_OBJECTS = "objects";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_LAT = "latitude";
    private static final String TAG_LNG = "longitude";
    private static final String TAG_DIS = "distance";

    String RES_LAT = "";
    String RES_LNG = "";
    String RES_NAME = "";
    String RES_DIS = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_list_item);
        Utilities.setAb(this);

        // getting intent data
        Intent in = getIntent();

        // Get JSON values from previous intent
        String RES_NAME = in.getStringExtra(TAG_NAME);
        String RES_LAT = in.getStringExtra(TAG_LAT);
        String RES_LNG = in.getStringExtra(TAG_LNG);
        String RES_DIS = in.getStringExtra(TAG_DIS);

        // Displaying all values on the screen
        TextView lblName = (TextView) findViewById(R.id.name);
        TextView lbllatitude = (TextView) findViewById(R.id.latitude);
        TextView lbllongitude = (TextView) findViewById(R.id.longitude);
        TextView lbldistance = (TextView) findViewById(R.id.distance);

        lblName.setText(RES_NAME);
        lbllatitude.setText(RES_LAT);
        lbllongitude.setText(RES_LNG);
        lbldistance.setText(RES_DIS);

        Button prefBtn = (Button) findViewById(R.id.take_me);
        prefBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView lblName = (TextView) findViewById(R.id.name);
                TextView lbllatitude = (TextView) findViewById(R.id.latitude);
                TextView lbllongitude = (TextView) findViewById(R.id.longitude);

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        //  Uri.parse(String.format("http://maps.google.com/maps?daddr=%s,%s", Double.toString(helper.getLatitude(c)), Double.toString(helper.getLongitude(c)))));
                        Uri.parse(String.format("geo:0,0?q=%s,%s (%s)", lbllatitude.getText(), lbllongitude.getText(), lblName.getText())));
                startActivity(intent);
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
}
