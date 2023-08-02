package com.example.soundwave.model.entity;

public class Overtone {

    private int index;
    private int frequency;
    private int amplitude;
    private boolean active;

    public Overtone(int index, int frequency, int amplitude, boolean active) {
        this.index = index;
        this.frequency = frequency;
        this.amplitude = amplitude;
        this.active = active;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getAmplitude() {
        return amplitude;
    }

    public boolean isActive() {
        return active;
    }
}
