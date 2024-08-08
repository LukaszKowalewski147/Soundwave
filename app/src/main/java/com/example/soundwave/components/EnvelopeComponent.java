package com.example.soundwave.components;

import androidx.annotation.NonNull;

import com.example.soundwave.utils.PresetEnvelope;
import com.example.soundwave.utils.UnitsConverter;

import java.io.Serializable;

public class EnvelopeComponent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final PresetEnvelope envelopePreset;
    private final int attackDuration;
    private final int decayDuration;
    private final int sustainLevel;
    private final int sustainDuration;
    private final int releaseDuration;
    private final int envelopeDurationInMs;
    private final double envelopeDurationInS;

    public enum EnvelopeParameters {
        ATTACK_DURATION,
        DECAY_DURATION,
        SUSTAIN_LEVEL,
        SUSTAIN_DURATION,
        RELEASE_DURATION
    }

    public enum EnvelopePhase {
        ATTACK,
        DECAY,
        SUSTAIN,
        RELEASE
    }

    public EnvelopeComponent(PresetEnvelope envelopePreset, int attackDuration, int decayDuration,
                             int sustainLevel, int sustainDuration, int releaseDuration) {
        this.envelopePreset = envelopePreset;
        this.attackDuration = attackDuration;
        this.decayDuration = decayDuration;
        this.sustainLevel = sustainLevel;
        this.sustainDuration = sustainDuration;
        this.releaseDuration = releaseDuration;
        this.envelopeDurationInMs = calculateTotalDurationInMs();
        this.envelopeDurationInS = calculateTotalDurationInSeconds();
    }

    public PresetEnvelope getEnvelopePreset() {
        return envelopePreset;
    }

    public int getAttackDuration() {
        return attackDuration;
    }

    public int getDecayDuration() {
        return decayDuration;
    }

    public int getSustainLevel() {
        return sustainLevel;
    }

    public int getSustainDuration() {
        return sustainDuration;
    }

    public int getReleaseDuration() {
        return releaseDuration;
    }

    public int getEnvelopeDurationInMs() {
        return envelopeDurationInMs;
    }

    public double getEnvelopeDurationInS() {
        return envelopeDurationInS;
    }

    private int calculateTotalDurationInMs() {
        return attackDuration + decayDuration + sustainDuration + releaseDuration;
    }

    private double calculateTotalDurationInSeconds() {
        return UnitsConverter.convertMsToSeconds(envelopeDurationInMs);
    }

    @NonNull
    @Override
    public String toString() {
        return envelopePreset + "," + attackDuration + "," + decayDuration + "," +
                sustainLevel + "," + sustainDuration + "," + releaseDuration;
    }
}
