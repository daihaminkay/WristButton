package com.sensor.magic.sensortest;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.ScrollView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AmbidexActivity extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private ScrollView mContainerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambidex);

        mContainerView = (ScrollView) findViewById(R.id.container_ambidex);
    }

    public void checkLefty(View view) {
        Intent intent = new Intent(this, MicActivity.class);
        intent.putExtra("MODE", "RAISE");
        intent.putExtra("HAND", "LEFTY");
        startActivity(intent);
    }

    public void checkRighty(View view) {
        Intent intent = new Intent(this, MicActivity.class);
        intent.putExtra("MODE", "RAISE");
        intent.putExtra("HAND", "RIGHTY");
        startActivity(intent);
    }
}
