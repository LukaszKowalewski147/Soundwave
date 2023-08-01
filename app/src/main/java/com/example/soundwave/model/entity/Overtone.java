package com.example.soundwave.model.entity;

public class Overtone {

    private int amplitude;
    private boolean active;

    public Overtone(int amplitude, boolean active) {
        this.amplitude = amplitude;
        this.active = active;
    }

    public int getAmplitude() {
        return amplitude;
    }

    public boolean isActive() {
        return active;
    }
}
