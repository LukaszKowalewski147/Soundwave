package com.example.soundwave.model.entity;

public class Overtone {

    private final int index;
    private final int frequency;
    private final double amplitude;
    private final boolean active;

    public Overtone(int index, int frequency, double amplitude, boolean active) {
        this.index = index;
        this.frequency = frequency;
        this.amplitude = amplitude;
        this.active = active;
    }

    public int getFrequency() {
        return frequency;
    }

    public double getAmplitude() {
        return amplitude;
    }

    public boolean isActive() {
        return active;
    }
}
