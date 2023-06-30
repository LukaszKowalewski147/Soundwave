package com.example.soundwave;

public class Tone {
    private final byte sinWaveData[];
    private final int frequency;
    private final short duration;
    private final SampleRate sampleRate;

    public Tone(byte[] sinWave, int frequency, short duration, SampleRate sampleRate) {
        this.sinWaveData = sinWave;
        this.frequency = frequency;
        this.duration = duration;
        this.sampleRate = sampleRate;
    }

    public byte[] getSinWaveData() {
        return sinWaveData;
    }

    public int getFrequency() {
        return frequency;
    }

    public short getDuration() {
        return duration;
    }

    public SampleRate getSampleRate() {
        return sampleRate;
    }
}
