package ee.arti.events;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegistrationIntentService extends IntentService {

    Handler handler;

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // Get registration token
            InstanceID instanceID = InstanceID.getInstance(this);
            Log.d(TAG, "Getting GCM registration token");
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(TAG, "GCM registration token: " + token);

            // Register this id in the backend server
            sendRegistrationToServer(token);

            // Subscribe to topic channels
            //subscribeTopics(token);

        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }
    }

    /**
     * Persist registration to third-party servers.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        try {
            URL server = new URL(getString(R.string.server));
            URL gcm = new URL(server, "gcm");

            // Open the connection
            Log.d(TAG, "Opening connection");
            HttpURLConnection con = (HttpURLConnection) gcm.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "text/plain");
            con.setDoOutput(true);

            Log.d(TAG, "Sending token");
            // Send our token to the server
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(token);
            wr.flush();
            wr.close();

            Log.d(TAG, "Getting response");
            int responseCode = con.getResponseCode();
            String responseMessage = con.getResponseMessage();
            if (responseCode >= 200 && responseCode < 300 || responseCode == 409) {
                Log.d(TAG, "Registration of token was OK with response " + responseCode + " " + responseMessage + " token: " + token);
                handler.post(new DisplayToast(this, "Registration succeeded"));
            } else {
                Log.e(TAG, "Token registration failed, response " + responseCode + " " + responseMessage + " token " + token);
                handler.post(new DisplayToast(this, "Registration failed, try again later (server problem)"));
            }

        } catch (IOException e) {
            e.printStackTrace();
            handler.post(new DisplayToast(this, "Registration failed, try again later (app problem)"));
        }
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
}