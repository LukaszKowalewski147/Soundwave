package com.example.soundwave.components.sound;

import com.example.soundwave.utils.SampleRate;

public abstract class Sound {
    final SampleRate sampleRate;
    final int durationInMs;
    final byte[] pcmData;

    protected Sound(SampleRate sampleRate, int durationInMs, byte[] pcmData) {
        this.sampleRate = sampleRate;
        this.durationInMs = durationInMs;
        this.pcmData = pcmData;
    }

    protected Sound(SampleRate sampleRate, byte[] pcmData) {
        this.sampleRate = sampleRate;
        this.pcmData = pcmData;
        double durationInSeconds = (double) pcmData.length / sampleRate.sampleRate / 2;
        this.durationInMs = (int) Math.ceil(durationInSeconds * 1000);
    }

    public SampleRate getSampleRate() {
        return sampleRate;
    }

    public int getDurationInMs() {
        return durationInMs;
    }

    public byte[] getPcmData() {
        return pcmData;
    }
}
