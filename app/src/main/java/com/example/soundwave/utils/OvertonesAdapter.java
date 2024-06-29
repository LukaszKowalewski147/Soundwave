package com.example.soundwave.utils;

import com.example.soundwave.Overtone;

import java.util.ArrayList;
import java.util.List;

public class OvertonesAdapter {
    public static List<Overtone> convertDbStringToOvertones(String overtones) {
        List<Overtone> overtonesList = new ArrayList<>();

        if (overtones == null || overtones.trim().isEmpty())
            return overtonesList;

        String[] entries = overtones.split(";");

        for (String entry : entries) {
            if (!entry.trim().isEmpty()) {
                String[] fields = entry.split(",");
                if (fields.length == 4) {
                    int index = Integer.parseInt(fields[0].trim());
                    int frequency = Integer.parseInt(fields[1].trim());
                    double amplitude = Double.parseDouble(fields[2].trim());
                    boolean active = Boolean.parseBoolean(fields[3].trim());
                    overtonesList.add(new Overtone(index, frequency, amplitude, active));
                }
            }
        }
        return overtonesList;
    }
}
