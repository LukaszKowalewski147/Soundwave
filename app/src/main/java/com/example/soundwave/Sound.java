package com.example.soundwave;

import com.example.soundwave.utils.SampleRate;

public class Sound {
    private final byte[] sinWaveData;
    private final int numberOfTones;
    private final short duration;
    private final SampleRate sampleRate;
    int tmp_debug_volume;

    public Sound(byte[] sinWave, int numberOfTones, short duration, SampleRate sampleRate, int tmp_debug_volume) {
        this.sinWaveData = sinWave;
        this.numberOfTones = numberOfTones;
        this.duration = duration;
        this.sampleRate = sampleRate;
        this.tmp_debug_volume = tmp_debug_volume;
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
