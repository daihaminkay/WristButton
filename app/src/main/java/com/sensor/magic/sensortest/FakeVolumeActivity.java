package com.sensor.magic.sensortest;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import android.os.Vibrator;


public class FakeVolumeActivity extends WearableActivity {
    private TextView target;
    private int targetNum;
    private TextView current;
    private int currentNum;
    private Random rand = new Random();
    private Vibrator v;
    private boolean test;
    private PrintStream ps;
    private final int TRIALS = 10;
    private int trial = 0;
    private long timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_volume);
        setAmbientEnabled();

        target = (TextView) findViewById(R.id.target);
        current = (TextView) findViewById(R.id.current);
        current.setText("0");
        targetNum = getRandom();
        target.setText(String.valueOf(targetNum));
        v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            test = extras.getBoolean("TEST");
        }

        timer = System.currentTimeMillis();

        if(!test) {
            File f = new File(getApplicationContext().getExternalFilesDir(null), "standardTestFile.csv");
            try {
                if (!f.exists()) {
                    f.createNewFile();
                }
                ps = new PrintStream(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void raiseVolume(View view){
        current.setText(String.valueOf(++currentNum));
        reset();
    }

    public void lowerVolume(View view){
        current.setText(String.valueOf(--currentNum));
        reset();
    }

    /**
     * Reset the target number to be matched
     */
    private void reset(){
        if(currentNum == targetNum){
            v.vibrate(500);
            current.setBackgroundColor(Color.GREEN);
            targetNum = getRandom();
            target.setText(String.valueOf(targetNum));
            if(!test){
                ps.print(System.currentTimeMillis() - timer);
                ps.println();
                ps.flush();
            }
            if(++trial == TRIALS && !test){
                this.finish();
            }
        } else {
            current.setBackgroundColor(Color.WHITE);
        }
    }

    private int getRandom(){
        int base = rand.nextInt(10);
        boolean sign = rand.nextBoolean();
        if(sign)
            base = -base;
        return base;
    }


}
