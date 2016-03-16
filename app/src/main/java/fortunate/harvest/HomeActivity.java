package fortunate.harvest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private String URL_TOP_250 = "http://api.androidhive.info/json/imdb_top_250.php?offset=";

    private SwipeRefreshLayout swipeRefreshLayout;
    // initially offset will be 0, later will be updated while parsing the json
    private int offSet = 0;
    private SwipeListAdapter adapter;
    private List<Movie> movieList;
    private ListView listView;

    List<Long> selected_list_items = new ArrayList<Long>();
    int selected_count = 0;
    private BroadcastReceiver broadcastURLReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        // for toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //database
        openDB();
        // for list view click action
        registerListClickCallback();


        listView = (ListView) findViewById(R.id.listView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        movieList = new ArrayList<>();
        adapter = new SwipeListAdapter(this, movieList);
        listView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);

                                        fetchMovies();
                                    }
                                });



        //populateListViewFromDB();

        //******************** Broadcast reciever *********************
        broadcastURLReceiver =  new BroadcastReceiver() {
            //@SuppressLint("NewApi")
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getStringExtra("ACTION");

                openDB();


                if(action == "reload") {
                    Log.e("mahc", "Broadcast_received: reload");
                    populateListViewFromDB();
                }

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastURLReceiver, new IntentFilter("ACTION_BROADCAST"));




    }

    private void registerListClickCallback() {
        ListView myList = (ListView) findViewById(R.id.listView);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long idInDB) {
                // Toast.makeText(getApplicationContext(), "Clicked on Item:"+idInDB, Toast.LENGTH_SHORT).show();
                ActionOnClick(idInDB);
            }
        });
    }

    private void ActionOnClick(long idInDB) {
        View v=null;
        Cursor cursor = myDb.getRow(idInDB);
        if (cursor.moveToFirst()) {
            long idDB = cursor.getLong(DBAdapter.COL_ROWID);
            String url = cursor.getString(DBAdapter.COL_URL);


            Intent web_intent = new Intent(getApplicationContext(), WebActivity.class);
            web_intent.putExtra("url",url);

            startActivity(web_intent);

        }
        cursor.close();

    }

    private void sendBroadcastMessage(String url) {

        Intent intent = new Intent("OPEN_WEB_PAGE");
        intent.putExtra("url", url);
        //intent.putExtra("mode", url);
        //intent.putExtra("close", url);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        Log.e("mahc", "==========sent broadcast"+url);

        // close this activity
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDB();
    }

    private void closeDB() {
        myDb.close();
        Log.e("mahc", "MainActivity::Database: close");
    }

    private void openDB() {
        myDb = new DBAdapter(this);
        myDb.open();
        Log.e("mahc", "MainActivity::Database: open");
    }

    public void populateListViewFromDB() {
        Log.e("mahc", "populateListViewFromDB()");
        // Set the adapter for the list view
        final ListView myList = (ListView) findViewById(R.id.listView);
        View empty = findViewById(R.id.emptyView);

        myList.setEmptyView(empty);

        Cursor cursor = myDb.getAllRows();

        // Allow activity to manage lifetime of the cursor.
        // DEPRECATED! Runs on the UI thread, OK for small/short queries.
        startManagingCursor(cursor);

        // Setup mapping from cursor to view fields:
        String[] fromFieldNames = new String[]
                {DBAdapter.KEY_TITLE, DBAdapter.KEY_DATE, DBAdapter.KEY_URL};
        int[] toViewIDs = new int[]
                {R.id.item_name, R.id.item_favcolour, R.id.item_otp};

        // Create adapter to may columns of the DB onto elemesnt in the UI.
        SimpleCursorAdapter myCursorAdapter =
                new SimpleCursorAdapter(
                        this,        // Context
                        R.layout.item_layout,    // Row layout template
                        cursor,                    // cursor (set of DB records to map)
                        fromFieldNames,            // DB Column names
                        toViewIDs                // View IDs to put information in
                );

        //for changing the date to good format
        myCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {

                if (aColumnIndex == 3) {
                    String createDate = aCursor.getString(aColumnIndex);
                    String dateString = getTimeAgo(Long.parseLong(createDate)*1000,getApplicationContext());

                    TextView textView = (TextView) aView;
                    textView.setText(dateString);
                    return true;
                }

                return false;
            }
        });


        myList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        myList.setItemsCanFocus(false);
        myList.setAdapter(myCursorAdapter);
        myList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                if (b) {
                    selected_count++;
                    actionMode.setTitle(selected_count + " items selected");
                    selected_list_items.add(l);
                } else {
                    selected_count--;
                    actionMode.setTitle(selected_count + " items selected");
                    selected_list_items.remove(l);
                }

            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.menu_otps, menu);
                selected_count=0;
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.delete_id:
                        if (selected_list_items.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Select items to delete!", Toast.LENGTH_LONG);
                        }
                        for (long id : selected_list_items) {
                            Cursor cursor = myDb.getRow(id);
                            if (cursor.moveToFirst()) {
                                long idDB = cursor.getLong(DBAdapter.COL_ROWID);
                                // String name = cursor.getString(DBAdapter.COL_TITLE);
                                String url = cursor.getString(DBAdapter.COL_URL);
                                //String favColour = cursor.getString(DBAdapter.COL_FAVCOLOUR);

                                myDb.deleteRow(idDB);
                            }
                            cursor.close();

                        }
                        populateListViewFromDB();
                        Toast.makeText(getApplicationContext(), selected_count + "items deleted", Toast.LENGTH_SHORT);

                        actionMode.finish();
                        return true;
//                        break;
                    case R.id.clear_id:
                        selected_count=0;
                        selected_list_items.clear();
                        for ( int i=0; i < myList.getAdapter().getCount(); i++) {
                            myList.setItemChecked(i, true);
                        }

                        break;

                    default:
                        return false;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                selected_count = 0;
                selected_list_items.clear();
            }
        });


    }

    public String getTimeAgo(long time, Context ctx) {

        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        final long DAY_MILLIS = 24 * HOUR_MILLIS;
        final long MONTH_MILLIS = 30 * DAY_MILLIS;
        long diff=0;

        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        if (isToday(time)) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            Date resultdate = new Date(time);
            return sdf.format(resultdate).toString();
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, dd/MM/yyyy");
            Date resultdate = new Date(time);
            return sdf.format(resultdate).toString();
        }

    }

    private boolean isToday(long time) {

        final int SECOND_MILLIS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        final long DAY_MILLIS = 24 * HOUR_MILLIS;
        final long MONTH_MILLIS = 30 * DAY_MILLIS;

        Date resultdate = new Date(time);
        long now = System.currentTimeMillis();

        if( Math.floor(now/DAY_MILLIS) == Math.floor(time/DAY_MILLIS)  ) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            long newId = myDb.insertRow("Result", "Its working", 10000);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        fetchMovies();
    }
    /**
     * Fetching movies json by making http call
     */
    private void fetchMovies() {

        // showing refresh animation before making http call
        swipeRefreshLayout.setRefreshing(true);

        // appending offset to url
        String url = URL_TOP_250 + offSet;

        // Volley's json array request object
        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());

                        if (response.length() > 0) {

                            // looping through json and adding to movies list
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject movieObj = response.getJSONObject(i);

                                    int rank = movieObj.getInt("rank");
                                    String title = movieObj.getString("title");

                                    Movie m = new Movie(rank, title);

                                    movieList.add(0, m);

                                    // updating offset value to highest value
                                    if (rank >= offSet)
                                        offSet = rank;

                                } catch (JSONException e) {
                                    Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                }
                            }

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
}
