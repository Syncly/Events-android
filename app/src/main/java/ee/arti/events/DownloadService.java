package ee.arti.events;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DownloadService extends Service {

    private static final String TAG = "DownloadService";

    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;

    private static ArrayList<HashMap<String, String>> queue;

    final int id = 1;

    private Thread tDownloader;

    public DownloadService() {
        queue = new ArrayList<>();
    }

    public static void startDownloadUrl(Context context, String title, String url, String fileName){
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra("title", title);
        intent.putExtra("url", url);
        intent.putExtra("fileName", fileName);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        if (intent != null) {
            addToQueue(intent.getStringExtra("title"),
                    intent.getStringExtra("url"),
                    intent.getStringExtra("fileName"));
            downloadUrl();
        }
        return Service.START_NOT_STICKY;
    }

    private void addToQueue(String title, String url, String fileName) {
        HashMap<String, String> task = new HashMap<>();
        task.put("title", title);
        task.put("url", url);
        task.put("fileName", fileName);
        queue.add(task);
    }

    private void downloadUrl() {
        // Start a lengthy operation in a background thread
        if (tDownloader == null || !tDownloader.isAlive()) {
            tDownloader = new Thread(new getFile());
            tDownloader.start();
            Log.d(TAG, "Thread started");
        } else {
            Log.d(TAG, "Thread already running");
            return;
        }

        Log.d(TAG, "Creating a new notification builder");
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Starting download")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCategory(Notification.CATEGORY_PROGRESS);


        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = mBuilder.build();
        notificationManager.notify(id, notification);
        startForeground(id, notification);

    }

    class getFile implements Runnable {
        private URL url;
        private String fileName;

        @Override
        public void run() {
            while (!queue.isEmpty()) {
                HashMap<String, String> task = queue.get(0);
                queue.remove(0);

                try {
                    url = new URL(task.get("url"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                fileName = task.get("fileName");
                mBuilder.setContentTitle(task.get("title"))
                    .setSound(null);

                Log.d(TAG, "downloading file "+url.toString());
                Log.d(TAG, "Tasks left in queue "+ queue.size());

                int count = 0;
                int progress = 0;
                int oldProgress = -1;
                try {
                    //URL url = new URL(url);
                    URLConnection connection = url.openConnection();
                    connection.connect();

                    // this will be useful so that you can show a tipical 0-100%
                    // progress bar
                    int lenghtOfFile = connection.getContentLength();

                    // download the file
                    InputStream input = new BufferedInputStream(url.openStream(),
                            8192);

                    // Output stream
                    OutputStream output = new FileOutputStream(Environment
                            .getExternalStorageDirectory().toString()
                            + "/Music/"+fileName);

                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        progress = (int) ((total * 100) / lenghtOfFile);
                        if (progress != oldProgress) {
                            mBuilder.setProgress(100, progress, false);
                            mBuilder.setContentText("Downloading: " + progress + "/100");
                            Notification notification = mBuilder.build();
                            notificationManager.notify(id, notification);
                            startForeground(id, notification);
                            oldProgress = progress;
                        }

                        // writing data to file
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                    input.close();

                    mBuilder.setContentText("Download completed");

                } catch (Exception e) {
                    Log.e("Error: ", e.getMessage());
                    mBuilder.setContentText("Download failed");
                }

                // When the loop is finished, updates the notification
                mBuilder.setProgress(0,0,false)  // Removes the progress bar
                        .setAutoCancel(true);
                        //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                notificationManager.notify(id, mBuilder.build());
                stopForeground(false);
            }
        }
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
