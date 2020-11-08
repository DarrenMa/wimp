package com.supremosolutions.wimp;

/**
 * Created with IntelliJ IDEA.
 * User: darren
 * Date: 6/18/12
 * Time: 2:00 PM
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends Activity {
    Button btnLogin;
    Button btnLinkToRegister;
    EditText inputUsername;
    EditText inputPassword;
    TextView loginErrorMsg;

    // JSON Response node names
    private static String KEY_SUCCESS = "success";
    private static String KEY_ERROR = "error";
    private static String KEY_ERROR_MSG = "error_msg";
    private static String KEY_UID = "uid";
    //private static String KEY_NAME = "name";
    private static String KEY_EMAIL = "email";
    private static String KEY_CREATED_AT = "created_at";

    private static String KEY_USERNAME = "username";
    private static String KEY_API = "api_key";

    // Progress Dialog
    private ProgressDialog pDialog;
    // Loading MSG
    String LOADING_MSG = "Authenticating...";
    private JSONObject json;
    private UserFunctions userFunction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Importing all assets like buttons, text fields
        inputUsername = (EditText) findViewById(R.id.loginUsername);
        inputPassword = (EditText) findViewById(R.id.loginPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        loginErrorMsg = (TextView) findViewById(R.id.login_error);

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                if (Utilities.checkNotEmpty(inputUsername) && Utilities.checkNotEmpty(inputPassword)) {
                    // Clear error and continue
                    loginErrorMsg.setText("");
                    new LoginTask().execute();
                }
                else{
                    // Set error msg
                    loginErrorMsg.setText("Please fillout all the fields");
                }
            }
        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    /**
     * Background Async Task to Load all spots by making an HTTP Request
     */
    class LoginTask extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage(LOADING_MSG);
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * getting spots in JSON from web
         */
        protected String doInBackground(String... args) {
            String username = inputUsername.getText().toString();
            String password = inputPassword.getText().toString();
            userFunction = new UserFunctions();
            json = userFunction.loginUser(username, password);

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * *
         */
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // check for login response
            if (json != null) {

                try {

                    if (json.getString(KEY_SUCCESS) != null) {
                        loginErrorMsg.setText("");
                        String res = json.getString(KEY_SUCCESS);
                        if (Integer.parseInt(res) == 1) {
                            // user successfully logged in
                            // Store user details in SQLite Database
                            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                            //JSONObject json_user = json.getJSONObject("user");

                            // Clear all previous data in database
                            userFunction.logoutUser(getApplicationContext());
                            db.addUser(json.getString(KEY_USERNAME), json.getString(KEY_API));

                            // Launch Dashboard Screen
                            Intent dashboard = new Intent(getApplicationContext(), OverviewActivity.class);

                            // Close all views before launching Dashboard
                            dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(dashboard);

                            // Close Login Screen
                            finish();
                        } else {
                            // Error in login
                            loginErrorMsg.setText("Incorrect username/password");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                // Error in login
                loginErrorMsg.setText("Incorrect username/password");
            }
        }
    }
}