package com.example.soundwave.components.sound;

import com.example.soundwave.utils.SampleRate;

public abstract class Sound {
    final SampleRate sampleRate;
    final int durationInMilliseconds;

    Sound(SampleRate sampleRate, int durationInMilliseconds) {
        this.sampleRate = sampleRate;
        this.durationInMilliseconds = durationInMilliseconds;
    }

    Sound(SampleRate sampleRate, double pcmDataLength) {
        this.sampleRate = sampleRate;
        this.durationInMilliseconds = calculateDurationInMilliseconds(pcmDataLength);
    }

    public SampleRate getSampleRate() {
        return sampleRate;
    }

    public int getDurationInMilliseconds() {
        return durationInMilliseconds;
    }

    private int calculateDurationInMilliseconds(double pcmDataLength) {
        double durationInSeconds = pcmDataLength / sampleRate.sampleRate / 2;
        return (int) Math.ceil(durationInSeconds * 1000);
    }
}
