package com.example.soundwave.components.sound;

import com.example.soundwave.utils.SampleRate;
import com.example.soundwave.utils.UnitsConverter;

import java.io.Serializable;

public class Silence extends Sound implements Trackable, Serializable {
    private static final long serialVersionUID = 1L;

    private final double[] samples;

    public Silence(SampleRate sampleRate, int durationMilliseconds, double[] samples) {
        super(sampleRate, durationMilliseconds);
        this.samples = samples;
    }

    @Override
    public double[] getSamples() {
        return samples;
    }

    @Override
    public double getDurationSeconds() {
        return UnitsConverter.convertMillisecondsToSeconds(durationMilliseconds);
    }

    @Override
    public int getSamplesNumber() {
        return samples.length;
    }
}
