package com.example.soundwave.components;

import com.example.soundwave.Overtone;
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

    public PresetOvertones getOvertonesPreset() {
        return overtonesPreset;
    }
}
