package com.example.soundwave;

public enum SamplingRate {
    RATE_44_1_KHZ(44100),
    RATE_48_KHZ (48000),
    RATE_96_KHZ (96000),
    RATE_192_KHZ (192000);

    int samplingRate;

    SamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }
}
