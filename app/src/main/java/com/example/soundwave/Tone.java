package com.example.soundwave;

import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.components.FundamentalFrequencyComponent;
import com.example.soundwave.components.OvertonesComponent;
import com.example.soundwave.utils.PresetEnvelope;
import com.example.soundwave.utils.PresetOvertones;
import com.example.soundwave.utils.SampleRate;

import java.io.Serializable;
import java.util.ArrayList;

public class Tone implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private final SampleRate sampleRate;
    private final EnvelopeComponent envelopeComponent;
    private final FundamentalFrequencyComponent fundamentalFrequencyComponent;
    private final OvertonesComponent overtonesComponent;
    private final byte[] samples;

    public Tone(SampleRate sampleRate, EnvelopeComponent ec, FundamentalFrequencyComponent ffc,
                OvertonesComponent oc, byte[] samples) {
        this.id = -1;
        this.name = "";
        this.sampleRate = sampleRate;
        this.envelopeComponent = ec;
        this.fundamentalFrequencyComponent = ffc;
        this.overtonesComponent = oc;
        this.samples = samples;
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

    public byte[] getSamples() {
        return samples;
    }

    public PresetEnvelope getEnvelopePreset() {
        return envelopeComponent.getEnvelopePreset();
    }

    public PresetOvertones getOvertonesPreset() {
        return overtonesComponent.getOvertonesPreset();
    }

    public int getFundamentalFrequency() {
        return fundamentalFrequencyComponent.getFundamentalFrequency();
    }

    public int getMasterVolume() {
        return fundamentalFrequencyComponent.getMasterVolume();
    }

    public double getDurationInSeconds() {
        return envelopeComponent.getTotalDurationInSeconds();
    }

    public int getDurationInMilliseconds() {
        return envelopeComponent.getTotalDurationInMilliseconds();
    }

    public ArrayList<Overtone> getOvertones() {
        return overtonesComponent.getOvertones();
    }
}
