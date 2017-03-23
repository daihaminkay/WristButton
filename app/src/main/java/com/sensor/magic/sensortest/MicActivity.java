package com.sensor.magic.sensortest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

public class MicActivity extends Activity implements SensorEventListener {

    private ArrayList<AccelData> sensorData = new ArrayList<>();
    private CSampler sampler;
    private TextView tv;
    private TextView tv2;

    private Random rand = new Random();
    private int target;

    private SensorManager mSensorManager;
    private Sensor mGyro;
    private Sensor mAccel;
    private boolean useAccel;
    private boolean lefty;
    private Vibrator v;
    private PrintStream ps;
    private final int TRIALS = 10;
    private int trial = 0;

    private enum Direction {
        UP("UP"), DOWN("DOWN"), FORWARD("FORWARD"),
        BACKWARD("BACKWARD"), LEFT("LEFT"), RIGHT("RIGHT");

        private String name;

        Direction(String name){
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mic);
        tv = (TextView) findViewById(R.id.mic_text);
        tv2 = (TextView) findViewById(R.id.mic_text2);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

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

            String hand = extras.getString("HAND");
            switch (mode) {
                case "LEFTY":
                    lefty = true;
                    break;
                case "RIGHTY":
                    lefty = false;
                    break;
            }
            //The key argument here must match that used in the other activity
        }

        File f = new File(getApplicationContext().getExternalFilesDir(null), (useAccel ? "raiseTest" : "rotateTest")+"File.csv");
        try {
            if(!f.exists()){
                f.createNewFile();
            }
            ps = new PrintStream(f);
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
        sampler.SetSleeping(Boolean.valueOf(true));
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
                sampler.Restart();
                sampler.SetSleeping(Boolean.valueOf(false));
                super.onResume();
                return;
            }
            try {
                Thread.sleep(500L);
                System.out.println("Hang on..");
                i++;
                if (!sampler.GetDead2().booleanValue())
                    System.out.println("sampler not DEAD!!!");
                if (i <= 4)
                    continue;
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
        ps.close();
        super.onStop();
    }


    /**
     * Recives the buffert from the sampler
     */
    public void setBuffer(short[] paramArrayOfShort) {

    }

