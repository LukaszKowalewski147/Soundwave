package com.example.soundwave.components.sound;

import com.example.soundwave.utils.SampleRate;

public abstract class Sound {
    final SampleRate sampleRate;
    final int durationMilliseconds;

    Sound(SampleRate sampleRate, int durationMilliseconds) {
        this.sampleRate = sampleRate;
        this.durationMilliseconds = durationMilliseconds;
    }

    Sound(SampleRate sampleRate, double pcmDataLength) {
        this.sampleRate = sampleRate;
        this.durationMilliseconds = calculateDurationMilliseconds(pcmDataLength);
    }

    public SampleRate getSampleRate() {
        return sampleRate;
    }

    public int getDurationMilliseconds() {
        return durationMilliseconds;
    }

    private int calculateDurationMilliseconds(double pcmDataLength) {
        double durationInSeconds = pcmDataLength / sampleRate.sampleRate / 2;
        return (int) Math.ceil(durationInSeconds * 1000);
    }
}
