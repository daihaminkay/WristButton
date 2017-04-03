package com.sensor.magic.sensortest;


import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;


public class SensorActivity extends WearableActivity {


    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
    }

    public void startKnockGame(View view) {
        Intent intent = new Intent(this, RunModeActivity.class);
        intent.putExtra("ACTION", "KNOCK");
        startActivity(intent);
    }

    public void viewAllUpdates(View view) {
        Intent intent = new Intent(this, ViewRawActivity.class);
        startActivity(intent);
    }

    public void startAudio(View view) {
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
