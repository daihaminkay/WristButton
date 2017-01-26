package com.sensor.magic.sensortest;

/**
 * This is the sampler for the visualizer
 * This collects the data the will be visualized
 * http://www.dreamincode.net/forums/topic/303235-visualizing-sound-from-the-microphone/
 *
 * @author Pontus Holmberg (EndLessMind)
 * Email: the_mr_hb@hotmail.com
 **/

import android.media.AudioRecord;
import android.util.Log;

public class CSampler {
    private static final int SAMPPERSEC = 16000;
    private static short[] buffer;
    private AudioRecord ar;
    private int audioEncoding = 2;
    private int buffersizebytes;
    private int buflen;
    private int channelConfiguration = 16;
    private int mSamplesRead;
    private Boolean m_bDead = Boolean.valueOf(false);
    private Boolean m_bDead2 = Boolean.valueOf(true);
    private Boolean m_bRun;
    private Boolean m_bSleep = Boolean.valueOf(false);
    private MicActivity m_ma;
    private Thread recordingThread;
    private boolean recording;

    public CSampler(MicActivity paramMainActivity) {
        m_ma = paramMainActivity;
        m_bRun = Boolean.valueOf(false);
    }

    public Boolean GetDead2() {
        return m_bDead2;
    }

    public Boolean GetSleep() {
        return m_bSleep;
    }

    /**
     * Prepares to collect audiodata.
     * @throws Exception
     */
    public void Init() throws Exception {
        try {
            if (!m_bRun) {
                ar = new AudioRecord(1, SAMPPERSEC, channelConfiguration, audioEncoding, AudioRecord.getMinBufferSize(SAMPPERSEC, channelConfiguration, audioEncoding));
                if (ar.getState() != 1)
                    return;
                System.out.println("State initialized");
            }
        } catch (Exception e) {
            Log.d("TE", e.getMessage());
            throw new Exception();
        }
        while (true) {
            buffersizebytes = AudioRecord.getMinBufferSize(SAMPPERSEC, channelConfiguration, audioEncoding);
            buffer = new short[buffersizebytes];
            m_bRun = Boolean.valueOf(true);
            System.out.println("State unitialized!!!");
            return;
        }
    }

    /**
     * Restarts the thread
     */
    public void Restart() {
        while (true) {
            if (m_bDead2.booleanValue()) {
                m_bDead2 = Boolean.valueOf(false);
                if (m_bDead.booleanValue()) {
                    m_bDead = Boolean.valueOf(false);
                    ar.stop();
                    ar.release();
                    try {
                        Init();
                    } catch (Exception e) {
                        return;
                    }
                    StartRecording();
                    StartSampling();
                }
                return;
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException localInterruptedException) {
                localInterruptedException.printStackTrace();
            }
        }
    }

    /**
     * Reads the data-bufferts
     */
    private final int bound = 8;
    private int block = 0;
    public void Sample() {
        mSamplesRead = ar.read(buffer, 0, buffersizebytes);
        short[] nbuff = new short[buffer.length];
        // TODO: 19/01/17 Play around with the 'a' value
        double RC = 1.0/(10000*2*3.14);
        double dt = 1.0/16000;
        double a = RC/(RC + dt);
        nbuff[0] = buffer[0];
        for(int i = 1; i < buffer.length; i++){
            nbuff[i] = (short) (a * nbuff[i-1] + a * (buffer[i] - buffer[i-1]));
        }
        buffer = nbuff;
        if(nbuff.length != 0)
            Log.d("BUFF"+a, bufferPrint(nbuff));
    }

    private double toSee = 1;
    private boolean rising = true;

    private String bufferPrint(short[] buffer){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        short prev = buffer[0];
        int count = 0;
        boolean shouldBeNegative = false;
        for(short s : buffer){
            int abs = Math.abs(s-prev);
            if( Math.abs(s) >= 10 && Math.abs(s) <= 50 && abs < 100 && abs > 20 ) {
                //if(s > 0 && !shouldBeNegative){
//                    sb.append(s);
//                    sb.append(" ");
                    shouldBeNegative = true;
//                } else if (s < 0 && shouldBeNegative){
//                    sb.append(s);
//                    sb.append(" ");
//                    shouldBeNegative = false;
//                }
//                sb.append(prev);
//                sb.append("~");
                count++;
            }
            prev = s;
        }
        double density = (count * 100.0) / buffer.length;
        // TODO: 26/01/2017 NEED TO SEE THE "HILL" - density rises up to ~30% and then drops smoothly
        if(density > toSee){
            if(rising) {
                toSee = density;
                block++;
                sb.append(density);

                if (toSee > 15 && toSee < 40) {
                    sb.append(" ??? "+density+" ??? ");
                    rising = false;
                }
            } else {
                toSee = 1;
            }
        } else if (density < toSee && density > 0.0){
            if(!rising) {
                toSee = density;
                block++;
                sb.append(density);
                if (density < 3) {
                    sb.append(" !!! "+density+" !!! ");
                    Log.e("LOL", "SCRATCH");
                    block = 0;
                    rising = true;
                    toSee = 1;
                }
            } else {
                rising = true;
                toSee = 1;
            }
        } else if(density == 0.0){
            block = 0;
            rising = true;
        }

        sb.append("]");
        return sb.toString();
    }


    public void SetRun(Boolean paramBoolean) {
        m_bRun = paramBoolean;
        if (m_bRun.booleanValue())
            StartRecording();
        while (true) {

            StopRecording();
            return;
        }
    }

    public void SetSleeping(Boolean paramBoolean) {
        m_bSleep = paramBoolean;
    }


    public void StartRecording() {
        if (ar == null) {
            try {
                Init();
            } catch (Exception e) {
                e.printStackTrace();
            }
            StartRecording();
        } else {
            recording = true;
            ar.startRecording();
        }

    }

    /**
     * Collects audiodata and sends it back to the main activity
     */
    public void StartSampling() {
        recordingThread = new Thread() {
            public void run() {
                while (true) {
                    if (!m_bRun.booleanValue()) {
                        m_bDead = Boolean.valueOf(true);
                        m_bDead2 = Boolean.valueOf(true);
                        return;
                    }
                    Sample();
                    m_ma.setBuffer(CSampler.buffer);
                }
            }
        };
        recordingThread.start();
    }

    public void StopRecording() {
        ar.stop();
        recording = false;
    }

    public short[] getBuffer() {
        return buffer;
    }

    public boolean getRecordingTrue() {
        return recording;
    }

}
