package com.example.soundwave;

public class SineWave {

    private final int frequency;
    private final double amplitude;

    public SineWave(int frequency, double amplitude) {
        this.frequency = frequency;
        this.amplitude = amplitude;
    }

    public int getFrequency() {
        return frequency;
    }

    public double getAmplitude() {
        return amplitude;
    }
}
