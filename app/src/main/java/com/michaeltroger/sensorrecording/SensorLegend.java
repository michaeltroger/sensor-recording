package com.michaeltroger.sensorrecording;

import android.hardware.Sensor;
import android.util.SparseArray;

class SensorLegend {
    private static final SparseArray<String[]> legendsAllSensors = new SparseArray<>();
    static {
        legendsAllSensors.append(Sensor.TYPE_ACCELEROMETER, new String[]{"accX", "accY", "accZ"});
        legendsAllSensors.append(Sensor.TYPE_MAGNETIC_FIELD, new String[]{"magX", "magY", "magZ"});
        legendsAllSensors.append(Sensor.TYPE_GYROSCOPE, new String[]{"gyrX", "gyrY", "gyrZ"});
        legendsAllSensors.append(Sensor.TYPE_LIGHT, new String[]{"light"});
        legendsAllSensors.append(Sensor.TYPE_PRESSURE, new String[]{"pressure"});
        legendsAllSensors.append(Sensor.TYPE_PROXIMITY, new String[]{"prox"});
        legendsAllSensors.append(Sensor.TYPE_GRAVITY, new String[]{"gravX", "gravY", "gravZ"});
        legendsAllSensors.append(Sensor.TYPE_ROTATION_VECTOR, new String[]{"rot0", "rot1", "rot2", "rot3", "rot4"});
        legendsAllSensors.append(Sensor.TYPE_ORIENTATION, new String[]{"azimuth", "pitch", "roll"});
        legendsAllSensors.append(Sensor.TYPE_RELATIVE_HUMIDITY, new String[]{"humidity"});
        legendsAllSensors.append(Sensor.TYPE_AMBIENT_TEMPERATURE, new String[]{"ambientTemp"});
        legendsAllSensors.append(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, new String[]{"xUncalib", "yUncalib", "zUncalib", "xBias", "yBias", "zBias"});
        legendsAllSensors.append(Sensor.TYPE_GAME_ROTATION_VECTOR, new String[]{"rot0", "rot1", "rot2", "rot3", "rot4"});
        legendsAllSensors.append(Sensor.TYPE_GYROSCOPE_UNCALIBRATED, new String[]{"gyr0", "gyr1", "gyr2", "gyr3", "gyr4", "gyr5"});
        legendsAllSensors.append(Sensor.TYPE_POSE_6DOF, new String[]{"pose0","pose1","pose2","pose3","pose4","pose5","pose6","pose7","pose8","pose9","pose10","pose11","pose12","pose13","pose14"});
        legendsAllSensors.append(Sensor.TYPE_STATIONARY_DETECT, new String[]{"stationary"});
        legendsAllSensors.append(Sensor.TYPE_MOTION_DETECT, new String[]{"motion"});
        legendsAllSensors.append(Sensor.TYPE_HEART_BEAT, new String[]{"heartbeat"});
    }

    private SensorLegend() {}

    static String[] getLegend(final int sensorType) {
        final String[] legend = legendsAllSensors.get(sensorType);
        return legend != null ? legend : new String[]{"unknownType" + sensorType};
    }

}
