package com.example.soundwave.utils;

public enum SampleRate {
    RATE_44_1_KHZ(44100),
    RATE_48_KHZ(48000),
    RATE_96_KHZ(96000),
    RATE_192_KHZ(192000);

    public final int sampleRate;

    SampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }
}
