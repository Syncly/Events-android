package ee.arti.events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class Activity extends android.app.Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(this, RegistrationIntentService.class));
        Toast.makeText(this, "Registering Google Cloud Messaging", Toast.LENGTH_SHORT).show();
        finish();
    }
}
