package com.sensor.magic.sensortest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMOreg;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class KnockActivity extends WearableActivity implements SensorEventListener {

    private ArrayList<AccelData> sensorData = new ArrayList<>();
    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private SensorManager mSensorManager;
    private Sensor mAccel;
    private Sensor mGyro;
    private Queue<String> log = new LinkedBlockingQueue<>();
    private boolean recognised;
    private boolean gyro = false;
    private AccelData pendingRotation = null;
    private PrintStream ps;
    private final int TRIALS = 10;
    private int trial = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knock);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.knock_container);
        mTextView = (TextView) findViewById(R.id.knock_text);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        File f = new File(getApplicationContext().getExternalFilesDir(null), "knockTestFile.csv");
        try {
            if(!f.exists()){
                f.createNewFile();
            }
            ps = new PrintStream(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    protected void onStop() {
        System.out.println("onstop");
        ps.close();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    private boolean interval(double less, double val, double more) {
        return val > less && val < more;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        long timestamp = System.currentTimeMillis();
        AccelData dat = new AccelData(timestamp, x, y, z);
        sensorData.add(dat);
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            if (sensorData.size() > 1) {
                AccelData prev = sensorData.get(sensorData.size() - 2);
                //Attempt to eliminate non-knock movements
                double absX = Math.abs(x - prev.getX());
                double absY = Math.abs(y - prev.getY());
                double absZ = Math.abs(z - prev.getZ());

                DenseInstance di = new DenseInstance(6);
                di.setValue(0, prev.getX());
                di.setValue(1, prev.getY());
                di.setValue(2, prev.getZ());
                di.setValue(3, dat.getX());
                di.setValue(4, dat.getY());
                di.setValue(5, dat.getZ());
                /**In case I'd want to switch back to finger tap recognition**/
//                if (!recognised && interval(2, x, 4) && interval(5, absZ, 8)) {
//                    mRawView.setText(anythingToString(sensorData));
//                    mTextView.setText("2-FINGER TAP");
//                    recognised = true;
//                } else
                if (!recognised && absZ > 30 ) {
                    log.add(String.valueOf(absZ));
                    if (pendingRotation != null) {
                        if (interval(-120, pendingRotation.getZ(), -60)) {
                            if (interval(46, pendingRotation.getY(), 60)) {
                                mTextView.setText("RIGHT CHEST");
                                mTextView.setBackgroundColor(Color.CYAN);
                            } else if (interval(20, pendingRotation.getY(), 45)) {
                                mTextView.setText("LEFT CHEST ");
                                mTextView.setBackgroundColor(Color.MAGENTA);
                            } else {
                                mTextView.setText("KNOCK");
                            }
                        } else if (interval(60, pendingRotation.getZ(), 120)) {
                            if (interval(46, pendingRotation.getY(), 60)) {
                                mTextView.setText("LEFT CHEST");
                                mTextView.setBackgroundColor(Color.MAGENTA);
                            } else if (interval(20, pendingRotation.getY(), 45)) {
                                mTextView.setText("RIGHT CHEST");
                                mTextView.setBackgroundColor(Color.CYAN);
                            } else {
                                mTextView.setText("KNOCK");
                            }
                        } else {
                            mTextView.setText("KNOCK");
                            mTextView.setBackgroundColor(Color.YELLOW);
                        }

                    } else {
                        mTextView.setText("KNOCK");
                    }
                    ps.print(mTextView.getText());
                    if(++trial != TRIALS) {
                        ps.print(",");
                    } else {
                        ps.println();
                        this.finish();
                    }
                    recognised = true;
                    //                try {
                    //                    mRawView.setText(String.valueOf(classifier.classifyInstance(di)));
                    //                } catch (Exception e) {
                    //                    e.printStackTrace();
                    //                }
                } else if (!recognised && absZ > 5) {
                    log.add(String.valueOf(absZ));
                    mTextView.setText("SHAKE");

                    mTextView.setBackgroundColor(Color.WHITE);
                    //                try {
                    //                    mRawView.setText(String.valueOf(classifier.classifyInstance(di)));
                    //                } catch (Exception e) {
                    //                    e.printStackTrace();
                    //                }
                } else if (recognised && absX < 1 && absY < 1 && absZ < 1) {
                    recognised = false;
                }
                if (log.size() == 10) {
                    log.poll();
                }
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            float[] rotMat = new float[9];
            float[] vals = new float[3];
            SensorManager.getRotationMatrixFromVector(rotMat, event.values);
            SensorManager.remapCoordinateSystem(rotMat,
                    SensorManager.AXIS_X, SensorManager.AXIS_Z, vals);
            SensorManager.getOrientation(rotMat, vals);

            // Optionally convert the result from radians to degrees
            vals[0] = (float) Math.toDegrees(vals[0]);
            vals[1] = (float) Math.toDegrees(vals[1]);
            vals[2] = (float) Math.toDegrees(vals[2]);
            dat = new AccelData(timestamp, vals[0], vals[1], vals[2]);
            pendingRotation = dat;
        }
        if (sensorData.size() > 50)
            sensorData.remove(0);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private enum Choice {
        LEFT("LEFT"), KNOCK("KNOCK"), RIGHT("RIGHT");

        private String name;

        Choice(String name){
            this.name = name;
        }

        @Override
        public String toString(){
            return name;
        }
    }
}
