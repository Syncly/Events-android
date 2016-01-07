package ee.arti.events;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import java.net.MalformedURLException;
import java.net.URL;

public class EventService extends GcmListenerService {

    private static final String TAG = "EventsService";

    private boolean isRunning  = false;
    private NotificationManager notificationManager;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.i(TAG, "onMessageReceived, from: "+from);

        //Log.d(TAG, "Starting Thread");
        //new Thread(new mThread()).start();

        Log.d(TAG, "Showing message");
        sendNotification(data.getString("event"), data.getString("title"));

        DownloadService.startDownloadUrl(this, data.getString("title"), data.getString("url"));
    }

    public class mThread implements Runnable {

        public void run() {
            int i = 0;
            while (i < 20) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "Threading, isRunning "+isRunning+" i " +i);
                i++;
            }
            stopSelf();
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        isRunning = true;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        isRunning = false;
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