    /**
     * Called by OnCreate to get everything up and running
     */
    public void run() {
        try {
            if (sampler == null)
                sampler = new CSampler(this);
            Context localContext = getApplicationContext();
            Display localDisplay = getWindowManager().getDefaultDisplay();
            Toast localToast = Toast.makeText(localContext, "Please make some noise..", Toast.LENGTH_LONG);
            localToast.setGravity(48, 0, localDisplay.getHeight() / 8);
            localToast.show();
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
    double accelVal = 0;
    double fixY = 0;
    int index = 0;
    long timer = 0;
    ArrayList<Direction> targetDir;
    boolean overall = true;
    Direction current =null;


    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        long timestamp = System.currentTimeMillis();

        Sensor sens = event.sensor;

        if(sens.getType() == Sensor.TYPE_LINEAR_ACCELERATION ){
            accelVal = Math.abs(z) > Math.abs(accelVal) ? z: accelVal;
            if(tilted && useAccel) {
                double absX = Math.abs(x);
                double absY = Math.abs(y);
                double absZ = Math.abs(z);
                if (absX > 4 || absY > 4 || absZ > 4) {
                    if (absX > absY && absX > absZ) {
                        if (x > 4) {
                            current = lefty ? Direction.BACKWARD : Direction.FORWARD;
                            threshold = 0;
                        } else if (x < 2) {
                            current = lefty ? Direction.FORWARD : Direction.BACKWARD;
                            threshold = 0;
                        }
                    } else if (absY > absX && absY > absZ) {
                        if (y > 4) {
                            current = lefty ? Direction.RIGHT : Direction.LEFT;
                            threshold = 0;
                        }
                        if (y < 4) {
                            current = lefty ? Direction.LEFT : Direction.RIGHT;
                            threshold = 0;
                        }
                    } else if (absZ > absX && absZ > absY) {
                        if (z > 4) {
                            current = Direction.DOWN;
                            threshold = 0;
                        }
                        if (z < 4) {
                            current = Direction.UP;
                            threshold = 0;
                        }
                    }
                    tv2.setText(current.toString());
                } else {
                    Log.e("DATA", threshold + " : " + targetDir.get(index).toString() + " : " + (current == null ? "null" : current.toString()));
                    if (++threshold == 35) {
                        tv2.setText(current.toString());
                        boolean currentMatch = targetDir.get(index).equals(current);
                        threshold = 0;
                        ps.print(String.valueOf(System.currentTimeMillis() - timer));
                        ps.print(", ");
                        ps.print(String.valueOf(current.toString()));
                        ps.print(", ");
                        ps.print(String.valueOf(targetDir.get(index).toString()));
                        ps.print(", ");
                        ps.print(String.valueOf(currentMatch));
                        ps.print(", ");
                        ps.flush();
                        overall = overall && currentMatch;
                        v.vibrate(500);
                        if(currentMatch) {
                            tv.setBackgroundColor(Color.rgb(152, 251, 152));
                            if (++index != targetDir.size()) {
                                tv.setText(targetDir.get(index).toString());
                            }
                            if (index == targetDir.size()) {
                                tv.setText("NOPE");
                                tv2.setText("NOPE");
                                tv.setBackgroundColor(Color.GREEN);
                                tv2.setBackgroundColor(Color.GREEN);
                                ps.print(String.valueOf(overall));
                                ps.println();
                                ps.flush();
                                tilted = false;
                                index = 0;
                                overall = true;
                                if(++trial == TRIALS){
                                    this.finish();
                                }
                            }
                        } else {
                            tv.setBackgroundColor(Color.rgb(255,204,203));
                            if (++index != targetDir.size()) {
                                tv.setText(targetDir.get(index).toString());
                            }
                            if (index == targetDir.size()) {
                                tv.setText("NOPE");
                                tv2.setText("NOPE");
                                tv.setBackgroundColor(Color.RED);
                                tv2.setBackgroundColor(Color.RED);
                                ps.print(String.valueOf(overall));
                                ps.println();
                                ps.flush();
                                tilted = false;
                                index = 0;
                                overall = true;
                                if(++trial == TRIALS){
                                    this.finish();
                                }
                            }
                        }
                    }
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
                if (sensorData.size() > 5) {
                    AccelData prev = sensorData.get(sensorData.size() - 6);
                    //Attempt to eliminate non-knock movements
                    double absZ = vals[2] - prev.getZ();
                    //tv.setText("LOL: " + Math.round(absZ));
                    if (!tilt && absZ > 20 && absZ < 30) {
                        Log.d("AAAAA", "GOT FIRST: " + accelVal);
                        if (Math.abs(accelVal) <= 5) {
                            tilt = true;
                        } else {
                            accelVal = 0;
                        }
                    } else if (tilt && sampler.getScratch()) {
                        Log.d("CCCCCC", "GOT THIRD: " + getApplicationContext().getExternalFilesDir(null).getAbsolutePath());
                        timer = System.currentTimeMillis();
                        tv.setText("TOUCH");
                        tv.setBackgroundColor(Color.BLUE);
                        tv2.setText("TOUCH");
                        tv2.setBackgroundColor(Color.BLUE);
                        tilt = false;
                        tilted = true;
                        fixY = vals[1];
                        threshold = 0;
                        sampler.toggleScratch();

                        if(useAccel){
                            targetDir = getRandomDirs();
                            tv.setText(targetDir.get(0).toString());
                        } else {
                            target = getRandom();
                            tv.setText(String.valueOf(target));
                        }
                    } else if (tilt && absZ > 5 && ++threshold > 10) {
                        tv.setText("NOPE");
                        tv.setBackgroundColor(Color.RED);
                        if (!sampler.getScratch()) {
                            tv2.setText("NOPE");
                            tv2.setBackgroundColor(Color.RED);
                            tilt = false;
                        }
                    } else if (!tilt && absZ < 5 && ++threshold > 10){
                        tv.setText("NOPE");
                        tv.setBackgroundColor(Color.RED);
                        if(sampler.getScratch()){
                            sampler.toggleScratch();
                            tv2.setText("NOPE");
                            tv2.setBackgroundColor(Color.RED);
                            threshold = 0;
                        }
                    }
                }

                if (sensorData.size() > 5) {
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
                    double absY = Math.abs(vals[1] - prev.getY());
                    //if(absY > 3){
                    Log.e("HAHAHA", fixY + " : "+vals[1]);
                    int current = (int) (fixY - (int)vals[1])/3;
                        tv2.setText(String.valueOf(current));
                    //}


                    if(absY < 1){
                        if(++threshold > 30){
                            ps.print(String.valueOf(System.currentTimeMillis() - timer));
                            ps.print(", ");
                            ps.print(String.valueOf(current == target));
                            ps.print(", ");
                            ps.print(String.valueOf(current));
                            ps.print(", ");
                            ps.print(String.valueOf(target));
                            ps.println();
                            ps.flush();
                            if(current == target) {
                                tv.setBackgroundColor(Color.GREEN);
                                tv2.setBackgroundColor(Color.GREEN);
                            } else {
                                tv.setBackgroundColor(Color.RED);
                                tv2.setBackgroundColor(Color.RED);
                            }
                            v.vibrate(500);
                            tilted = false;
                            threshold = 0;
                            fixY = y;
                            if(++trial == TRIALS){
                                this.finish();
                            }
                        }
                    } else {
                        threshold = 0;
                    }

                    if(sensorData.size() > 30){
                        sensorData.remove(0);
                    }
                }
            }
        }
    }

    private int getRandom(){
        int base = rand.nextInt(10);
        boolean sign = rand.nextBoolean();
        if(sign)
            base = -base;
        return base;
    }

    private ArrayList<Direction> getRandomDirs(){
        ArrayList<Direction> output = new ArrayList<>();
        for(int i = 0; i < 6; i++){
            int var = rand.nextInt(6);
            switch(var){
                case 0:
                    output.add(Direction.UP);
                    break;
                case 1:
                    output.add(Direction.DOWN);
                    break;
                case 2:
                    output.add(Direction.FORWARD);
                    break;
                case 3:
                    output.add(Direction.BACKWARD);
                    break;
                case 4:
                    output.add(Direction.LEFT);
                    break;
                case 5:
                    output.add(Direction.RIGHT);
                    break;
            }
        }
        return output;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
