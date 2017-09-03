package com.michaeltroger.sensorrecording;

import android.databinding.DataBindingUtil;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;

import com.michaeltroger.sensorrecording.databinding.ActivityMainBinding;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private ActivityMainBinding binding;
    private boolean mIsRecording = false;
    private FileWriter mFileWriter;
    private String mTempFileName;

    private static final String TAG = "MainActivity";
    private static final String APP_DIRECTORY = "SensorRecording";
    private static final String TEMP_FILENAME = "tempSensorData.csv";

    private long mStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setHandlers(new MyHandlers());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        final List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        int id = 0;
        for (final Sensor sensor : sensors) {
            final CheckBox checkBox = new CheckBox(this);
            checkBox.setText(sensor.getName());
            checkBox.setId(id++);
            binding.availableSensors.addView(checkBox);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    mSensors.add(sensor);
                } else {
                    mSensors.remove(sensor);
                }

                if (mSensors.isEmpty()) {
                    binding.btnRecord.setEnabled(false);
                } else {
                    binding.btnRecord.setEnabled(true);
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

        final float seconds = (event.timestamp - mStartTime) / 1000000000f;
        final SensorData sensorData = new SensorData();
        sensorData.time = seconds;
        sensorData.values = new LinkedHashMap<>(mCurrentCachedValues);

        mAllCachedValuesNotSerializedYet.add(sensorData);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // accuracy is expected to not change
    }

    public class MyHandlers {
        public void showLicenseInfo(View view) {
            new LicensesDialog.Builder(MainActivity.this)
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
            mCurrentCachedValues.put("tag", new float[]{1});

            final float seconds = (SystemClock.elapsedRealtimeNanos() - mStartTime) / 1000000000f;
            final SensorData sensorData = new SensorData();
            sensorData.time = seconds;
            sensorData.values = new LinkedHashMap<>(mCurrentCachedValues);

            mAllCachedValuesNotSerializedYet.add(sensorData);
            mCurrentCachedValues.put("tag", new float[]{Float.MIN_VALUE});
        }
    }


    private void stopRecording() {
        binding.btnRecord.setText(R.string.record);
        binding.btnTag.setEnabled(false);
        binding.availableSensors.setVisibility(View.VISIBLE);

        mSensorManager.unregisterListener(this);

        showDialogAndRenameFile();

        mIsRecording = false;
    }

    private void startRecording() {
        binding.btnRecord.setText(R.string.stop_recording);
        binding.btnTag.setEnabled(true);
        binding.availableSensors.setVisibility(View.INVISIBLE);

        mLabels.clear();
        mLabels.add("time");
        mLabels.add("tag");

        mStartTime = SystemClock.elapsedRealtimeNanos();

        mCurrentCachedValues.clear();
        mCurrentCachedValues.put("tag", new float[]{Float.MIN_VALUE});

        Collections.sort(mSensors, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        for (final Sensor sensor : mSensors) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);

            final String[] labels = SensorLegend.getLegend(sensor.getType());
            mLabels.addAll(Arrays.asList(labels));

            final float[] fl = new float[labels.length];
            for (int i = 0; i < fl.length; i++) {
                fl[i] = Float.MIN_VALUE;
            }

            mCurrentCachedValues.put(sensor.getName(), fl);
        }

        mTempFileName= System.currentTimeMillis() + TEMP_FILENAME;
        try {
            createTempFile();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        mIsRecording = true;
    }

    // source: https://stackoverflow.com/questions/27772011/how-to-export-data-to-csv-file-in-android
    private void createTempFile() throws IOException {
        new File(getExternalStorageDirectory(), APP_DIRECTORY).mkdirs();

        final String baseDir = getExternalStorageDirectory().getAbsolutePath();
        final String filePath = baseDir + File.separator + APP_DIRECTORY + File.separator + mTempFileName;
        final File file = new File(filePath);
        final CSVWriter writer;

        String[] dataAsArray;

        if (file.exists() && !file.isDirectory()) {
            mFileWriter = new FileWriter(filePath, true);
            writer = new CSVWriter(mFileWriter);

            final List<String> data = new ArrayList<>();

            Collections.sort(mAllCachedValuesNotSerializedYet);

            for (final SensorData m : mAllCachedValuesNotSerializedYet) {
                data.add(Float.toString(m.time));
                for (final float[] f : m.values.values()) {
                    for (final float f1 : f) {
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

            Log.d(TAG, "file exists");
        } else {
            writer = new CSVWriter(new FileWriter(filePath));

            dataAsArray = mLabels.toArray(new String[mLabels.size()]);
            writer.writeNext(dataAsArray);
            Log.d(TAG, "file does not exists");
        }

        writer.close();
    }

    private void renameFile(@NonNull final String oldFileName, @NonNull final String newFileName) {
        String fileName = newFileName.trim();

        if (!fileName.endsWith(".csv")) {
            fileName += ".csv";
        }
        final String baseDir = getExternalStorageDirectory().getAbsolutePath();
        final String appFolder = baseDir + File.separator + APP_DIRECTORY;
        final File from = new File(appFolder, oldFileName);
        final File to = new File(appFolder, fileName);
        from.renameTo(to);
    }

    // source: https://stackoverflow.com/questions/10903754/input-text-dialog-android
    private void showDialogAndRenameFile() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("File name");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            final String newFileName = input.getText().toString();
            final String oldFileName = mTempFileName;
            try {
                createTempFile();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }

            renameFile(oldFileName, newFileName);
            Log.d(TAG, "OK");
        });

        builder.setOnCancelListener(dialog -> {
            try {
                createTempFile();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
            Log.d(TAG, "cancelled");
        });
        builder.show();
    }

}
