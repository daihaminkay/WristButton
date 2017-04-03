package com.sensor.magic.sensortest;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;

public class ModeActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);
    }

    /**
     * Motion mode
     * @param view caller
     */
    public void checkMotion(View view) {
        Intent intent = new Intent(this, RunModeActivity.class);
        intent.putExtra("MODE", "RAISE");
        intent.putExtra("ACTION", "BUTTON");
        startActivity(intent);
    }

    /**
     * Rotation mode
     * @param view caller
     */
    public void checkRotate(View view) {
        Intent intent = new Intent(this, RunModeActivity.class);
        intent.putExtra("MODE", "ROTATE");
        intent.putExtra("ACTION", "BUTTON");
        startActivity(intent);
    }

    /**
     * Fake volume mode
     * @param view caller
     */
    public void checkConventional(View view) {
        Intent intent = new Intent(this, RunModeActivity.class);
        intent.putExtra("ACTION", "NORM");
        startActivity(intent);
    }
}
