package com.michaeltroger.sensorrecording;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;


import java.util.ArrayList;
import java.util.List;

import de.psdev.licensesdialog.LicensesDialog;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private List<Sensor> mSensors = new ArrayList<>();
    private Button mRecordButton;
    private Button mTagButton;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        mRecordButton = (Button) findViewById(R.id.record);
        mTagButton = (Button) findViewById(R.id.tag);
        ViewGroup sensorWrapper = (ViewGroup) findViewById(R.id.available_sensors);

        int id = 0;
        for(final Sensor sensor : sensors){
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(sensor.getName());
            checkBox.setId(id++);
            sensorWrapper.addView(checkBox);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mSensors.add(sensor);
                    } else {
                        mSensors.remove(sensor);
                    }

                    if (mSensors.size() == 0) {
                        mRecordButton.setEnabled(false);
                    } else {
                        mRecordButton.setEnabled(true);
                    }
                }
            });
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(event.sensor.getName());
        stringBuilder.append("\n");
        stringBuilder.append(event.timestamp);
        Log.d("test", stringBuilder.toString());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void showLicenseInfo(View view) {
        new LicensesDialog.Builder(this)
                .setNotices(R.raw.notices)
                .setIncludeOwnLicense(true)
                .build()
                .show();
    }

    public void record(View view) {
        if (isRecording) {
           stopRecording();
        } else {
           startRecording();
        }

    }

    public void tag(View view) {

    }

    private void stopRecording() {
        mRecordButton.setText(R.string.record);
        mTagButton.setEnabled(false);

        mSensorManager.unregisterListener(this);

        isRecording = false;
    }
    private void startRecording() {
        mRecordButton.setText(R.string.stop_recording);
        mTagButton.setEnabled(true);

        for (final Sensor sensor : mSensors) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        isRecording = true;
    }


}
