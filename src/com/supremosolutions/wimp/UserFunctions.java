package com.supremosolutions.wimp; /**
 * Created with IntelliJ IDEA.
 * User: darren
 * Date: 6/18/12
 * Time: 2:02 PM
 */

import android.content.Context;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserFunctions {

    private JSONParser jsonParser;

    // use http://10.0.2.2/ to connect to your localhost ie http://localhost/
    private static String loginURL = Utilities.website_login_url;
    private static String registerURL = Utilities.website_register_url;

    private static String login_tag = "login";
    private static String register_tag = "register";

    int result = 500;

    // constructor
    public UserFunctions() {
        jsonParser = new JSONParser();
    }

    /**
     * function make Login Request
     *
     * @param username
     * @param password
     */
    public JSONObject loginUser(String username, String password) {
        JSONObject json = null;
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        List<NameValuePair> authParams = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", login_tag));
        params.add(new BasicNameValuePair(Utilities.username_key, username));
        params.add(new BasicNameValuePair(Utilities.password_key, password));
        //JSONObject json = jsonParser.getJSONFromUrl(loginURL, params);

        // getting JSON string from URL
//        json = jsonParser.getJSONFromUrl(loginURL, params);
        HttpResponse httpResponse = jsonParser.makeHttpRequest(loginURL, "POST", params, authParams);
        if (httpResponse != null) {
            result = httpResponse.getStatusLine().getStatusCode();
        }
        if (result == 201 || result == 200) {
            json = jsonParser.convertStreamToJsonString(httpResponse);
        }
        return json;
    }

    /**
     * function make Login Request
     *
     * @param username
     * @param email
     * @param password
     */
    public JSONObject registerUser(String username, String email, String password) {
        JSONObject json = null;
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        List<NameValuePair> authParams = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", register_tag));
        params.add(new BasicNameValuePair(Utilities.username_key, username));
        params.add(new BasicNameValuePair(Utilities.email_key, email));
        params.add(new BasicNameValuePair(Utilities.password_key, password));

        // getting JSON Object
        //JSONObject json = jsonParser.getJSONFromUrl(registerURL, params);
        // return json
        HttpResponse httpResponse = jsonParser.makeHttpRequest(registerURL, "POST", params, authParams);
        if (httpResponse != null) {
            result = httpResponse.getStatusLine().getStatusCode();
        }
        if (result == 201 || result == 200) {
            json = jsonParser.convertStreamToJsonString(httpResponse);
        }
        return json;
    }

    /**
     * Function get Login status
     */
    public boolean isUserLoggedIn(Context context) {
        DatabaseHandler db = new DatabaseHandler(context);
        int count = db.getRowCount();
        if (count > 0) {
            // user logged in
            return true;
        }
        return false;
    }

    /**
     * Function to logout user
     * Reset Database
     */
    public boolean logoutUser(Context context) {
        DatabaseHandler db = new DatabaseHandler(context);
        db.resetTables();
        return true;
    }

}