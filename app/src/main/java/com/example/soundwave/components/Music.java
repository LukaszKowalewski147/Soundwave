package com.example.soundwave.components;

import com.example.soundwave.utils.SampleRate;

public class Music {
    private final SampleRate sampleRate;
    private final byte[] samples16BitPCM;
    private final double durationInSeconds;
    private final int durationInMilliseconds;

    public Music(SampleRate sampleRate, byte[] samples16BitPCM) {
        this.sampleRate = sampleRate;
        this.samples16BitPCM = samples16BitPCM;
        this.durationInSeconds = (double) samples16BitPCM.length  / sampleRate.sampleRate / 2;
        this.durationInMilliseconds = (int) Math.ceil(durationInSeconds * 1000);
    }

    public SampleRate getSampleRate() {
        return sampleRate;
    }

    public byte[] getSamples16BitPCM() {
        return samples16BitPCM;
    }

    public double getDurationInSeconds() {
        return durationInSeconds;
    }

    public int getDurationInMilliseconds() {
        return durationInMilliseconds;
    }
}
