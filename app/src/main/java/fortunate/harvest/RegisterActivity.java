package fortunate.harvest;

/**
 * Created by mahc on 2/1/2016.
 */
import static fortunate.harvest.CommonUtilities.SENDER_ID;
import static fortunate.harvest.CommonUtilities.SERVER_URL;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class RegisterActivity extends AppCompatActivity {
    private static final String URL_REG_CHECK = "http://www.harvestentrancecoaching.com/app/parent_login_check.php";
    // alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();

    // Internet detector
    ConnectionDetector cd;
    boolean isConnectedToInternet = false;

    // UI elements
    EditText txtName;
    EditText txtEmail;

    // Register button
    Button btnRegister;
    private Toolbar mToolbar;

    // User Session Manager Class
    UserSessionManager session;

    ProgressDialog pd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // for toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        cd = new ConnectionDetector(getApplicationContext());

        pd = new ProgressDialog(RegisterActivity.this);       pd.setMessage("loading");
        pd.setCancelable(false);

        // User Session Manager
        session = new UserSessionManager(getApplicationContext());
        if(session.isUserLoggedIn()) {
            //move to HomeActivity
            Intent intentHome = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intentHome);
            finish();
        } else {

            if(!cd.isConnectingToInternet()) {
                checkInternetAndLaunchDialog();
            }
        }

        // Check if GCM configuration is set
        if (SERVER_URL == null || SENDER_ID == null || SERVER_URL.length() == 0
                || SENDER_ID.length() == 0) {
            // GCM sernder id / server url is missing
            alert.showAlertDialog(RegisterActivity.this, "Configuration Error!",
                    "Please set your Server URL and GCM Sender ID", false);
            // stop executing code by return
            return;
        }

        txtName = (EditText) findViewById(R.id.txtUsername);
        txtEmail = (EditText) findViewById(R.id.txtPassword);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        Toast.makeText(getApplicationContext(),
                "User Login Status: " + session.isUserLoggedIn(),
                Toast.LENGTH_LONG).show();



        /*
         * Click event on Register button
         * */
        btnRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Read EditText dat
                String name = txtName.getText().toString();
                String email = txtEmail.getText().toString();

                // Check if user filled the form
                if(name.trim().length() > 0 && email.trim().length() > 0){
                    // // TODO: 5/4/2016 remove this portion
                    if(cd.isConnectingToInternet()) { // TODO: 5/8/2016 net cnected )
                        IsRegisteredOnline(name, email);
                    } else {
                        if(!cd.isConnectingToInternet()) {
                            checkInternetAndLaunchDialog();
                        }
                    }


                }else{
                    // user doen't filled that data
                    // ask him to fill the form
                    alert.showAlertDialog(RegisterActivity.this, "Registration Error!", "Please enter your details", false);
                }
            }
        });
    }

    private void checkInternetAndLaunchDialog() {

        alert.showAlertDialog(RegisterActivity.this, "Connection Failed!", "Please Check Your Internet Connection", false);

//        AlertDialog dialog = new AlertDialog.Builder(this)
//                .setTitle("Connection Failed")
//                .setMessage("Please Check Your Internet Connection")
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        //Code for try again
//                        // nothing to do, dialog will close
//                    }
//                }).create();
//        dialog.show();
    }

    private void IsRegisteredOnline(String username, String password) {
        // appending offset to url
        String url = URL_REG_CHECK+"?username="+username+"&password="+password;

        pd.show();

        // Volley's json array request object
        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        String username;
                        String password;
                        Log.d("IsRegisteredOnline", response.toString());

                        pd.hide();


                        username = txtName.getText().toString();
                        password = txtEmail.getText().toString();

                        if (response.length() > 0) {

                            // looping through json and adding to movies list
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject messageObj = response.getJSONObject(i);

                                    int registered        = messageObj.getInt("registered");
                                    int gcmRegistered     = messageObj.getInt("gcm_registered");
                                    int standard          = messageObj.getInt("standard");
                                    int course            = messageObj.getInt("course");
                                    String gcm_id         = messageObj.getString("gcm_id");

                                   if(registered==1 ) {
                                       //get GCM Id and update gcm users table, then update shared pref
                                        // Launch Main Activity
                                        Intent intentMain = new Intent(getApplicationContext(), MainActivity.class);

                                        // Registering user on our server
                                        // Sending registration details to MainActivity
                                        intentMain.putExtra("name", username);
                                        intentMain.putExtra("email", password);
                                        intentMain.putExtra("standard", standard);
                                        intentMain.putExtra("course", course);

                                        startActivity(intentMain);
                                        finish();


                                    } else {
                                       // Toast.makeText(getApplicationContext(), "Your password is incorrect!", Toast.LENGTH_LONG).show();
                                       alert.showAlertDialog(RegisterActivity.this, "Login Error!",
                                               "Your password is incorrect!", false);
                                   }



                                } catch (JSONException e) {
                                    Log.e("IsRegisteredOnline", "JSON Parsing error: " + e.getMessage());
                                }
                            }

                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("IsRegisteredOnline", "Server Error: " + error.getMessage());

                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(req);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {

            // TODO: 5/8/2016 show help
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
