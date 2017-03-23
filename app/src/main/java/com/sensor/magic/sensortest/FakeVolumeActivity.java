package com.sensor.magic.sensortest;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import android.os.Vibrator;


public class FakeVolumeActivity extends WearableActivity {

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private ScrollView mContainerView;
    private TextView target;
    private int targetNum;
    private TextView current;
    private int currentNum;
    private Random rand = new Random();
    private Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_volume);
        setAmbientEnabled();

        mContainerView = (ScrollView) findViewById(R.id.container_volume);
        target = (TextView) findViewById(R.id.target);
        current = (TextView) findViewById(R.id.current);
        current.setText("0");
        targetNum = getRandom();
        target.setText(String.valueOf(targetNum));
        v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void raiseVolume(View view){
        current.setText(String.valueOf(++currentNum));
        if(currentNum == targetNum){
            v.vibrate(500);
            current.setBackgroundColor(Color.GREEN);
            targetNum = getRandom();
            target.setText(String.valueOf(targetNum));
        } else {
            current.setBackgroundColor(Color.WHITE);
        }
    }

    public void lowerVolume(View view){
        current.setText(String.valueOf(--currentNum));
        if(currentNum == targetNum){
            v.vibrate(500);
            current.setBackgroundColor(Color.GREEN);
            targetNum = getRandom();
            target.setText(String.valueOf(targetNum));
        } else {
            current.setBackgroundColor(Color.WHITE);
        }
    }

    private int getRandom(){
        int base = rand.nextInt(30);
        boolean sign = rand.nextBoolean();
        if(sign)
            base = -base;
        return base;
    }


}
