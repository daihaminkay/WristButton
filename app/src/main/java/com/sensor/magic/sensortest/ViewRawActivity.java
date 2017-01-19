package com.sensor.magic.sensortest;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;

public class ViewRawActivity extends WearableActivity implements SensorEventListener {

    int toSkip = 10;
    int count = 0;
    boolean chart = false;
    boolean fix = false;
    boolean gyro = false;
    GestureDetector gd;
    private SensorManager mSensorManager;
    private Sensor mLight;
    private Sensor mGyro;
    private ArrayList<AccelData> sensorData = new ArrayList<>();
    private ArrayList<AccelData> fullSensorData = new ArrayList<>();
    private BoxInsetLayout layout;
    private TextView raw;
    private View mChart;

    static String anythingToString(ArrayList<AccelData> q) {
        StringBuilder sb = new StringBuilder();
        for (int i = q.size() - 1; i >= 0; i--) {
            sb.append(q.get(i).toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_raw);
        layout = (BoxInsetLayout) findViewById(R.id.test_data_container);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        gd = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {


            //here is the method for double tap


            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                super.onSingleTapUp(e);
                gyro = !gyro;
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {

                chart = !chart;

                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                fix = !fix;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }


        });

        ScrollView scroll = (ScrollView) findViewById(R.id.scroll_text);

        scroll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return gd.onTouchEvent(event);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_FASTEST);
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

            mChart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    return gd.onTouchEvent(event);
                }
            });
            // Adding the Line Chart to the LinearLayout
            layout.addView(mChart);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sens = event.sensor;
        if (sens.getType() == Sensor.TYPE_LINEAR_ACCELERATION && !gyro) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            long timestamp = System.currentTimeMillis();
            final AccelData dat = new AccelData(timestamp, x, y, z);
            sensorData.add(dat);

            if ((Math.abs(x) > 2 || Math.abs(y) > 2 || Math.abs(z) > 2) && !fix) {
                if (chart) {
                    raw.setVisibility(View.INVISIBLE);

                    if (++count == toSkip) {
                        openChart();
                        count = 0;
                    }
                } else {
                    raw = (TextView) findViewById(R.id.raw);
                    raw.setText(anythingToString(sensorData));
                    if (raw.getVisibility() != View.VISIBLE)
                        raw.setVisibility(View.VISIBLE);
                    if (mChart != null)
                        layout.removeView(mChart);
                }
            }

            if (sensorData.size() > 30) {
                sensorData.remove(0);
            }
        } else if (sens.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR && gyro) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float a = event.values[3];

            float[] rotMat = new float[9];
            float[] vals = new float[3];
            long timestamp = System.currentTimeMillis();
            if ((Math.abs(x) > 0.05 || Math.abs(y) > 0.05 || Math.abs(z) > 0.05) && !fix) {
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
                raw = (TextView) findViewById(R.id.raw);
                raw.setText(anythingToString(sensorData));
                if (sensorData.size() > 30) {
                    sensorData.remove(0);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
