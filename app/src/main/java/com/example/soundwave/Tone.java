package com.example.soundwave;

public class Tone {

    private final byte[] sinWaveData;
    private final int fundamentalFrequency;
    private final int amplitude;

    public Tone(byte[] sinWaveData, int fundamentalFrequency, int amplitude) {
        this.sinWaveData = sinWaveData;
        this.fundamentalFrequency = fundamentalFrequency;
        this.amplitude = amplitude;
    }

    public byte[] getSinWaveData() {
        return sinWaveData;
    }

    public int getFundamentalFrequency() {
        return fundamentalFrequency;
    }

    public int getAmplitude() {
        return amplitude;
    }
}
