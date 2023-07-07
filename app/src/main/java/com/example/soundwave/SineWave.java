package com.example.soundwave;

public class SineWave {

    private final int frequency;
    private final int amplitude;

    public SineWave(int frequency, int amplitude) {
        this.frequency = frequency;
        this.amplitude = amplitude;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getAmplitude() {
        return amplitude;
    }
}
