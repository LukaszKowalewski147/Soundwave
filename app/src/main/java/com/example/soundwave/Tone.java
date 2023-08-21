package com.example.soundwave;

import com.example.soundwave.utils.SampleRate;

public class Tone {

    private final byte[] samples;
    private final int fundamentalFrequency;
    private final double masterVolume;
    private final double duration;
    private final SampleRate sampleRate;
    private boolean saved;

    public Tone(byte[] samples, int fundamentalFrequency, double masterVolume, double duration, SampleRate sampleRate) {
        this.samples = samples;
        this.fundamentalFrequency = fundamentalFrequency;
        this.masterVolume = masterVolume;
        this.duration = duration;
        this.sampleRate = sampleRate;
        this.saved = false;
    }

    public byte[] getSamples() {
        return samples;
    }

    public int getFundamentalFrequency() {
        return fundamentalFrequency;
    }

    public double getMasterVolume() {
        return masterVolume;
    }

    public double getDuration() {
        return duration;
    }

    public SampleRate getSampleRate() {
        return sampleRate;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean isSaved) {
        this.saved = isSaved;
    }
}
