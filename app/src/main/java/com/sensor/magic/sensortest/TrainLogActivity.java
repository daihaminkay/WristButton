package com.sensor.magic.sensortest;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TrainLogActivity extends WearableActivity {

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_log);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.train_log_container);
        mTextView = (TextView) findViewById(R.id.log_text);

    }

    @Override
    public void onStart() {
        super.onStart();
        File f = new File(this.getApplicationContext().getFilesDir().getPath() + "micdata.txt");
        if (f.exists()) {
            try {
                Scanner sc = new Scanner(f);
                String data = "";
                while (sc.hasNextLine()) {
                    data += sc.nextLine() + "\n";
                }
                mTextView.setText(data);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
