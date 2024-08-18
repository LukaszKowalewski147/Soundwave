package com.example.soundwave.components;

import com.example.soundwave.utils.PresetEnvelope;
import com.example.soundwave.utils.PresetOvertones;
import com.example.soundwave.utils.SampleRate;
import com.example.soundwave.utils.UnitsConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class Tone implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private final SampleRate sampleRate;
    private final EnvelopeComponent envelopeComponent;
    private final FundamentalFrequencyComponent fundamentalFrequencyComponent;
    private final OvertonesComponent overtonesComponent;
    private final int durationInMs;
    private final double[] samples;
    private final byte[] pcmSound;

    public Tone(SampleRate sampleRate, EnvelopeComponent ec, FundamentalFrequencyComponent ffc,
                OvertonesComponent oc, int durationInMs, double[] samples, byte[] pcmSound) {
        this.id = -1;
        this.name = "";
        this.sampleRate = sampleRate;
        this.envelopeComponent = ec;
        this.fundamentalFrequencyComponent = ffc;
        this.overtonesComponent = oc;
        this.durationInMs = durationInMs;
        this.samples = samples;
        this.pcmSound = pcmSound;
    }

    // Constructor to generate silence tone
    public Tone(SampleRate sampleRate, int durationInMs, double[] samples) {
        this.id = -1;
        this.sampleRate = sampleRate;
        this.durationInMs = durationInMs;
        this.samples = samples;
        this.name = null;
        this.envelopeComponent = null;
        this.fundamentalFrequencyComponent = null;
        this.overtonesComponent = null;
        this.pcmSound = null;
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

    public EnvelopeComponent getEnvelopeComponent() {
        return envelopeComponent;
    }

    public FundamentalFrequencyComponent getFundamentalFrequencyComponent() {
        return fundamentalFrequencyComponent;
    }

    public OvertonesComponent getOvertonesComponent() {
        return overtonesComponent;
    }

    public double[] getSamples() {
        return samples;
    }

    public byte[] getPcmSound() {
        return pcmSound;
    }

    public PresetEnvelope getEnvelopePreset() {
        return Objects.requireNonNull(envelopeComponent).getEnvelopePreset();
    }

    public PresetOvertones getOvertonesPreset() {
        return Objects.requireNonNull(overtonesComponent).getOvertonesPreset();
    }

    public int getFundamentalFrequency() {
        return Objects.requireNonNull(fundamentalFrequencyComponent).getFundamentalFrequency();
    }

    public int getMasterVolume() {
        return Objects.requireNonNull(fundamentalFrequencyComponent).getMasterVolume();
    }

    public double getDurationInSeconds() {
        return UnitsConverter.convertMsToSeconds(durationInMs);
    }

    public int getDurationInMs() {
        return durationInMs;
    }

    public ArrayList<Overtone> getOvertones() {
        return Objects.requireNonNull(overtonesComponent).getOvertones();
    }

    public int getSamplesNumber() {
        return samples.length;
    }
}
