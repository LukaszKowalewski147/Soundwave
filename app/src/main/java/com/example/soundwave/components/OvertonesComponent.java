package com.example.soundwave.components;

import com.example.soundwave.utils.PresetOvertones;

import java.io.Serializable;
import java.util.ArrayList;

public class OvertonesComponent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ArrayList<Overtone> overtones;
    private final PresetOvertones overtonesPreset;

    public OvertonesComponent(ArrayList<Overtone> overtones, PresetOvertones overtonesPreset) {
        this.overtones = overtones;
        this.overtonesPreset = overtonesPreset;
    }

    public ArrayList<Overtone> getOvertones() {
        return overtones;
    }

    public ArrayList<Overtone> getActiveOvertones() {
        ArrayList<Overtone> activeOvertones = new ArrayList<>();

        for (Overtone overtone : overtones) {
            if (overtone.isActive())
                activeOvertones.add(overtone);
        }
        return activeOvertones;
    }

    public PresetOvertones getOvertonesPreset() {
        return overtonesPreset;
    }

    public int getActiveOvertonesNumber() {
        int activeOvertones = 0;

        if (!overtones.isEmpty()) {
            for (Overtone overtone : overtones) {
                if (overtone.isActive())
                    ++activeOvertones;
            }
        }

        return activeOvertones;
    }
}
