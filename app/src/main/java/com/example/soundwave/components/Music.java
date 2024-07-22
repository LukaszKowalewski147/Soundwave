package com.example.soundwave.components;

import com.example.soundwave.utils.SampleRate;

public class Music {

    private int id;
    private String name;
    private final SampleRate sampleRate;
    private final byte[] samples16BitPCM;
    private final double durationInSeconds;
    private final int durationInMilliseconds;

    public Music(SampleRate sampleRate, byte[] samples16BitPCM) {
        this.id = -1;
        this.name = "";
        this.sampleRate = sampleRate;
        this.samples16BitPCM = samples16BitPCM;
        this.durationInSeconds = (double) samples16BitPCM.length / sampleRate.sampleRate / 2;
        this.durationInMilliseconds = (int) Math.ceil(durationInSeconds * 1000);
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
