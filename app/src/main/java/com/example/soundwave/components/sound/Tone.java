package com.example.soundwave.components.sound;

import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.components.FundamentalFrequencyComponent;
import com.example.soundwave.components.OvertonesComponent;
import com.example.soundwave.utils.PresetEnvelope;
import com.example.soundwave.utils.PresetOvertones;
import com.example.soundwave.utils.SampleRate;
import com.example.soundwave.utils.UnitsConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class Tone extends ListenableSound implements Trackable, Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private final EnvelopeComponent envelopeComponent;
    private final FundamentalFrequencyComponent fundamentalFrequencyComponent;
    private final OvertonesComponent overtonesComponent;
    private final double[] samples;

    public Tone(SampleRate sampleRate, EnvelopeComponent ec, FundamentalFrequencyComponent ffc,
                OvertonesComponent oc, int durationMilliseconds, double[] samples, byte[] pcmData) {

        super("", sampleRate, durationMilliseconds, pcmData);
        this.id = -1;
        this.envelopeComponent = ec;
        this.fundamentalFrequencyComponent = ffc;
        this.overtonesComponent = oc;
        this.samples = samples;
    }

    @Override
    public double[] getSamples() {
        return samples;
    }

    @Override
    public double getDurationSeconds() {
        return UnitsConverter.convertMillisecondsToSeconds(durationMilliseconds);
    }

    @Override
    public int getSamplesNumber() {
        return samples.length;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
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

    public ArrayList<Overtone> getOvertones() {
        return Objects.requireNonNull(overtonesComponent).getOvertones();
    }
}
