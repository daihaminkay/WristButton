package com.sensor.magic.sensortest;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ModeActivity extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private ScrollView mContainerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);

        mContainerView = (ScrollView) findViewById(R.id.container_mode);
    }

    public void checkRaise(View view) {
        Intent intent = new Intent(this, MicActivity.class);
        intent.putExtra("MODE", "RAISE");
        startActivity(intent);
    }

    public void checkRotate(View view) {
        Intent intent = new Intent(this, MicActivity.class);
        intent.putExtra("MODE", "ROTATE");
        startActivity(intent);
    }
}
