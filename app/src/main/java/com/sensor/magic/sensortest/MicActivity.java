package com.sensor.magic.sensortest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

public class MicActivity extends Activity implements SensorEventListener {

    private ArrayList<SensorData> sensorData = new ArrayList<>();
    private MicrophoneListener sampler;
    private TextView tv;
    private TextView tv2;

    private Random rand = new Random();
    private int target;

    private SensorManager mSensorManager;
    private Sensor mGyro;
    private Sensor mAccel;
    private boolean useAccel;
    private boolean lefty;
    private boolean test;
    private Vibrator v;
    private PrintStream ps;
    private int TRIALS = 10;
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

            if(useAccel)
                TRIALS = 5;

            String hand = extras.getString("HAND");
            switch (hand) {
                case "LEFTY":
                    lefty = true;
                    break;
                case "RIGHTY":
                    lefty = false;
                    break;
            }

            test = extras.getBoolean("TEST");
        }

        if(!test) {
            File f = new File(getApplicationContext().getExternalFilesDir(null), (useAccel ? "raiseTest" : "rotateTest") + "File.csv");
            try {
                if (!f.exists()) {
                    f.createNewFile();
                }
                ps = new PrintStream(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        while (true) {
            run();
            return;
        }
    }

    /**
     * Pause the audio thread when the app is paused
     */
    @Override
    protected void onPause() {
        sampler.setRun(Boolean.FALSE);
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    /**
     * Restarts the audio thread when the app restarts
     */
    @Override
    protected void onRestart() {
        super.onRestart();
    }

    /**
     * Resume the audio thread when the app resumes
     */
    @Override
    protected void onResume() {
        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_UI);
        while (true) {
            if ((sampler.getDead())) {
                sampler.restart();
                super.onResume();
                return;
            }
            try {
                Thread.sleep(500L);
            } catch (InterruptedException localInterruptedException) {
                localInterruptedException.printStackTrace();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        if(!test)
            ps.close();
        super.onStop();
    }

    /**
     * Called by onCreate to get everything up and running
     */
    public void run() {
        try {
            if (sampler == null)
                sampler = new MicrophoneListener();

            sampler.init();
            sampler.startRecording();
            sampler.startSampling();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
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

        //This option is triggered when the Motion mode is enabled 
        //is concerned with processing acceleration data
        if(sens.getType() == Sensor.TYPE_LINEAR_ACCELERATION ){
            accelVal = Math.abs(z) > Math.abs(accelVal) ? z: accelVal;
            if(tilted && useAccel) {
                double absX = Math.abs(x);
                double absY = Math.abs(y);
                double absZ = Math.abs(z);
                //check for significant motion
                if (absX > 4 || absY > 4 || absZ > 4) {
                    //checks for the prevalence of motion in any direction
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
                    //insignificant motion triggers timer - once reached, the gesture is confirmed
                    if (++threshold == 200) {
                        tv2.setText(current != null ? current.toString() : "null");
                        boolean currentMatch = targetDir.get(index).equals(current);
                        threshold = 0;
                        if(!test) {
                            ps.print(String.valueOf(System.currentTimeMillis() - timer));
                            ps.print(", ");
                            ps.print(String.valueOf(current == null ? "null" : current.toString()));
                            ps.print(", ");
                            ps.print(String.valueOf(targetDir.get(index).toString()));
                            ps.print(", ");
                            ps.print(String.valueOf(currentMatch));
                            ps.print(", ");
                            ps.flush();
                        }
                        overall = overall && currentMatch;
                        v.vibrate(200);
                        if(currentMatch) {
                            tv.setBackgroundColor(Color.rgb(152, 251, 152));
                            if (++index != targetDir.size()) {
                                tv.setText(targetDir.get(index).toString());
                            }
                            if (index == targetDir.size()) {
                                tv.setText(R.string.idle);
                                tv2.setText(R.string.idle);
                                tv.setBackgroundColor(Color.GREEN);
                                tv2.setBackgroundColor(Color.GREEN);
                                if(!test) {
                                    ps.print(String.valueOf(overall));
                                    ps.println();
                                    ps.flush();
                                }
                                tilted = false;
                                index = 0;
                                overall = true;
                                if(++trial == TRIALS && !test){
                                    this.finish();
                                }
                            }
                        } else {
                            tv.setBackgroundColor(Color.rgb(255,204,203));
                            if (++index != targetDir.size()) {
                                tv.setText(targetDir.get(index).toString());
                            }
                            if (index == targetDir.size()) {
                                tv.setText(R.string.idle);
                                tv2.setText(R.string.idle);
                                tv.setBackgroundColor(Color.RED);
                                tv2.setBackgroundColor(Color.RED);
                                if(!test) {
                                    ps.print(String.valueOf(overall));
                                    ps.println();
                                    ps.flush();
                                }
                                tilted = false;
                                index = 0;
                                overall = true;
                                if(++trial == TRIALS && !test){
                                    this.finish();
                                }
                            }
                        }
                    }
                }
            }
            // This part is responsible for WristButton detection - once triggered, disables and
            // awaits for completion of either motion or rotation modes
        } else if(sens.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR && !tilted) {
            float[] rotMat = new float[9];
            float[] vals = new float[3];

            if ((Math.abs(x) > 0.05 || Math.abs(y) > 0.05 || Math.abs(z) > 0.05)) {
                SensorManager.getRotationMatrixFromVector(rotMat, event.values);
                SensorManager.remapCoordinateSystem(rotMat,
                        SensorManager.AXIS_X, SensorManager.AXIS_Z, vals);
                SensorManager.getOrientation(rotMat, vals);

                // Converting the result from radians to degrees
                vals[0] = (float) Math.toDegrees(vals[0]);
                vals[1] = (float) Math.toDegrees(vals[1]);
                vals[2] = (float) Math.toDegrees(vals[2]);
                final SensorData dat = new SensorData(timestamp, vals[0], vals[1], vals[2]);
                sensorData.add(dat);
                //if sound flag is triggered
                if (sampler.getScratch()) {
                    tv2.setText(R.string.sound_flag);
                    tv2.setBackgroundColor(Color.MAGENTA);
                }
                if (sensorData.size() > 10) {
                    SensorData prev = sensorData.get(sensorData.size() - 10);
                    // Attempt to eliminate non-knock movements
                    double absZ = lefty ? - (vals[2] - prev.getZ()) : vals[2] - prev.getZ();
                    // recognising wrist tilt
                    if (!tilt && absZ > 20 && absZ < 30) {
                        tv.setText(R.string.tilt_flag);
                        if (Math.abs(accelVal) <= 5) {
                            tilt = true;
                        } else {
                            accelVal = 0;
                        }
                    // if dual flag is enabled, trigger WristButton
                    } else if (tilt && sampler.getScratch()) {
                        timer = System.currentTimeMillis();
                        tv.setText(R.string.dual_flag);
                        tv.setBackgroundColor(Color.BLUE);
                        tv2.setText(R.string.dual_flag);
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
                    // if dual flag wasn't received in a short interval, disable singular flag
                    } else if (tilt && absZ > 5 && ++threshold > 20) {
                        tv.setText(R.string.idle);
                        tv.setBackgroundColor(Color.RED);
                        if (!sampler.getScratch()) {
                            tv2.setText(R.string.idle);
                            tv2.setBackgroundColor(Color.RED);
                            tilt = false;
                        }
                    } else if (!tilt && absZ < 5 && ++threshold > 20){
                        tv.setText(R.string.idle);
                        tv.setBackgroundColor(Color.RED);
                        if(sampler.getScratch()){
                            sampler.toggleScratch();
                            tv2.setText(R.string.idle);
                            tv2.setBackgroundColor(Color.RED);
                            threshold = 0;
                        }
                    }
                }

                //maintaining fixed buffer size
                if (sensorData.size() > 15) {
                    sensorData.remove(0);
                }
            }
        // managing rotation mode after WristButton triggering
        } else if (sens.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR && tilted && !useAccel){
            float[] rotMat = new float[9];
            float[] vals = new float[3];

            if ((Math.abs(x) > 0.05 || Math.abs(y) > 0.05 || Math.abs(z) > 0.05)) {
                SensorManager.getRotationMatrixFromVector(rotMat, event.values);
                SensorManager.remapCoordinateSystem(rotMat,
                        SensorManager.AXIS_X, SensorManager.AXIS_Z, vals);
                SensorManager.getOrientation(rotMat, vals);

                // Convert the result from radians to degrees
                vals[0] = (float) Math.toDegrees(vals[0]);
                vals[1] = (float) Math.toDegrees(vals[1]);
                vals[2] = (float) Math.toDegrees(vals[2]);
                final SensorData dat = new SensorData(timestamp, vals[0], vals[1], vals[2]);
                sensorData.add(dat);
                if(sensorData.size() > 3) {
                    SensorData prev = sensorData.get(sensorData.size() - 3);
                    double absY = Math.abs(vals[1] - prev.getY());
                    int current = (int) (fixY - (int)vals[1])/3;
                    tv2.setText(String.valueOf(current));

                    if(absY < 1){
                        if(++threshold > 100){
                            if(!test) {
                                ps.print(String.valueOf(System.currentTimeMillis() - timer));
                                ps.print(", ");
                                ps.print(String.valueOf(current == target));
                                ps.print(", ");
                                ps.print(String.valueOf(current));
                                ps.print(", ");
                                ps.print(String.valueOf(target));
                                ps.println();
                                ps.flush();
                            }
                            if(current == target) {
                                tv.setBackgroundColor(Color.GREEN);
                                tv2.setBackgroundColor(Color.GREEN);
                            } else {
                                tv.setBackgroundColor(Color.RED);
                                tv2.setBackgroundColor(Color.RED);
                            }
                            v.vibrate(200);
                            tilted = false;
                            threshold = 0;
                            fixY = y;
                            if(++trial == TRIALS && !test){
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

    /**
     * Generate random number within a limit
     * @return random integer
     */
    private int getRandom(){
        int base = rand.nextInt(10);
        boolean sign = rand.nextBoolean();
        if(sign)
            base = -base;
        return base;
    }

    /**
     * Generate random list of directions
     * @return an ArrayList of directions
     */
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
