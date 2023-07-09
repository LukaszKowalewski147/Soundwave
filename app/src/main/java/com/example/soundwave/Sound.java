package com.example.soundwave;

public class Sound {
    private final byte[] sinWaveData;
    private final int numberOfTones;
    private final short duration;
    private final SampleRate sampleRate;

    public Sound(byte[] sinWave, int numberOfTones, short duration, SampleRate sampleRate) {
        this.sinWaveData = sinWave;
        this.numberOfTones = numberOfTones;
        this.duration = duration;
        this.sampleRate = sampleRate;
    }

    public byte[] getSinWaveData() {
        return sinWaveData;
    }

    public int getNumberOfTones() {
        return numberOfTones;
    }

    public short getDuration() {
        return duration;
    }

    public SampleRate getSampleRate() {
        return sampleRate;
    }
}
