package com.sensor.magic.sensortest;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class KnockActivity extends WearableActivity implements SensorEventListener {

    private ArrayList<SensorData> sensorData = new ArrayList<>();
    private TextView mTextView;
    private SensorManager mSensorManager;
    private Sensor mAccel;
    private Sensor mGyro;
    private boolean recognised;
    private SensorData pendingRotation = null;
    private PrintStream ps;
    private boolean test;
    private boolean lefty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knock);
        setAmbientEnabled();

        mTextView = (TextView) findViewById(R.id.knock_text);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            test = extras.getBoolean("TEST");
            lefty = extras.getString("HAND").equals("LEFTY");
        }

        if(!test) {
            File f = new File(getApplicationContext().getExternalFilesDir(null), "knockTestFile.csv");
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
        if(!test) {
            ps.close();
        }
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

    /**
     * Checks if the number falls within the interval specified
     * @param less lower bound
     * @param val value to check
     * @param more upper bound
     * @return true if the value is in between
     */
    private boolean interval(double less, double val, double more) {
        return val > less && val < more;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        long timestamp = System.currentTimeMillis();
        SensorData dat = new SensorData(timestamp, x, y, z);
        sensorData.add(dat);
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            if (sensorData.size() > 1) {
                SensorData prev = sensorData.get(sensorData.size() - 2);

                //absolute difference between adjacent recordings
                double absX = Math.abs(x - prev.getX());
                double absY = Math.abs(y - prev.getY());
                double absZ = Math.abs(z - prev.getZ());

                if (!recognised && absZ > 30 ) {
                    if (pendingRotation != null) {
                        if (lefty && interval(-120, pendingRotation.getZ(), -60)) {
                            if (interval(46, pendingRotation.getY(), 60)) {
                                mTextView.setText("RIGHT CHEST");
                                mTextView.setBackgroundColor(Color.CYAN);
                            } else if (interval(20, pendingRotation.getY(), 45)) {
                                mTextView.setText("LEFT CHEST ");
                                mTextView.setBackgroundColor(Color.MAGENTA);
                            } else {
                                mTextView.setText("KNOCK");
                            }
                        } else if (!lefty && interval(60, pendingRotation.getZ(), 120)) {
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
                        mTextView.setBackgroundColor(Color.YELLOW);
                    }
                    if(!test) {
                        ps.print(mTextView.getText() + " ");
                    }
                    recognised = true;
                } else if (!recognised && absZ > 5) {
                    mTextView.setText("SHAKE");
                    mTextView.setBackgroundColor(Color.WHITE);
                } else if (recognised && absX < 1 && absY < 1 && absZ < 1) {
                    //resetting if the hand is stable
                    recognised = false;
                }
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            float[] rotMat = new float[9];
            float[] vals = new float[3];

            //Converting rotational values to degrees
            SensorManager.getRotationMatrixFromVector(rotMat, event.values);
            SensorManager.remapCoordinateSystem(rotMat,
                    SensorManager.AXIS_X, SensorManager.AXIS_Z, vals);
            SensorManager.getOrientation(rotMat, vals);

            vals[0] = (float) Math.toDegrees(vals[0]);
            vals[1] = (float) Math.toDegrees(vals[1]);
            vals[2] = (float) Math.toDegrees(vals[2]);
            dat = new SensorData(timestamp, vals[0], vals[1], vals[2]);
            pendingRotation = dat;
        }

        //Maintaining the constant array size
        if (sensorData.size() > 50)
            sensorData.remove(0);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
