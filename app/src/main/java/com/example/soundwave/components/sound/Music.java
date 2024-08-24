package com.example.soundwave.components.sound;

import com.example.soundwave.utils.SampleRate;

public class Music extends Sound implements Listenable {

    private int id;
    private String name;
    private final double durationSeconds;
    private final byte[] pcmData;

    public Music(SampleRate sampleRate, byte[] pcmData) {
        super(sampleRate, (double) pcmData.length);
        this.id = -1;
        this.name = "";
        this.durationSeconds = (double) pcmData.length / sampleRate.sampleRate / 2;
        this.pcmData = pcmData;
    }

    @Override
    public byte[] getPcmData() {
        return pcmData;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }
}
