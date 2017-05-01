package com.michaeltroger.sensorrecording;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;


import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.psdev.licensesdialog.LicensesDialog;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private List<Sensor> mSensors = new ArrayList<>();
    private List<String> mLabels = new ArrayList<>();
    private Map<String, float[]> mCurrentCachedValues = new HashMap<>();
    private List<Map<String, float[]>> mAllCachedValuesNotSerializedYet = new ArrayList<>();
    private Button mRecordButton;
    private Button mTagButton;
    private boolean mIsRecording = false;
    private FileWriter mFileWriter;

    private static final String TEMP_FILENAME = "tempSensorData.csv";
    private Runnable mTimer;
    private final Handler mHandler = new Handler();

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

        mTimer = new Runnable() {
            @Override
            public void run() {
                try {
                    createTempFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mHandler.postDelayed(this, 330);
            }
        };
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
        mCurrentCachedValues.put(event.sensor.getName(), Arrays.copyOf(event.values, event.values.length));
        mAllCachedValuesNotSerializedYet.add(mCurrentCachedValues);
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
        mHandler.removeCallbacks(mTimer);

        showDialogAndRenameFile();

        mIsRecording = false;
    }
    private void startRecording() {
        mRecordButton.setText(R.string.stop_recording);
        mTagButton.setEnabled(true);

        mLabels.clear();

        for (final Sensor sensor : mSensors) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);

            String[] labels = SensorValuesMeta.getLabelsSensorValues(sensor.getType());
            mLabels.addAll(Arrays.asList(labels));
        }

        mHandler.postDelayed(mTimer, 100);

        mIsRecording = true;
    }

    // source: https://stackoverflow.com/questions/27772011/how-to-export-data-to-csv-file-in-android
    private synchronized void createTempFile() throws IOException {
        String baseDir = getExternalStorageDirectory().getAbsolutePath();
        String fileName = TEMP_FILENAME;
        String filePath = baseDir + File.separator + fileName;
        File file = new File(filePath);
        CSVWriter writer;

        if(file.exists() && !file.isDirectory()){
            mFileWriter = new FileWriter(filePath, true);
            writer = new CSVWriter(mFileWriter);

            List<String> data = new ArrayList<>();

            Iterator<Map<String,float[]>> iter = mAllCachedValuesNotSerializedYet.iterator();
            while (iter.hasNext()) {
                Map<String,float[]> m = iter.next();
                for (float[] f : m.values()) {
                    for (float f1 : f) {
                        data.add(Float.toString(f1));
                    }
                }
                iter.remove();
                String[] dataAsArray = data.toArray(new String[data.size()]);
                writer.writeNext(dataAsArray);
                data.clear();
            }


            Log.d("tag", "file exists");
        }
        else {
            writer = new CSVWriter(new FileWriter(filePath));

            String[] dataArray = mLabels.toArray(new String[mLabels.size()]);
            writer.writeNext(dataArray);
            Log.d("tag", "file notexists");
        }

        writer.close();
    }

    private void renameFile(String newFileName) {
        String fileName = newFileName.trim();
        if (fileName.length() == 0) {
            fileName = TEMP_FILENAME + System.currentTimeMillis();
        }
        File from = new File(getExternalStorageDirectory().getAbsolutePath(), TEMP_FILENAME);
        File to = new File(getExternalStorageDirectory().getAbsolutePath(), fileName);
        from.renameTo(to);
    }

    // source: https://stackoverflow.com/questions/10903754/input-text-dialog-android
    private void showDialogAndRenameFile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("File name");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                renameFile(text);
            }
        });
        
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                renameFile("");
            }
        });

        builder.show();

    }

}
