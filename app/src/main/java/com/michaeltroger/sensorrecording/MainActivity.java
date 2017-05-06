package com.michaeltroger.sensorrecording;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.psdev.licensesdialog.LicensesDialog;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private List<Sensor> mSensors = new ArrayList<>();
    private List<String> mLabels = new ArrayList<>();
    private Map<String, float[]> mCurrentCachedValues = new LinkedHashMap<>();
    private List<SensorData> mAllCachedValuesNotSerializedYet = new ArrayList<>();
    private Button mRecordButton;
    private Button mTagButton;
    private boolean mIsRecording = false;
    private FileWriter mFileWriter;
    private String mTempFileName;

    private static final String APP_DIRECTORY = "SensorRecording";
    private static final String TEMP_FILENAME = "tempSensorData.csv";

    private final Handler mHandler = new Handler();
    private long mStartTime;
    private ViewGroup mSensorWrapper;
    private AlertDialog.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        mRecordButton = (Button) findViewById(R.id.record);
        mTagButton = (Button) findViewById(R.id.tag);
        mSensorWrapper = (ViewGroup) findViewById(R.id.available_sensors);

        int id = 0;
        for (final Sensor sensor : sensors) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(sensor.getName());
            checkBox.setId(id++);
            mSensorWrapper.addView(checkBox);

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
        mCurrentCachedValues.put(event.sensor.getName(), Arrays.copyOf(event.values, event.values.length));

        float seconds = (event.timestamp - mStartTime) / 1000000000f;
        SensorData sensorData = new SensorData();
        sensorData.time = seconds;
        sensorData.values = new LinkedHashMap<>(mCurrentCachedValues);

        mAllCachedValuesNotSerializedYet.add(sensorData);
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
        Log.d("test", "tag");

        mCurrentCachedValues.put("tag", new float[]{1});

        float seconds = (SystemClock.elapsedRealtimeNanos() - mStartTime) / 1000000000f;
        SensorData sensorData = new SensorData();
        sensorData.time = seconds;
        sensorData.values = new LinkedHashMap<>(mCurrentCachedValues);

        mAllCachedValuesNotSerializedYet.add(sensorData);
        mCurrentCachedValues.put("tag", new float[]{Float.MIN_VALUE});
    }

    private void stopRecording() {
        mRecordButton.setText(R.string.record);
        mTagButton.setEnabled(false);
        mSensorWrapper.setVisibility(View.VISIBLE);

        mSensorManager.unregisterListener(this);

        showDialogAndRenameFile();

        mIsRecording = false;
    }

    private void startRecording() {
        mRecordButton.setText(R.string.stop_recording);
        mTagButton.setEnabled(true);
        mSensorWrapper.setVisibility(View.INVISIBLE);

        mLabels.clear();
        mLabels.add("time");
        mLabels.add("tag");

        mStartTime = SystemClock.elapsedRealtimeNanos();

        mCurrentCachedValues.clear();
        mCurrentCachedValues.put("tag", new float[]{Float.MIN_VALUE});

        Collections.sort(mSensors, new Comparator<Sensor>() {
            @Override
            public int compare(Sensor o1, Sensor o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (final Sensor sensor : mSensors) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

            String[] labels = SensorValuesMeta.getLabelsSensorValues(sensor.getType());
            mLabels.addAll(Arrays.asList(labels));

            float[] fl = new float[SensorValuesMeta.getSensorValuesSize(sensor.getType())];
            for (int i = 0; i < fl.length; i++) {
                fl[i] = Float.MIN_VALUE;
            }

            mCurrentCachedValues.put(sensor.getName(), fl);
        }

        mTempFileName= System.currentTimeMillis() + TEMP_FILENAME;
        try {
            createTempFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mIsRecording = true;
    }

    // source: https://stackoverflow.com/questions/27772011/how-to-export-data-to-csv-file-in-android
    private void createTempFile() throws IOException {
        new File(getExternalStorageDirectory(), APP_DIRECTORY).mkdirs();

        String baseDir = getExternalStorageDirectory().getAbsolutePath();
        String filePath = baseDir + File.separator + APP_DIRECTORY + File.separator + mTempFileName;
        File file = new File(filePath);
        CSVWriter writer;

        String[] dataAsArray;

        if (file.exists() && !file.isDirectory()) {
            mFileWriter = new FileWriter(filePath, true);
            writer = new CSVWriter(mFileWriter);

            List<String> data = new ArrayList<>();

            Collections.sort(mAllCachedValuesNotSerializedYet);

            for (SensorData m : mAllCachedValuesNotSerializedYet) {
                data.add(Float.toString(m.time));
                for (float[] f : m.values.values()) {
                    for (float f1 : f) {
                        if (f1 == Float.MIN_VALUE) {
                            data.add("");
                        } else if (f1 != 0) {
                            data.add(Float.toString(f1));
                        }
                    }
                }
                dataAsArray = data.toArray(new String[data.size()]);
                writer.writeNext(dataAsArray);
                data.clear();
            }
            mAllCachedValuesNotSerializedYet.clear();

            Log.d("tag", "file exists");
        } else {
            writer = new CSVWriter(new FileWriter(filePath));

            dataAsArray = mLabels.toArray(new String[mLabels.size()]);
            writer.writeNext(dataAsArray);
            Log.d("tag", "file notexists");
        }

        writer.close();
    }

    private void renameFile(String newFileName) {
        String fileName = newFileName.trim();

        if (!fileName.endsWith(".csv")) {
            fileName += ".csv";
        }
        String baseDir = getExternalStorageDirectory().getAbsolutePath();
        String appFolder = baseDir + File.separator + APP_DIRECTORY;
        File from = new File(appFolder, mTempFileName);
        File to = new File(appFolder, fileName);
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
                try {
                    createTempFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                renameFile(text);
                Log.d("tag", "OK");
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                try {
                    createTempFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("tag", "cancelled");
            }
        });
        builder.show();
    }

}
