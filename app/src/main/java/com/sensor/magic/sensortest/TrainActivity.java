package com.sensor.magic.sensortest;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMOreg;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class TrainActivity extends WearableActivity implements SensorEventListener {

    int toSkip = 8;
    int count = 0;
    private SensorManager mSensorManager;
    private Sensor mLight;
    private ArrayList<AccelData> sensorData = new ArrayList<>();
    private ArrayList<AccelData> fullSensorData = new ArrayList<>();
    private View mChart;
    private TextView info;
    private LinearLayout layout;
    private Classifier classifier;
    private ArrayList<Attribute> attrs;
    private Instances inst;
    private FileWriter fw;
    private FileWriter fw_raw;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);
        info = new TextView(getApplicationContext());
        layout = (LinearLayout) findViewById(R.id.train_container);
        classifier = new SMOreg(); // new LinearRegression(); //new ClassificationViaRegression();
        attrs = new ArrayList<>();
        attrs.add(new Attribute("X"));
        attrs.add(new Attribute("Y"));
        attrs.add(new Attribute("Z"));
        //attrs.add(new Attribute("Gesture"));
        inst = new Instances("XYZ", attrs, 1000);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        info.setText("LOL");
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        long timestamp = System.currentTimeMillis();
        final AccelData dat = new AccelData(timestamp, x, y, z);
        sensorData.add(dat);
        // TODO: 28/12/2016 Distinguished knock non-knock difference!! check the one-step difference - much larger jump in knocking case
        fullSensorData.add(dat);
        if (sensorData.size() > 1) {
            double prevX = sensorData.get(sensorData.size() - 2).getX();
            double prevZ = sensorData.get(sensorData.size() - 2).getZ();
            //Attempt to eliminate non-knock movements
            if (Math.abs(x - prevX) > 15 || Math.abs(z - prevZ) > 15) {

                // TODO: 22/12/2016 Able to learn from input, need to specify some exact gesture input
                DenseInstance di = new DenseInstance(3);
                di.setValue(0, x);
                di.setValue(1, y);
                di.setValue(2, z);

                inst.add(di);
//            Thread thread = new Thread() {
//                @Override
//                public void run() {
//                }
//            };
//            thread.start();
            }
        }
        if (sensorData.size() > 50)
            sensorData.remove(0);

        if (++count == toSkip) {
            openChart();
            count = 0;
        }

        // Do something with this sensor value.
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

    private synchronized void openChart() {
        if (sensorData != null || sensorData.size() > 0) {
            layout.removeView(mChart);
            long t = sensorData.get(0).getTimestamp();
            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

            XYSeries xSeries = new XYSeries("X");
            XYSeries ySeries = new XYSeries("Y");
            XYSeries zSeries = new XYSeries("Z");
//            XYSeries aSeries = new XYSeries("A");

            for (int i = 0; i < sensorData.size(); i++) {
                AccelData data = sensorData.get(i);
                xSeries.add(data.getTimestamp() - t, data.getX());
                ySeries.add(data.getTimestamp() - t, data.getY());
                zSeries.add(data.getTimestamp() - t, data.getZ());
//                aSeries.add(data.getTimestamp() - t, data.getA());
            }

            dataset.addSeries(xSeries);
            dataset.addSeries(ySeries);
            dataset.addSeries(zSeries);
//            dataset.addSeries(aSeries);

            XYSeriesRenderer xRenderer = new XYSeriesRenderer();
            xRenderer.setColor(Color.RED);
            xRenderer.setPointStyle(PointStyle.CIRCLE);
            xRenderer.setFillPoints(true);
            xRenderer.setLineWidth(1);
            xRenderer.setDisplayChartValues(false);

            XYSeriesRenderer yRenderer = new XYSeriesRenderer();
            yRenderer.setColor(Color.GREEN);
            yRenderer.setPointStyle(PointStyle.CIRCLE);
            yRenderer.setFillPoints(true);
            yRenderer.setLineWidth(1);
            yRenderer.setDisplayChartValues(false);

            XYSeriesRenderer zRenderer = new XYSeriesRenderer();
            zRenderer.setColor(Color.BLUE);
            zRenderer.setPointStyle(PointStyle.CIRCLE);
            zRenderer.setFillPoints(true);
            zRenderer.setLineWidth(1);
            zRenderer.setDisplayChartValues(false);

//            XYSeriesRenderer aRenderer = new XYSeriesRenderer();
//            aRenderer.setColor(Color.GRAY);
//            aRenderer.setPointStyle(PointStyle.CIRCLE);
//            aRenderer.setFillPoints(true);
//            aRenderer.setLineWidth(1);
//            aRenderer.setDisplayChartValues(false);


            XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
            multiRenderer.setXLabels(0);
            multiRenderer.setLabelsColor(Color.RED);
            multiRenderer.setChartTitle("t vs (x,y,z,a)");
            multiRenderer.setXTitle("Sensor Data");
            multiRenderer.setYTitle("Values of Acceleration & Audio");
            multiRenderer.setZoomButtonsVisible(true);
            for (int i = 0; i < sensorData.size(); i++) {

                multiRenderer.addXTextLabel(i + 1, ""
                        + (sensorData.get(i).getTimestamp() - t));
            }
            for (int i = 0; i < 12; i++) {
                multiRenderer.addYTextLabel(i + 1, "" + i);
            }

            multiRenderer.addSeriesRenderer(xRenderer);
            multiRenderer.addSeriesRenderer(yRenderer);
            multiRenderer.addSeriesRenderer(zRenderer);
            //multiRenderer.addSeriesRenderer(aRenderer);

            // Getting a reference to LinearLayout of the MainActivity Layout

            // Creating a Line Chart
            mChart = ChartFactory.getLineChartView(getBaseContext(), dataset,
                    multiRenderer);

            // Adding the Line Chart to the LinearLayout
            layout.addView(mChart);
        }
    }

    public void finishTraining(View view) {
        Log.d("SIZE", String.valueOf(inst.size()));
        inst.setClassIndex(inst.numAttributes() - 1);
        try {
            classifier.buildClassifier(inst);
            Log.d("CLASSIFIER", classifier.toString());
        } catch (Exception e) {
            Log.d("ECLASS", e.getMessage());
        }
        try {

            File f = new File(this.getApplicationContext().getFilesDir().getPath() + "traindata.txt");
            if (!f.exists()) {
                f.createNewFile();
            }
            fw = new FileWriter(f);
            fw.write(inst.toString());
            fw.flush();

            fw.close();

            File f_raw = new File(this.getApplicationContext().getFilesDir().getPath() + "traindata_raw.txt");
            if (!f.exists()) {
                f.createNewFile();
            }
            fw_raw = new FileWriter(f_raw);
            for (AccelData ad : fullSensorData)
                fw_raw.write(ad.toString() + "\n");
            fw_raw.flush();

            fw_raw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finish();
    }
}
