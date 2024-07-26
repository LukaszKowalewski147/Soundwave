package com.example.soundwave.components;

import com.example.soundwave.utils.SampleRate;

public class Track {
    private final SampleRate sampleRate;
    private final double[] samples;

    public Track(SampleRate sampleRate, double[] samples) {
        this.sampleRate = sampleRate;
        this.samples = samples;
    }

    public SampleRate getSampleRate() {
        return sampleRate;
    }

    public double[] getSamples() {
        return samples;
    }

    public int getNumberOfSamples() {
        return samples.length;
    }

    public double getTrackDurationInSeconds() {
        return (double) samples.length / sampleRate.sampleRate;
    }
}
