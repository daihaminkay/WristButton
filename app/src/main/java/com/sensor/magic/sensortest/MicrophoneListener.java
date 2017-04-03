package com.sensor.magic.sensortest;

import android.media.AudioRecord;
import android.util.Log;

class MicrophoneListener {
    private static final int SAMPPERSEC = 16000;
    private static short[] buffer;
    private AudioRecord ar;
    private int buffersizebytes;
    private Boolean dead = Boolean.FALSE;
    private Boolean dead2 = Boolean.TRUE;
    private Boolean run;
    private Thread recordingThread;

    MicrophoneListener() {
        run = Boolean.FALSE;
    }

    Boolean getDead() {
        return dead2;
    }


    /**
     * Prepares to collect audiodata and instantiates AudioRecord
     * @throws Exception if initialisation failed
     */
    void init() throws Exception {
        int audioEncoding = 2;
        int channelConfiguration = 12;
        try {
            if (!run) {
                ar = new AudioRecord(1, SAMPPERSEC, channelConfiguration, audioEncoding, AudioRecord.getMinBufferSize(SAMPPERSEC, channelConfiguration, audioEncoding));
                if (ar.getState() != 1)
                    return;
            }
        } catch (Exception e) {
            throw new Exception();
        }
        while (true) {
            buffersizebytes = AudioRecord.getMinBufferSize(SAMPPERSEC, channelConfiguration, audioEncoding);
            buffer = new short[buffersizebytes];
            run = Boolean.TRUE;
            return;
        }
    }

    /**
     * Restarts the audio thread
     */
    void restart() {
        while (true) {
            if (dead2) {
                dead2 = Boolean.FALSE;
                if (dead) {
                    dead = Boolean.FALSE;
                    ar.stop();
                    ar.release();
                    try {
                        init();
                    } catch (Exception e) {
                        return;
                    }
                    startRecording();
                    startSampling();
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
     * Reads the data-buffers and makes audio data accessible
     */
    private void sample() {
        int mSamplesRead = ar.read(buffer, 0, buffersizebytes);
        short[] nbuff = new short[buffer.length];
        double RC = 1.0/(12000*2*3.14);
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
    private boolean scratch = false;

    /**
     * Toggle scratching flag from outside
     */
    void toggleScratch(){
        scratch = false;
    }

    /**
     * Retrieve scratch flag
     * @return true if scratch was registered
     */
    boolean getScratch(){
        return scratch;
    }

    boolean beforePeak = true;

    /**
     * Performs heuristic audio data analysis
     * @param buffer sample buffer
     * @return
     */
    private String bufferPrint(short[] buffer){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        short prev = buffer[0];
        int count = 0;

        //count the filtered needed samples in a buffer
        for(short s : buffer){
            int abs = Math.abs(s-prev);
            if( Math.abs(s) >= 20 && Math.abs(s) <= 150 && abs < 80 && abs > 10 ) {
                count++;
            }
            prev = s;
        }

        //density of the matched samples in the buffer
        double density = (count * 100.0) / buffer.length;

        if(Math.abs(density-toSee) < 20){
            if(density > toSee){
                if(rising) {
                    //Peak
                    if (density > 15 && density < 30 && !beforePeak) {
                        toSee = density;
                        sb.append(" ??? "+density+" ??? ");
                        scratch = true;
                        rising = true;
                        toSee = 1;
                    } else {
                        toSee = density;
                        sb.append(density);
                        beforePeak = false;
                    }
                } else {
                    toSee = 1;
                    rising = true;
                }
            } else if (density < toSee && density > 0.0){
                if(!rising) {
                    toSee = density;
                    sb.append(density);
                    if (density < 9 && toSee != 1 && !beforePeak) {
                        sb.append(" !!! "+density+" !!! ");
                        Log.e("LOL", "SCRATCH");
                        scratch = true;
                        rising = true;
                        toSee = 1;
                    }
                } else {
                    rising = true;
                    toSee = 1;
                }
            } else {
                toSee = 1;
                rising = true;
            }
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Displays running flag and manages data recording
     * @param paramBoolean to  run or not
     */
    void setRun(Boolean paramBoolean) {
        run = paramBoolean;
        if (run)
            startRecording();
        while (true) {
            StopRecording();
            return;
        }
    }

    /**
     * Starts audio recording
     */
    void startRecording() {
        if (ar == null) {
            try {
                init();
            } catch (Exception e) {
                e.printStackTrace();
            }
            startRecording();
        } else {
            ar.startRecording();
        }

    }

    /**
     * Collects audiodata and sends it back to the main activity
     */
    void startSampling() {
        recordingThread = new Thread() {
            public void run() {
                while (true) {
                    if (!run) {
                        dead = Boolean.TRUE;
                        dead2 = Boolean.TRUE;
                        return;
                    }
                    sample();
                }
            }
        };
        recordingThread.start();
    }

    /**
     * Stops data recording
     */
    private void StopRecording() {
        ar.stop();
    }
}
