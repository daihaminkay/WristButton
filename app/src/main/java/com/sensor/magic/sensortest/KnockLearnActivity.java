package com.sensor.magic.sensortest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMOreg;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class KnockLearnActivity extends WearableActivity implements SensorEventListener {

    private ArrayList<AccelData> sensorData = new ArrayList<>();

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private Button yes;
    private Button no;
    private SensorManager mSensorManager;
    private Sensor mLight;
    private Queue<String> log = new LinkedBlockingQueue<>();
    private boolean recognised;
    private boolean knock;

    private Classifier classifier;
    private ArrayList<Attribute> attrs;
    private Instances inst;
    private FileWriter fw;
    private FileWriter fw_raw;

    private Pair<AccelData, AccelData> pending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knock_learn);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.knock_learn_container);
        mTextView = (TextView) findViewById(R.id.knock_learn_text);
        yes = (Button) findViewById(R.id.knock_yes);
        no = (Button) findViewById(R.id.knock_no);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        classifier = new SMOreg(); // new LinearRegression(); //new ClassificationViaRegression();
        attrs = new ArrayList<>();
        attrs.add(new Attribute("prevX"));
        attrs.add(new Attribute("prevY"));
        attrs.add(new Attribute("prevZ"));
        attrs.add(new Attribute("X"));
        attrs.add(new Attribute("Y"));
        attrs.add(new Attribute("Z"));
        //attrs.add(new Attribute("Gesture"));
        inst = new Instances("XYZ", attrs, 1000);
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    // TODO: 30/12/2016 FINISH LEARNING ACTIVITY
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        long timestamp = System.currentTimeMillis();
        final AccelData dat = new AccelData(timestamp, x, y, z);
        sensorData.add(dat);
        if (sensorData.size() > 1) {
            AccelData prev = sensorData.get(sensorData.size() - 2);
            double prevZ = prev.getZ();
            //Attempt to eliminate non-knock movements
            double abs = Math.abs(z - prevZ);
            if (!recognised && abs > 25) {
                log.add(String.valueOf(abs));
                mTextView.setText("KNOCK");
                knock = true;
                recognised = true;
                pending = new Pair<>(prev, dat);
                //while(pending != null);
            } else if (!recognised && abs > 5) {
                log.add(String.valueOf(abs));
                mTextView.setText("SHAKE");
                knock = false;
                pending = new Pair<>(prev, dat);
                //while(pending != null);
            } else if (recognised && abs < 1) {
                recognised = false;
            }
            if (log.size() == 10) {
                log.poll();
            }
        }
        if (sensorData.size() > 50)
            sensorData.remove(0);
    }

    public void saidYes(View view) {
        if (knock) {
            if (pending != null) {
                DenseInstance di = new DenseInstance(6);
                di.setValue(0, pending.first.getX());
                di.setValue(1, pending.first.getY());
                di.setValue(2, pending.first.getZ());
                di.setValue(3, pending.second.getX());
                di.setValue(4, pending.second.getY());
                di.setValue(5, pending.second.getZ());

                inst.add(di);
                pending = null;
            }
        }
    }

    public void saidNo(View view) {
        if (!knock) {
            if (pending != null) {
                DenseInstance di = new DenseInstance(6);
                di.setValue(0, pending.first.getX());
                di.setValue(1, pending.first.getY());
                di.setValue(2, pending.first.getZ());
                di.setValue(3, pending.second.getX());
                di.setValue(4, pending.second.getY());
                di.setValue(5, pending.second.getZ());

                inst.add(di);
                pending = null;
            }
        }
    }

    public void endTraining(View view) {
        Log.d("SIZE", String.valueOf(inst.size()));
        inst.setClassIndex(inst.numAttributes() - 1);
        try {
            classifier.buildClassifier(inst);
            Log.d("CLASSIFIER", classifier.toString());
        } catch (Exception e) {
            Log.d("ECLASS", e.getMessage());
        }
        try {

            File f = new File(this.getApplicationContext().getFilesDir().getPath() + "knock_game_data.txt");
            if (!f.exists()) {
                f.createNewFile();
            }
            fw = new FileWriter(f);
            fw.write(inst.toString());
            fw.flush();

            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
