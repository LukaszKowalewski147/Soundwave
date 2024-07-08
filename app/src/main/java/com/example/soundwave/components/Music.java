package com.example.soundwave.components;

import com.example.soundwave.utils.SampleRate;

public class Music {
    private final SampleRate sampleRate;
    private final byte[] samples;

    public Music(SampleRate sampleRate, byte[] samples) {
        this.sampleRate = sampleRate;
        this.samples = samples;
    }

    public SampleRate getSampleRate() {
        return sampleRate;
    }

    public byte[] getSamples() {
        return samples;
    }
}
