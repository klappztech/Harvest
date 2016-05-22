package fortunate.harvest;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gcm.GCMBaseIntentService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mahc on 2/27/2016.
 */
public class HomeActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {


    private Toolbar mToolbar;
    DBAdapter myDb;

    private String TAG = "mahc";

    private String URL_NOTI = "http://www.harvestentrancecoaching.com/app/get_notifications.php";
    private static final String URL_WEB_VIEW ="http://www.harvestentrancecoaching.com/app/web_view.php"; ;

    private SwipeRefreshLayout swipeRefreshLayout;
    // initially offset will be 0, later will be updated while parsing the json
    private int offSet = 0;
    private SwipeListAdapter adapter;
    private List<Message> messageList;
    private ListView listView;

    List<Integer> mapping_to_db = new ArrayList<>();
    int selected_count = 0;
    private BroadcastReceiver broadcastURLReceiver;

    // User Session Manager Class
    UserSessionManager session;

    // Asyntask
    AsyncTask<Void, Void, Void> mUnRegisterTask;
    private ConnectionDetector cd;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        // for toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // User Session Manager
        session = new UserSessionManager(this);

        cd = new ConnectionDetector(getApplicationContext());

        //database
        openDB();
        // for list view click action
        registerListClickCallback();

        //clear all notifications as you will see them now
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancelAll();
        session.clearPendingNotification();
        GCMIntentService.isPendingIntent = false;



