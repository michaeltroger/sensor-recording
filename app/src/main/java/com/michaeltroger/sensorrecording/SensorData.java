package com.michaeltroger.sensorrecording;


import java.util.Map;

public class SensorData implements Comparable<SensorData> {
    Map<String, float[]> values;
    float time;

    @Override
    public int compareTo(SensorData o) {
        return Float.compare(this.time, o.time);
    }
}
