package com.sensor.magic.sensortest;


import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;


public class MainActivity extends WearableActivity {


    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
    }

    /**
     * Trigger BodyKnock
     * @param view caller
     */
    public void startBodyKnock(View view) {
        Intent intent = new Intent(this, RunModeActivity.class);
        intent.putExtra("ACTION", "KNOCK");
        startActivity(intent);
    }

    /**
     * View raw sensor data - for debugging
     * @param view caller
     */
    public void viewAllUpdates(View view) {
        Intent intent = new Intent(this, ViewRawActivity.class);
        startActivity(intent);
    }

    /**
     * Trigger WristButton
     * @param view caller
     */
    public void startWristButton(View view) {
        Intent intent = new Intent(this, ModeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
