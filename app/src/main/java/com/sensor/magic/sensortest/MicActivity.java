package com.sensor.magic.sensortest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MicActivity extends Activity implements SensorEventListener {

    private ArrayList<AccelData> sensorData = new ArrayList<>();
    private CSampler sampler;
    private LinearLayout layout;
    private FileWriter fw;
    private TextView tv;
    private TextView tv2;

    private SensorManager mSensorManager;
    private Sensor mGyro;
    private Sensor mAccel;
    private boolean useAccel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mic);
        //mdrawer = (CDrawer) findViewById(R.id.drawer);
        layout = (LinearLayout) findViewById(R.id.mic_layout);
        tv = (TextView) findViewById(R.id.mic_text);
        tv2 = (TextView) findViewById(R.id.mic_text2);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String mode = extras.getString("MODE");
            switch (mode) {
                case "RAISE":
                    useAccel = true;
                    break;
                case "ROTATE":
                    useAccel = false;
                    break;
            }
            //The key argument here must match that used in the other activity
        }

        File f = new File(getApplicationContext().getFilesDir().getPath() + "micdata.txt");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            fw = new FileWriter(f);
            //mdrawer.setFileWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            run();
            System.out.println("mDrawThread NOT NULL");
            System.out.println("recorder NOT NULL");
            return;
        }
    }

    /**
     * Pause the visualizer when the app is paused
     */
    @Override
    protected void onPause() {
        System.out.println("onpause");
        sampler.SetRun(Boolean.valueOf(false));
        //mDrawThread.setRun(Boolean.valueOf(false));
        sampler.SetSleeping(Boolean.valueOf(true));
        //mDrawThread.SetSleeping(Boolean.valueOf(true));
        Boolean.valueOf(false);
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    /**
     * Resters the visualizer when the app restarts
     */
    @Override
    protected void onRestart() {
        System.out.println("onRestart");
        super.onRestart();
    }

    /**
     * Resume the visualizer when the app resumes
     */
    @Override
    protected void onResume() {
        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_UI);
        System.out.println("onresume");
        int i = 0;
        while (true) {
            if ((sampler.GetDead2().booleanValue())) {
                //System.out.println(sampler.GetDead2() + ", " + mdrawer.GetDead2());
                sampler.Restart();
//                if (!m_bStart.booleanValue())
//                    mdrawer.Restart(Boolean.valueOf(true));
                sampler.SetSleeping(Boolean.valueOf(false));
                //mDrawThread.SetSleeping(Boolean.valueOf(false));
                super.onResume();
                return;
            }
            try {
                Thread.sleep(500L);
                System.out.println("Hang on..");
                i++;
                if (!sampler.GetDead2().booleanValue())
                    System.out.println("sampler not DEAD!!!");
//                if (!mdrawer.GetDead2().booleanValue()) {
//                    System.out.println("mDrawer not DeAD!!");
//                    mdrawer.SetRun(Boolean.valueOf(false));
//                }
                if (i <= 4)
                    continue;
                //mDrawThread.SetDead2(Boolean.valueOf(true));
            } catch (InterruptedException localInterruptedException) {
                localInterruptedException.printStackTrace();
            }
        }

    }

    @Override
    protected void onStart() {
        System.out.println("onstart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        System.out.println("onstop");
        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStop();
    }


    /**
     * Recives the buffert from the sampler
     */
    public void setBuffer(short[] paramArrayOfShort) {
//        mDrawThread = mdrawer.getThread();
//        mDrawThread.setBuffer(paramArrayOfShort);
    }

    /**
     * Called by OnCreate to get everything up and running
     */
    public void run() {
        try {
//            if (mDrawThread == null) {
//                mDrawThread = mdrawer.getThread();
//            }
            if (sampler == null)
                sampler = new CSampler(this);
            Context localContext = getApplicationContext();
            Display localDisplay = getWindowManager().getDefaultDisplay();
            Toast localToast = Toast.makeText(localContext, "Please make some noise..", Toast.LENGTH_LONG);
            localToast.setGravity(48, 0, localDisplay.getHeight() / 8);
            localToast.show();
            //mdrawer.setOnClickListener(listener);
            if (sampler != null) {
                sampler.Init();
                sampler.StartRecording();
                sampler.StartSampling();
            }


        } catch (NullPointerException e) {
            Log.e("Main_Run", "NullPointer: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: 03/02/2017 use this to live log the density data to see what is up. 

    boolean tilt = false;
    int threshold = 0;

    boolean tilted = false;

    float position = 0;
    int posCount = 0;
    boolean set = false;
    boolean ready = false;
    double fixY = 0;
    boolean up = false;

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        long timestamp = System.currentTimeMillis();

        if(!set) {
            fixY = tv.getTextSize();
            set = true;
        }

        Sensor sens = event.sensor;

        if(sens.getType() == Sensor.TYPE_LINEAR_ACCELERATION && tilted && useAccel) {
            if(Math.abs(y) > 2 && ready) {
                position += y;

                tv.setBackgroundColor(Color.YELLOW);
                if(y > 0 && !up ) {
                    //Log.e("ACCEL", "Y: "+y);
                    fixY = fixY-y;
                    tv.setText(String.valueOf(fixY));
                    ready = false;
                    up = true;
                    posCount = 0;
                } else if(y < 0 && up){
                    //Log.e("ACCEL", "Y: "+y);
                    fixY = fixY-y;
                    tv.setText(String.valueOf(fixY));
                    ready = false;
                    up = false;
                    posCount = 0;
                }

                //tv.setText(z > 0 ? "UP" : "DOWN");
            } else {
                posCount++;
                if (!ready){
                    Log.e("ACCEL", "Yyyyyyyyyyyyyyyyyyyyyyyyyy");
                    if(posCount > 10){
                        ready = true;
                        Log.e("ACCEL", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa    "+posCount);
                    }
                }

                if (posCount > 100) {
                    posCount = 0;
                    position = 0;
                    tilted = false;
                    set = false;
                    tv.setBackgroundColor(Color.RED);
                    tv2.setBackgroundColor(Color.RED);
                }
            }
        } else if(sens.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR && !tilted) {
            float[] rotMat = new float[9];
            float[] vals = new float[3];

            if ((Math.abs(x) > 0.05 || Math.abs(y) > 0.05 || Math.abs(z) > 0.05)) {
                SensorManager.getRotationMatrixFromVector(rotMat, event.values);
                SensorManager.remapCoordinateSystem(rotMat,
                        SensorManager.AXIS_X, SensorManager.AXIS_Z, vals);
                SensorManager.getOrientation(rotMat, vals);

                // Optionally convert the result from radians to degrees
                vals[0] = (float) Math.toDegrees(vals[0]);
                vals[1] = (float) Math.toDegrees(vals[1]);
                vals[2] = (float) Math.toDegrees(vals[2]);
                final AccelData dat = new AccelData(timestamp, vals[0], vals[1], vals[2]);
                sensorData.add(dat);
                if (sampler.getScratch()) {
                    tv2.setText("SOUND");
                    tv2.setBackgroundColor(Color.MAGENTA);
                }
                if (sensorData.size() > 9) {
                    AccelData prev = sensorData.get(sensorData.size() - 10);
                    //Attempt to eliminate non-knock movements
                    double absZ = Math.abs(vals[2] - prev.getZ());
                    //tv.setText("LOL: " + Math.round(absZ));
                    if (!tilt && absZ > 20 && absZ < 30) {
                        Log.d("AAAAA", "GOT FIRST");
                        tilt = true;
                    } else if (tilt && absZ < 5 && ++threshold > 5) {
                        tv.setText("TILT");
                        if (sampler.getScratch()) {
                            Log.d("CCCCCC", "GOT THIRD");
                            tv.setText("TOUCH");
                            tv.setBackgroundColor(Color.BLUE);
                            tv2.setText("TOUCH");
                            tv2.setBackgroundColor(Color.BLUE);
                            tilt = false;
                            tilted = true;
                            fixY = y;
                            threshold = 0;
                            sampler.toggleScratch();
                        }
                    } else if (tilt && absZ > 5) {
                        tv.setText("NOPE");
                        tv.setBackgroundColor(Color.RED);
                        if (!sampler.getScratch()) {
                            tv2.setText("NOPE");
                            tv2.setBackgroundColor(Color.RED);
                            tilt = false;
                        } else {
                            //sampler.toggleScratch();
                        }
                    }
                }

                if (sensorData.size() > 15) {
                    sensorData.remove(0);
                }
            }
        } else if (sens.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR && tilted && !useAccel){
            float[] rotMat = new float[9];
            float[] vals = new float[3];

            if ((Math.abs(x) > 0.05 || Math.abs(y) > 0.05 || Math.abs(z) > 0.05)) {
                SensorManager.getRotationMatrixFromVector(rotMat, event.values);
                SensorManager.remapCoordinateSystem(rotMat,
                        SensorManager.AXIS_X, SensorManager.AXIS_Z, vals);
                SensorManager.getOrientation(rotMat, vals);

                // Optionally convert the result from radians to degrees
                vals[0] = (float) Math.toDegrees(vals[0]);
                vals[1] = (float) Math.toDegrees(vals[1]);
                vals[2] = (float) Math.toDegrees(vals[2]);
                final AccelData dat = new AccelData(timestamp, vals[0], vals[1], vals[2]);
                sensorData.add(dat);
                if(sensorData.size() > 3) {
                    AccelData prev = sensorData.get(sensorData.size() - 3);
                    //Attempt to eliminate non-knock movements
                    double absY = Math.abs(vals[1] - prev.getY());
                        tv.setText(String.valueOf((int)(fixY-vals[1])));


                    if(absY < 1){
                        if(++posCount > 100){
                            tv.setBackgroundColor(Color.RED);
                            tv2.setBackgroundColor(Color.RED);
                            tilted = false;
                            posCount = 0;
                            fixY = tv.getTextSize();
                        }
                    } else {
                        posCount = 0;
                    }

                    if(sensorData.size() > 30){
                        sensorData.remove(0);
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
