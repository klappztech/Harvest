package fortunate.harvest;

/**
 * Created by mahc on 2/1/2016.
 */
import static fortunate.harvest.CommonUtilities.SENDER_ID;
import static fortunate.harvest.CommonUtilities.SERVER_URL;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ProfileActivity extends AppCompatActivity {
    private static final String URL_REG_CHECK = "http://www.harvestentrancecoaching.com/app/parent_login_check.php";
    // alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();

    // Internet detector
    ConnectionDetector cd;

    // UI elements
    EditText txtName;
    EditText txtEmail;

    // Register button
    Button btnRegister;
    private Toolbar mToolbar;

    // User Session Manager Class
    UserSessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        // for toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // User Session Manager
        session = new UserSessionManager(getApplicationContext());

        String[] course_mapping = {"Medical", "Engineering"};

        TextView username = (TextView) findViewById(R.id.txtProfileUsername);
        TextView std = (TextView) findViewById(R.id.txtProfileStd);
        TextView course = (TextView) findViewById(R.id.txtProfileCourse);

        username.setText(session.getUsername());
        std.setText(Integer.toString(session.getStd()));

        if(session.getcourse() <= 1) {
            course.setText(course_mapping[session.getcourse()]);
        } else {
            course.setText("Not Available!");
        }


    }

}
