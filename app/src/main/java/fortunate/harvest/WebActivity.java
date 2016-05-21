package fortunate.harvest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Date;


public class WebActivity extends AppCompatActivity {


    private static final String TAG = "mahc" ;
    WebView myWebView;
    public static final String WEB_URL = "http://harvestentrancecoaching.com/app/pdf.php?url=", EXTRA_MODE_INT = "MODE";
    private static final String EXTRA_CLOSE = "CLOSE" ;
    ProgressBar progress;
    Button showBM,saveBM;
    Button webRefresh,webBackError;
    ImageView loadError;
    boolean isFirstTime=false,isError=false;
    TextView progressPercent,txtMode;
    int globalMode=0;//  0:offline, 1:online

    DBAdapter myDb ;
    private Animation twistAnim;
    private BroadcastReceiver broadcastURLReceiver;
    private Toolbar mToolbar;
    Cursor cursor;
    String url;
    String htmlsrc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

       // cahce mode
        myWebView = (WebView) findViewById(R.id.webview);
        myWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        myWebView.getSettings().setBuiltInZoomControls(true);

        // to disable long click(selection) in webview
        myWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        myWebView.setLongClickable(false);
        myWebView.setHapticFeedbackEnabled(false);




        //database
        openDB();

        // Getting name, email from intent
        Intent i = getIntent();

        int dbID = i.getIntExtra("dbID",0);

        cursor = myDb.getRow(dbID);
        myDb.markAsRead(dbID);


        if( cursor.getCount() > 0 ) {
            cursor.moveToFirst();
            String titleStr = cursor.getString(DBAdapter.COL_TITLE);
            String decStr = cursor.getString(DBAdapter.COL_DESCRIPRION);
            String htmlStr = cursor.getString(DBAdapter.COL_URL);

            htmlsrc ="<p>&nbsp;</p>" +
                    "<h1>"+titleStr+"</h1>" +
                    "<p align=\"justify\"> "+decStr+"</p>" +
                    "<p>"+htmlStr+"</p>";


            if(htmlsrc!=null) {
                    myWebView.loadDataWithBaseURL("", htmlsrc , "text/html", "utf-8", "");
                Log.e("mahc", "URL: " + WEB_URL+url);
                }


        } else {
            Log.e(TAG,"Id not found:"+dbID );
          }

    }

    private void openWebUrl(String url) {

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        myWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        myWebView.loadUrl(url);
        // for progress bar
        final Activity activity = this;
        myWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%

                progressPercent.setText(Integer.toString(progress) + "%");
            }
        });
        //page opens in the same view
        myWebView.setWebViewClient(new MyWebViewClient());
    }

    public void showError(boolean status) {

        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT);
    }

    public void zoomInWebView(View v) {
        myWebView.zoomIn();
    }

    public void zoomOutWebView(View v) {
        myWebView.zoomOut();
    }


    private void openDB() {

        myDb = new DBAdapter(this);
        myDb.open();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDB();
    }

    private void closeDB() {
        myDb.close();
    }

    // buttons
    public void webBack(View v) {
        if ( myWebView.canGoBack()) {
            myWebView.goBack();
        }
    }
    public void webForward(View v) {
        if (myWebView.canGoForward()) {
            myWebView.goForward();
        }
    }



    public void webClearCache(View v) {

        if(isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(), "Getting Latest...", Toast.LENGTH_SHORT).show();
            int currentCacheCongif = myWebView.getSettings().getCacheMode();
            myWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            openWebUrl(WEB_URL + "?m=1");
            myWebView.getSettings().setCacheMode(currentCacheCongif);
        } else {
            Toast.makeText(getApplicationContext(), "No Internet connection!", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
            webBack(myWebView);
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }


    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            Log.e("mahc", "URL: "+ url);
            if (Uri.parse(url).getHost().equals(Uri.parse(WEB_URL).getHost())) {
                // This is my web site, so do not override; let my WebView load the page

                return false;
            }
            if(url.equals("http://ailrsaapp.blogspot.com/?m=0") ) {
                url="http://railwayslocopilots.blogspot.in/?m=0";
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//            startActivity(intent);
//
//            Log.e("mahc", "open Outside!!");
//            Toast.makeText(getApplicationContext(), "Opening in Browser...", Toast.LENGTH_SHORT).show();

            return false;//true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressPercent.setText("0%");
            if(isFirstTime) {
                myWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                isFirstTime =  false;
            }
            if(isError == false) {
                showError(false);
            }

        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            isError = false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            isError = true;
            showError(true);

        }
    }

}
