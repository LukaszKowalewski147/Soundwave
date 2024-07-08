package com.example.soundwave.components;

import com.example.soundwave.utils.SampleRate;

public class Track {
    private final SampleRate sampleRate;
    private final byte[] samples;

    public Track(SampleRate sampleRate, byte[] samples) {
        this.sampleRate = sampleRate;
        this.samples = samples;
    }

    public SampleRate getSampleRate() {
        return sampleRate;
    }

    public byte[] getSamples() {
        return samples;
    }

    public double getTrackDuration() {
        return 0.0d;
    }
}
