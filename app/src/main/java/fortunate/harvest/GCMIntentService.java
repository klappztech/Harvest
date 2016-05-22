package fortunate.harvest;

/**
 * Created by mahc on 2/1/2016.
 */
        import android.app.Notification;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.app.TaskStackBuilder;
        import android.content.Context;
        import android.content.Intent;
        import android.support.v4.app.NotificationCompat;
        import android.support.v4.content.LocalBroadcastManager;
        import android.util.Log;
        import android.widget.Toast;

        import com.google.android.gcm.GCMBaseIntentService;

        import java.util.Date;

        import static fortunate.harvest.CommonUtilities.SENDER_ID;
        import static fortunate.harvest.CommonUtilities.displayMessage;

public class GCMIntentService extends GCMBaseIntentService {


    static DBAdapter myDb;

    private static final String TAG = "GCMIntentService";

    static UserSessionManager session;
    public static boolean isPendingIntent=false;

    public GCMIntentService() {
        super(SENDER_ID);
    }

    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        // User Session Manager
        session = new UserSessionManager(getApplicationContext());

        //displayMessage(context, "Your device registred with GCM");
        Log.d("NAME", MainActivity.name);
        ServerUtilities.register(context, MainActivity.name, MainActivity.email, registrationId);

        //set as logged in
        session.createUserLoginSession(MainActivity.name, MainActivity.email, registrationId, MainActivity.standard, MainActivity.course);
    }

    /**
     * Method called on device un registred
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        //displayMessage(context, getString(R.string.gcm_unregistered));
        ServerUtilities.unregister(context, registrationId);
    }

    /**
     * Method called on Receiving a new message
     * */
    @Override
    protected void onMessage(Context context, Intent intent) {
        String message = intent.getExtras().getString("price");
        String dbID_str = intent.getExtras().getString("id");
        long dbID = Integer.valueOf(dbID_str);
        Log.e(TAG, "Received message" + message + "id: " + dbID);

        //displayMessage(context, message);

        // User Session Manager
        session = new UserSessionManager(getApplicationContext());
        if(session.isUserLoggedIn()) {
            // notifies user
            generateNotification(context, message,dbID);
        }
    }

    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        //displayMessage(context, message);
        // notifies user
        generateNotification(context, message,0);
    }

    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
        //displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        //displayMessage(context, getString(R.string.gcm_recoverable_error, errorId));
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message,long dbID) {
        int icon = R.drawable.ic_launcher;
        int mId=0;
        long when = System.currentTimeMillis();

        session = new UserSessionManager(context);

        context.getSystemService(Context.NOTIFICATION_SERVICE);
        //Notification notification = new Notification(icon, message, when);


        //String title = context.getString(R.string.app_name);

        //Date timeInMillis = new Date();

        //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        Log.e("mahc", "showing message: "+ message);

        // add to database
        openDB(context);
        //long newId = myDb.insertRow("Result", message, timeInMillis.getTime()/1000,);




        if(isPendingIntent == false){
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("Harvest")
                            .setContentText(message)
                            .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_LIGHTS);;

            // clear notification on click
            mBuilder.setAutoCancel(true);
// Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, WebActivity.class);
            resultIntent.putExtra("id",dbID);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(HomeActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(mId, mBuilder.build());

            //set that notification is pending to be read
            session.setPendingNotification();
            isPendingIntent = true;
        } else {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("Harvest")
                            .setContentText("You've received new messages!")
                            .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_LIGHTS);;

            // clear notification on click
            mBuilder.setAutoCancel(true);
// Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, HomeActivity.class);
            resultIntent.putExtra("id",dbID);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(HomeActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(mId, mBuilder.build());

            //set that notification is pending to be read
            session.setPendingNotification();
        }

        // update the list view in the home activity
        sendBroadcastMessage( context );


    }
    public void clearNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.cancelAll();
    }

    private static void sendBroadcastMessage(Context context) {

        //send broadcast
        Intent intent = new Intent("ACTION_BROADCAST");

        intent.putExtra("ACTION", "reload");
        Log.e("mahc", "Broadcast: reload");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }

    private static void openDB(Context context) {
        myDb = new DBAdapter(context);
        myDb.open();
        Log.e("mahc", "ChatHeadService::Database: open");
    }



}