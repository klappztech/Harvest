package fortunate.harvest;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class UserSessionManager {

    // Shared Preferences reference
    SharedPreferences pref;

    // Editor reference for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREFER_NAME = "AndroidExamplePref";

    // All Shared Preferences Keys
    private static final String IS_USER_LOGIN = "IsUserLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "name";

    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";

    // gcmID
    public static final String KEY_GCM = "gcm_id";

    // std
    public static final String KEY_STD = "gcm_std";

    // course
    public static final String KEY_COURSE = "gcm_course";

    // course
    public static final String KEY_PENDING_NOTIFICATION = "pending_notification";

    // Constructor
    public UserSessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    //Create login session
    public void createUserLoginSession(String name, String email,String gcmId,int std, int course){
        // Storing login value as TRUE
        editor.putBoolean(IS_USER_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_NAME, name);

        // Storing email in pref
        editor.putString(KEY_EMAIL, email);

        // Storing email in pref
        editor.putString(KEY_GCM, gcmId);

        // Storing standard
        editor.putInt(KEY_STD, std);

        // Storing course
        editor.putInt(KEY_COURSE, course);

        // Storing login value as TRUE
        editor.putBoolean(KEY_PENDING_NOTIFICATION, false);

        // commit changes
        editor.commit();

        Log.e("createUserLoginSession", "("+name+","+email+")");
    }

    public String getUsername() {

        return pref.getString(KEY_NAME, "");
    }

    public String getGcmId() {

        return pref.getString(KEY_GCM, "");
    }

    public int getStd() {

        return pref.getInt(KEY_STD, 9999);
    }

    public int getcourse() {

        return pref.getInt(KEY_COURSE, 9999);
    }

    public void setPendingNotification() {

        // Storing pending Notification value as TRUE
        editor.putBoolean(KEY_PENDING_NOTIFICATION, true);
        editor.commit();
    }

    public void clearPendingNotification() {
        // Storing pending Notification value as TRUE
        editor.putBoolean(KEY_PENDING_NOTIFICATION, false);
        editor.commit();
    }

    public boolean isPendingNotification() {
        return pref.getBoolean(KEY_PENDING_NOTIFICATION, false);
    }
    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else do anything
     * */
    public boolean checkLogin(){
        // Check login status
        if(!this.isUserLoggedIn()){

            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, RegisterActivity.class);

            // Closing all the Activities from stack
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);

            return true;
        }
        return false;
    }



    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){

        //Use hashmap to store user credentials
        HashMap<String, String> user = new HashMap<String, String>();

        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        // user email id
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // user email id
       /// user.put(KEY_GCM, pref.getString(KEY_GCM, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){

        // Clearing all user data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, RegisterActivity.class);

        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);

        Log.e("logoutUser", "successful");
    }


    // Check for login
    public boolean isUserLoggedIn(){
        if(pref.getBoolean(IS_USER_LOGIN, false)  ){
            Log.e("isUserLoggedIn", "yes");
        } else {
            Log.e("isUserLoggedIn", "no");
        }

        return pref.getBoolean(IS_USER_LOGIN, false);

    }
}