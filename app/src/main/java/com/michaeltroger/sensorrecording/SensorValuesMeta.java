package com.michaeltroger.sensorrecording;

import android.hardware.Sensor;

public class SensorValuesMeta {
    public static String[] getLabelsSensorValues(final int sensorType) {
        final String[] units;

        switch(sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                units = new String[]{"accX", "accY", "accZ"};
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                units = new String[]{"magX", "magY", "magZ"};
                break;
            case Sensor.TYPE_GYROSCOPE:
                units = new String[]{"gyrX", "gyrY", "gyrZ"};
                break;
            case Sensor.TYPE_LIGHT:
                units = new String[]{"light"};
                break;
            case Sensor.TYPE_PRESSURE:
                units = new String[]{"pressure"};
                break;
            case Sensor.TYPE_PROXIMITY:
                units = new String[]{"prox"};
                break;
            case Sensor.TYPE_GRAVITY:
                units = new String[]{"gravX", "gravY", "gravZ"};
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                units = new String[]{"rot0", "rot1", "rot2", "rot3", "rot4"};
                break;
            case Sensor.TYPE_ORIENTATION:
                units = new String[]{"azimuth", "pitch", "roll"};
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                units = new String[]{"humidity"};
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                units = new String[]{"ambientTemp"};
                break;
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                units = new String[]{"xUncalib", "yUncalib", "zUncalib", "xBias", "yBias", "zBias"};
                break;
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                units = new String[]{"rot0", "rot1", "rot2", "rot3", "rot4"};
                break;
            case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
                units = new String[]{"gyr0", "gyr1", "gyr2", "gyr3", "gyr4", "gyr5"};
                break;
            case Sensor.TYPE_POSE_6DOF:
                units = new String[]{"pose0","pose1","pose2","pose3","pose4","pose5","pose6","pose7","pose8","pose9","pose10","pose11","pose12","pose13","pose14"};
                break;
            case Sensor.TYPE_STATIONARY_DETECT:
                units = new String[]{"stationary"};
                break;
            case Sensor.TYPE_MOTION_DETECT:
                units = new String[]{"motion"};
                break;
            case Sensor.TYPE_HEART_BEAT:
                units = new String[]{"heartbeat"};
                break;
            default:
                units = new String[]{"unknownType" + sensorType};
                break;
        }

        return units;
    }
}
