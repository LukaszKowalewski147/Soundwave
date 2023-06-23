package com.example.soundwave;

public class Tone {
    private final byte sinWaveData[];
    private final int frequency;
    private final short duration;
    private final SamplingRate samplingRate;

    public Tone(byte[] sinWave, int frequency, short duration, SamplingRate samplingRate) {
        this.sinWaveData = sinWave;
        this.frequency = frequency;
        this.duration = duration;
        this.samplingRate = samplingRate;
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

    public SamplingRate getSamplingRate() {
        return samplingRate;
    }
}
