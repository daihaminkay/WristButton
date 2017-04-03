package com.sensor.magic.sensortest;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;

public class RunModeActivity extends WearableActivity {

    private String mode;
    private String knock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_mode);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mode = extras.getString("MODE");
            knock = extras.getString("ACTION");
        }
    }

    /**
     * Trigger interaction in test mode - no stats collected
     * @param view caller
     */
    public void startTest(View view){
        Intent intent;
        if(knock.equals("KNOCK")){
            intent = new Intent(this, AmbidexActivity.class);
            intent.putExtra("ACTION", knock);
        } else if (knock.equals("BUTTON")) {
            intent = new Intent(this, AmbidexActivity.class);
            intent.putExtra("MODE", mode);
        } else {
            intent = new Intent(this, FakeVolumeActivity.class);
        }
        intent.putExtra("TEST", true);
        startActivity(intent);
    }

    /**
     * Trigger interaction in trial mode - stats are collected
     * @param view caller
     */
    public void startTrial(View view){
        Intent intent;
        if(knock.equals("KNOCK")){
            intent = new Intent(this, AmbidexActivity.class);
            intent.putExtra("ACTION", knock);
        } else if (knock.equals("BUTTON")) {
            intent = new Intent(this, AmbidexActivity.class);
            intent.putExtra("MODE", mode);
        } else {
            intent = new Intent(this, FakeVolumeActivity.class);
        }
        intent.putExtra("TEST", false);
        startActivity(intent);
    }
}
