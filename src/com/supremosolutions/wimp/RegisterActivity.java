package com.supremosolutions.wimp;

/**
 * Created with IntelliJ IDEA.
 * User: darren
 * Date: 6/18/12
 * Time: 2:14 PM
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

public class RegisterActivity extends Activity {
    Button btnRegister;
    Button btnLinkToLogin;
    EditText inputUsername;
    EditText inputEmail;
    EditText inputPassword;
    TextView registerErrorMsg;

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
    String LOADING_MSG = "Registering...";
    private JSONObject json;
    private UserFunctions userFunction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // Importing all assets like buttons, text fields
        inputUsername = (EditText) findViewById(R.id.registerName);
        inputEmail = (EditText) findViewById(R.id.registerEmail);
        inputPassword = (EditText) findViewById(R.id.registerPassword);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
        registerErrorMsg = (TextView) findViewById(R.id.register_error);

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (Utilities.checkNotEmpty(inputUsername) && Utilities.checkNotEmpty(inputPassword) && Utilities.checkNotEmpty(inputEmail)) {
                    // Clear error and continue
                    registerErrorMsg.setText("");
                    new RegisterTask().execute();
                }
                else{
                    // Set error msg
                    registerErrorMsg.setText("Please fillout all the fields");
                }
            }
        });


        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                // Close Registration View
                finish();
            }
        });
    }

    /**
     * Background Async Task to Load all spots by making an HTTP Request
     */
    class RegisterTask extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RegisterActivity.this);
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
            String email = inputEmail.getText().toString();
            String password = inputPassword.getText().toString();
            userFunction = new UserFunctions();
            json = userFunction.registerUser(username, email, password);

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
                // check for login response
                try {
                    if (json.getString(KEY_SUCCESS) != null) {
                        registerErrorMsg.setText("");
                        String res = json.getString(KEY_SUCCESS);
                        if (Integer.parseInt(res) == 1) {
                            // user successfully registered
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
                            // Close Registration Screen
                            finish();
                        } else {
                            // Error in registration
                            registerErrorMsg.setText("Error occurred in registration");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                // Error in login
                registerErrorMsg.setText("Username already exists. Try another.");
            }
        }
    }
}