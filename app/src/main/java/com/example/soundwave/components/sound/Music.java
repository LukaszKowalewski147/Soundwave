package com.example.soundwave.components.sound;

import com.example.soundwave.utils.SampleRate;

public class Music extends ListenableSound {

    private int id;
    private final double durationSeconds;

    public Music(SampleRate sampleRate, byte[] pcmData) {
        super("", sampleRate, pcmData);
        this.id = -1;
        this.durationSeconds = (double) pcmData.length / sampleRate.sampleRate / 2;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }
}