        listView = (ListView) findViewById(R.id.listView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        messageList = new ArrayList<>();
        adapter = new SwipeListAdapter(this, messageList);
        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.emptyView));

        // list view touch actions
        //listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        //listView.setItemsCanFocus(false);

        swipeRefreshLayout.setOnRefreshListener(this);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
                                        Log.e(TAG, "on create");
                                        // check whether internaL database s empty
                                        int msg_count  = populateMessageListFromInternalDB();

                                        if(msg_count > 0) {
                                            // display list view
                                            adapter.notifyDataSetChanged();

                                        } else {
                                            // goto next step, ie: get data from online
                                        }

                                        if(cd.isConnectingToInternet()) { // TODO: 5/8/2016 net cnected )
                                            fetchMessagesFromOnlineDB();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Not connected to Internet!",Toast.LENGTH_SHORT);
                                            swipeRefreshLayout.setRefreshing(false);
                                        }
                                    }
                                });



        //******************** Broadcast reciever *********************
        // on getting notification, broadcast to update listview => check and remove // TODO: 5/8/2016
        broadcastURLReceiver =  new BroadcastReceiver() {
            //@SuppressLint("NewApi")
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra("ACTION");
                openDB();


                if(action == "reload") {
                    Log.e("mahc", "Broadcast_received: reload");
                    fetchMessagesFromOnlineDB();
                }

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastURLReceiver, new IntentFilter("ACTION_BROADCAST"));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        adapter.notifyDataSetChanged();
    }

    private void registerListClickCallback() {
        ListView myList = (ListView) findViewById(R.id.listView);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(), "Clicked on Item:id="+id+", pos="+position , Toast.LENGTH_SHORT).show();
                ActionOnClick(id);
            }
        });
    }

    private void ActionOnClick(long id) {
        View v=null;
        long idInOnlineDB = mapping_to_db.get((int) id);
        //setting as read
        messageList.get((int)id).read=1;


        myDb.markAsRead(idInOnlineDB);

        Intent web_intent = new Intent(getApplicationContext(), WebActivity.class);
        web_intent.putExtra("id", idInOnlineDB);

        startActivity(web_intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDB();
    }

    private void closeDB() {
        myDb.close();
        Log.e("mahc", "HOMEActivity::Database: close");
    }

    private void openDB() {
        myDb = new DBAdapter(this);
        myDb.open();
        Log.e("mahc", "HOMEActivity::Database: open");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {

            //logout
            if(session.isUserLoggedIn()) {
                //move to logout
                session.logoutUser();

                //delete internal DB
                myDb.deleteAll();

                mUnRegisterTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        // Register on our server
                        // On server creates a new user
                        // TODO: 5/7/2016 this is not successful
                        ServerUtilities.unregister(getApplicationContext(), session.getGcmId());
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        mUnRegisterTask = null;
                    }

                };
                mUnRegisterTask.execute(null, null, null);



                //GCMRegistrar.unregister(getApplicationContext());//MyApplication.getInstance().session.getGcmId()

                Toast.makeText(getApplicationContext(), "I am logged in, but loggin out", Toast.LENGTH_LONG);
            }

            return true;
        } else  if (id == R.id.action_profile) {

            Intent profile_intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(profile_intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        Log.e(TAG, "OnRefresh..");

        if(cd.isConnectingToInternet()) {
            fetchMessagesFromOnlineDB();
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getApplicationContext(), "Not connected to Internet!",Toast.LENGTH_SHORT);
            swipeRefreshLayout.setRefreshing(false);
        }
        //clear all notifications as you will see them now
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancelAll();
        session.clearPendingNotification();
        GCMIntentService.isPendingIntent = false;

    }
    /**
     * Fetching movies json by making http call
     */
    private void fetchMessagesFromOnlineDB() {

        // showing refresh animation before making http call
        swipeRefreshLayout.setRefreshing(true);

        // appending offset to url
        String url = URL_NOTI+"?std="+session.getStd()+"&course="+session.getcourse();
        Log.e("get_notification", url);

        // Volley's json array request object
        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        int found=0;
                        boolean isNew;
                        Log.d(TAG, response.toString());

                        if (response.length() > 0) {

                            // looping through json and adding to movies list
                            for (int i = 0; i < response.length(); i++) {
                                isNew=true;
                                try {
                                    JSONObject messageObj = response.getJSONObject(i);

                                    Date timeInMillis = new Date();

                                    Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    long date_rcvd = timeInMillis.getTime(); //formatter.format(timeInMillis);

                                    int id          = messageObj.getInt("id");
                                    String title    = messageObj.getString("title");
                                    String url      = messageObj.getString("url");
                                    long date_pub   = messageObj.getLong("date");
                                    String desc     = messageObj.getString("description");



                                    // search for this id in existing Message list
                                    for( Message msg_iter : messageList){
                                        if(msg_iter.id == id) {
                                            found++;isNew=false;
                                            break;
                                        }
                                    }
                                    if(isNew) {
                                        // insert to internal message list
                                        Message m = new Message(id, title,url,date_rcvd,date_pub,desc,0);
                                        messageList.add(0,m);
                                        //create mapping to db here
                                        mapping_to_db.add(0,id);


                                        // TODO: check whether order of messages will change on the go... as message list is updated from online, on the go

                                        //insert in to internal db
                                        myDb.insertRow(id,title,url,date_rcvd,date_pub,desc,0);
                                        Log.e(TAG, "Updated database...");
                                    }


                                } catch (JSONException e) {
                                    Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                }
                            }
                            Log.e(TAG, found+" out of"+response.length()+" is alredy in  database...");

                            // TODO: 3/26/2016 call only if updated?
                            adapter.notifyDataSetChanged();
                        }

                        // stopping swipe refresh
                        swipeRefreshLayout.setRefreshing(false);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Server Error: " + error.getMessage());

                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();

                // stopping swipe refresh
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(req);
    }

    private int populateMessageListFromInternalDB() {

        Cursor cursor = myDb.getAllRows();
        if( cursor.getCount() > 0 ) {

            for( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                Message m = new Message(cursor.getInt(DBAdapter.COL_ROWID)
                        , cursor.getString(DBAdapter.COL_TITLE)
                        , cursor.getString(DBAdapter.COL_URL)
                        , cursor.getLong(DBAdapter.COL_DATE_RCVD)
                        , cursor.getLong(DBAdapter.COL_DATE_PUB)
                        , cursor.getString(DBAdapter.COL_DESCRIPRION)
                        , cursor.getInt(DBAdapter.COL_READ));

                messageList.add(0,m);
                //create mapping to db here
                mapping_to_db.add(0,cursor.getInt(DBAdapter.COL_ROWID));
            }
            Log.e(TAG,"Updated MessageList from DB: count = "+cursor.getCount());
            return cursor.getCount();

        } else {
            Log.e(TAG,"DB is empty");
            return 0;
        }

    }
}
