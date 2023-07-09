package com.example.soundwave;

public class Tone {

    private final double[] samples;
    private final int fundamentalFrequency;
    private final double volume;

    public Tone(double[] samples, int fundamentalFrequency, double volume) {
        this.samples = samples;
        this.fundamentalFrequency = fundamentalFrequency;
        this.volume = volume;
    }

    public double[] getSamples() {
        return samples;
    }

    public int getFundamentalFrequency() {
        return fundamentalFrequency;
    }

    public double getVolume() {
        return volume;
    }
}
