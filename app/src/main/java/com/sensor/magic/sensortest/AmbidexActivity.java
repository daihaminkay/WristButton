package com.sensor.magic.sensortest;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;

public class AmbidexActivity extends WearableActivity {

    private String mode;
    private String action;
    private boolean test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambidex);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mode = extras.getString("MODE");
            test = extras.getBoolean("TEST");
            action = extras.getString("ACTION");
        }
    }

    /**
     * Invoked if left-handed interaction is triggered.
     * Redirects to the interaction activity according to the previously
     * received intents
     * @param view invoked by this vew (Button)
     */
    public void checkLefty(View view) {
        Intent intent;
        if(action != null && action.equals("KNOCK")){
            intent = new Intent(this, KnockActivity.class);

        } else {
            intent = new Intent(this, MicActivity.class);
            intent.putExtra("MODE", mode);
        }
        intent.putExtra("TEST", test);
        intent.putExtra("HAND", "LEFTY");
        startActivity(intent);
    }

    /**
     * Invoked if right-handed interaction is triggered
     * Redirects to the interaction activity according to the previously
     * received intents
     * @param view invoked by this vew (Button)
     */
    public void checkRighty(View view) {
        Intent intent;
        if(action != null && action.equals("KNOCK")){
            intent = new Intent(this, KnockActivity.class);

        } else {
            intent = new Intent(this, MicActivity.class);
            intent.putExtra("MODE", mode);
        }
        intent.putExtra("TEST", test);
        intent.putExtra("HAND", "RIGHTY");
        startActivity(intent);
    }
}
