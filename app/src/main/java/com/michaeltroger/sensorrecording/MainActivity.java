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


import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.psdev.licensesdialog.LicensesDialog;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private List<Sensor> mSensors = new ArrayList<>();
    private Button mRecordButton;
    private Button mTagButton;
    private boolean mIsRecording = false;
    private FileWriter mFileWriter;

    private static final String TEMP_FILENAME = "tempSensorData.csv";

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

        try {
            createTempFile();
            renameFile("itWorks.csv");
        } catch (IOException e) {
            e.printStackTrace();
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
        String toPrint = event.sensor.getName();
        toPrint += "\n";
        toPrint += event.timestamp;
        Log.d("test", toPrint);
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
        if (mIsRecording) {
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

        mIsRecording = false;
    }
    private void startRecording() {
        mRecordButton.setText(R.string.stop_recording);
        mTagButton.setEnabled(true);

        for (final Sensor sensor : mSensors) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        mIsRecording = true;
    }

    // source: https://stackoverflow.com/questions/27772011/how-to-export-data-to-csv-file-in-android
    private void createTempFile() throws IOException {
        String baseDir = getExternalStorageDirectory().getAbsolutePath();
        String fileName = TEMP_FILENAME;
        String filePath = baseDir + File.separator + fileName;
        File file = new File(filePath, TEMP_FILENAME);
        CSVWriter writer;

        if(file.exists() && !file.isDirectory()){
            mFileWriter = new FileWriter(filePath, true);
            writer = new CSVWriter(mFileWriter);
            String[] data = {"5.23","2.4"};
            writer.writeNext(data);
        }
        else {
            writer = new CSVWriter(new FileWriter(filePath));
            String[] data = {"Ship Name","Scientist Name"};
            writer.writeNext(data);
        }

        writer.close();
    }

    private void renameFile(String newFileName) {
        File from = new File(getExternalStorageDirectory().getAbsolutePath(), TEMP_FILENAME);
        File to = new File(getExternalStorageDirectory().getAbsolutePath(), newFileName);
        from.renameTo(to);
    }




}
