package com.sensor.magic.sensortest;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MicActivity extends Activity implements SensorEventListener {
//    private CDrawer.CDrawThread mDrawThread;
//    private CDrawer mdrawer;

    private ArrayList<AccelData> sensorData = new ArrayList<>();
    private boolean recognised;
    private View.OnClickListener listener;
    private Boolean m_bStart = Boolean.valueOf(false);
    private Boolean recording;
    private CSampler sampler;
    private RelativeLayout layout;
    private FileWriter fw;
    private TextView tv;
    private TextView tacc;

    private SensorManager mSensorManager;
    private Sensor mAccel;
    private boolean live = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mic);
        //mdrawer = (CDrawer) findViewById(R.id.drawer);
        layout = (RelativeLayout) findViewById(R.id.mic_layout);
        tv = (TextView) findViewById(R.id.mic_text);
        tacc = (TextView) findViewById(R.id.mic_raw_acc);
        tacc.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                live = !live;
                return false;
            }
        });
        m_bStart = Boolean.valueOf(false);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

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
            recording = Boolean.valueOf(false);
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
        m_bStart = Boolean.valueOf(true);
        System.out.println("onRestart");
        super.onRestart();
    }

    /**
     * Resume the visualizer when the app resumes
     */
    @Override
    protected void onResume() {
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_FASTEST);
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
                m_bStart = Boolean.valueOf(false);
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
            listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sampler.getRecordingTrue())
                        sampler.StopRecording();
                    else
                        sampler.StartRecording();
                }
            };
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        long timestamp = System.currentTimeMillis();
        AccelData dat = new AccelData(timestamp, x, y, z);
        sensorData.add(dat);
        if (sensorData.size() > 1) {
            AccelData prev = sensorData.get(sensorData.size() - 2);
            //Attempt to eliminate non-knock movements
            double absX = Math.abs(x - prev.getX());
            double absY = Math.abs(y - prev.getY());
            double absZ = Math.abs(z - prev.getZ());
            if (!recognised && absZ > 25) {
                sampler.StopRecording();
                recognised = true;
            } else if (recognised && absX < 1 && absY < 1 && absZ < 1) {
                recognised = false;
            }

            if (sensorData.size() > 30) {
                boolean negAcc = true;
                boolean posAcc = true;
                // TODO: 05/02/2017 Rewrite the loop to look for a ascending/descending pattern of negatives 
//                for(int i = sensorData.size()-1; i > sensorData.size() - 11; i--){
//                    if(sensorData.get(i).getX() > -1){
//                        negAcc = false;
//                    }
//                }
                int threshold = 10;
                boolean startDown = false;
                boolean startUp = false;
                for(int i = 0; i < sensorData.size(); i++){
                    double current = sensorData.get(i).getX();

                    if(startDown){
                        if(current < -4){
                            threshold--;
                        } else {
                            if(threshold > 0){
                                negAcc = false;
                                break;
                            }
                        }
                    }

                    if(startUp){
                        if(current > 4){
                            threshold--;
                        } else {
                            if(threshold > 0){
                                posAcc = false;
                                break;
                            }
                        }
                    }

                    if(!startUp && !startDown){
                        if(current < -4)
                            startDown = true;
                        else if(current > 4)
                            startUp = true;
                    }
                }

                if(sampler.getScratch()){
                    //Log.e("MIC", "GOT HERE");
                    if(negAcc){
                        tv.setText("DOWN SCRATCH");
                        sensorData.clear();
                    } else if(posAcc) {
                        tv.setText("UP SCRATCH");
                        sensorData.clear();
                    }
                    sampler.toggleScratch();
                } else if (Math.abs(x) > 4 || Math.abs(y) > 4 || Math.abs(z) > 4){
                    tv.setText("NOPE");
                    if(sampler.getScratch()){
                        tacc.setText(ViewRawActivity.anythingToString(sensorData));
                    }
                }
                if(!sensorData.isEmpty())
                    sensorData.remove(0);
            }
            if(live && (Math.abs(x) > 2 || Math.abs(y) > 2 || Math.abs(z) > 2)) {
                //tacc.setText(ViewRawActivity.anythingToString(sensorData));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
