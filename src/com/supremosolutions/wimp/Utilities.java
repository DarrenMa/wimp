package com.supremosolutions.wimp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: darren
 * Date: 6/4/12
 * Time: 1:42 PM
 */
public class Utilities {

    public static String username_key = "username";
    public static String api_key = "api_key";
    public static String website_key = "website";
    //            public static String website_login_url = "http://10.0.2.2:8000/spots/";
    public static String website_login_url = "http://wimp.supremosolutions.com/spots/";
    //            public static String website_register_url = "http://10.0.2.2:8000/spots/register/";
    public static String website_register_url = "http://wimp.supremosolutions.com/spots/register/";
    public static String password_key = "password";
    public static String email_key = "email";

    public static void setAb(Activity ac) {
        ActionBar actionBar = ac.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public static void LoadSettings(Activity ac) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ac);
        SharedPreferences.Editor editor = preferences.edit();

        DatabaseHandler dbhelper = new DatabaseHandler(ac);
        HashMap<String, String> user = new HashMap<String, String>();
        user = dbhelper.getUserDetails();

        editor.putString(username_key, user.get(username_key));
        editor.putString(api_key, user.get(api_key));
//        editor.putString(website_key, "http://10.0.2.2:8000/api/v1/spot/");
        editor.putString(website_key, "http://wimp.supremosolutions.com/api/v1/spot/");

        editor.commit();
    }

    public static void setMainMenu(Menu menu, Activity ac) {
        MenuInflater inflater = ac.getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
    }

    public static void myOptionItemSelected(MenuItem item, Activity ac) {
        Intent in = null;
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(ac, OverviewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ac.startActivity(intent);
                ac.finish();
                break;
//            case R.id.menu_list:
//                // Starting new intent
//                in = new Intent(ac, ParkingList.class);
//                ac.startActivity(in);
//                break;
//            case R.id.menu_aroundme:
//                // Starting new intent
//                in = new Intent(ac, AroundMeParking.class);
//                ac.startActivity(in);
//                break;
            case R.id.menu_save:
                // Starting new intent
                in = new Intent(ac, SaveParking.class);
                ac.startActivity(in);
                break;
            case R.id.menu_nearest:
                // Starting new intent
                in = new Intent(ac, NearestParking.class);
                ac.startActivity(in);
                break;
//            case R.id.menu_map_list:
//                // Starting new intent
//                in = new Intent(ac, ParkingMap.class);
//                ac.startActivity(in);
//                break;
            case R.id.menu_map_list_aroundme:
                // Starting new intent
                in = new Intent(ac, AroundMeParkingMap.class);
                ac.startActivity(in);
                break;
            case R.id.about:
                // Starting new intent
                in = new Intent(ac, About.class);
                ac.startActivity(in);
                break;
            default:
                break;
        }
    }

    public static boolean checkNotEmpty(EditText etText) {
        return (etText.getText().toString().trim().length() > 0);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
