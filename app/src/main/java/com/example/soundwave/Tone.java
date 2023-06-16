package com.example.soundwave;

public class Tone {
    private final byte sinWaveData[];
    private final int frequency;
    private final short duration;

    public Tone(byte[] sinWave, int frequency, short duration) {
        this.sinWaveData = sinWave;
        this.frequency = frequency;
        this.duration = duration;
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
}
