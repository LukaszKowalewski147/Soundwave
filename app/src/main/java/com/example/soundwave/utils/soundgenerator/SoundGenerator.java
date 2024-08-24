package com.example.soundwave.utils.soundgenerator;

import com.example.soundwave.utils.SampleRate;

public abstract class SoundGenerator {

    final SampleRate sampleRate;
    int samplesNumber;
    double[] samples;
    byte[] pcmData;

    // Constructor for ToneGenerator and SilenceGenerator
    public SoundGenerator(SampleRate sampleRate, double durationSeconds) {
        this.sampleRate = sampleRate;
        this.samplesNumber = (int) Math.ceil(durationSeconds * sampleRate.sampleRate);
        assignOtherAttributes();
    }

    // Constructor for TrackGenerator and MusicGenerator
    public SoundGenerator(SampleRate sampleRate, int samplesNumber) {
        this.sampleRate = sampleRate;
        this.samplesNumber = samplesNumber;
        assignOtherAttributes();
    }

    private void assignOtherAttributes() {
        this.samples = new double[samplesNumber];
        this.pcmData = new byte[2 * samplesNumber];      // 2 bytes of data for 16bit sample
    }
}
