package com.example.soundwave.components.sound;

import com.example.soundwave.utils.SampleRate;

public abstract class ListenableSound extends Sound {

    private String name;
    private final byte[] pcmData;

    ListenableSound(String name, SampleRate sampleRate, int durationMilliseconds, byte[] pcmData) {
        super(sampleRate, durationMilliseconds);
        this.pcmData = pcmData;
        assignOtherAttributes(name);
    }

    ListenableSound(String name, SampleRate sampleRate, byte[] pcmData) {
        super(sampleRate, (double) pcmData.length);
        this.pcmData = pcmData;
        assignOtherAttributes(name);
    }

    private void assignOtherAttributes(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public byte[] getPcmData() {
        return pcmData;
    }
}
