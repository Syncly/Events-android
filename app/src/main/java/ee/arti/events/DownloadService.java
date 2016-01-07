package ee.arti.events;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

public class DownloadService extends Service {

    private static final String TAG = "DownloadService";

    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;

    public DownloadService() {
    }

    public static void startDownloadUrl(Context context, String title, String url){
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        if (intent != null) {
            try {
                downloadUrl(intent.getStringExtra("title"), new URL(intent.getStringExtra("url")));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return Service.START_NOT_STICKY;
    }

    private void downloadUrl(String title, URL url) {
        final int id = 1;

        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(title)
                .setContentText(url.toString())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCategory(Notification.CATEGORY_PROGRESS);


        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = mBuilder.build();
        notificationManager.notify(id, notification);
        startForeground(id, notification);

        // Start a lengthy operation in a background thread
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        int incr;
                        // Do the "lengthy" operation 20 times
                        for (incr = 0; incr <= 100; incr+=5) {
                            // Sets the progress indicator to a max value, the
                            // current completion percentage, and "determinate"
                            // state
                            mBuilder.setProgress(100, incr, false);

                            mBuilder.setContentText("Downloading: "+incr+"/100");

                            // Displays the progress bar for the first time.
                            Notification notification = mBuilder.build();
                            notificationManager.notify(id, notification);
                            startForeground(id, notification);
                            // Sleeps the thread, simulating an operation
                            // that takes time
                            try {
                                // Sleep for 5 seconds
                                Thread.sleep(1*100);
                            } catch (InterruptedException e) {
                                Log.d(TAG, "sleep failure");
                            }
                        }
                        // When the loop is finished, updates the notification
                        mBuilder.setContentText("Download complete")
                                // Removes the progress bar
                                .setProgress(0,0,false)
                                .setAutoCancel(true)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                        notificationManager.notify(id, mBuilder.build());
                        stopForeground(false);
                    }
                }
                // Starts the thread by calling the run() method in its Runnable
        ).start();

    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "onBind");
        return null;
    }
}
