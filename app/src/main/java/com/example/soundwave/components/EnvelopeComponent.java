package com.example.soundwave.components;

import androidx.annotation.NonNull;

import com.example.soundwave.utils.PresetEnvelope;
import com.example.soundwave.utils.UnitsConverter;

import java.io.Serializable;

public class EnvelopeComponent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final PresetEnvelope envelopePreset;
    private final int attackDurationMilliseconds;
    private final int decayDurationMilliseconds;
    private final int sustainLevelPercent;
    private final int sustainDurationMilliseconds;
    private final int releaseDurationMilliseconds;
    private final int envelopeDurationMilliseconds;
    private final double envelopeDurationSeconds;

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

    public EnvelopeComponent(PresetEnvelope envelopePreset, int attackDurationMilliseconds,
                             int decayDurationMilliseconds, int sustainLevelPercent,
                             int sustainDurationMilliseconds, int releaseDurationMilliseconds) {
        this.envelopePreset = envelopePreset;
        this.attackDurationMilliseconds = attackDurationMilliseconds;
        this.decayDurationMilliseconds = decayDurationMilliseconds;
        this.sustainLevelPercent = sustainLevelPercent;
        this.sustainDurationMilliseconds = sustainDurationMilliseconds;
        this.releaseDurationMilliseconds = releaseDurationMilliseconds;
        this.envelopeDurationMilliseconds = calculateTotalDurationMilliseconds();
        this.envelopeDurationSeconds = calculateTotalDurationSeconds();
    }

    public PresetEnvelope getEnvelopePreset() {
        return envelopePreset;
    }

    public int getAttackDurationMilliseconds() {
        return attackDurationMilliseconds;
    }

    public int getDecayDurationMilliseconds() {
        return decayDurationMilliseconds;
    }

    public int getSustainLevelPercent() {
        return sustainLevelPercent;
    }

    public int getSustainDurationMilliseconds() {
        return sustainDurationMilliseconds;
    }

    public int getReleaseDurationMilliseconds() {
        return releaseDurationMilliseconds;
    }

    public int getEnvelopeDurationMilliseconds() {
        return envelopeDurationMilliseconds;
    }

    public double getEnvelopeDurationSeconds() {
        return envelopeDurationSeconds;
    }

    private int calculateTotalDurationMilliseconds() {
        return attackDurationMilliseconds + decayDurationMilliseconds +
                sustainDurationMilliseconds + releaseDurationMilliseconds;
    }

    private double calculateTotalDurationSeconds() {
        return UnitsConverter.convertMillisecondsToSeconds(envelopeDurationMilliseconds);
    }

    @NonNull
    @Override
    public String toString() {
        return envelopePreset + "," + attackDurationMilliseconds + "," +
                decayDurationMilliseconds + "," + sustainLevelPercent + "," +
                sustainDurationMilliseconds + "," + releaseDurationMilliseconds;
    }
}
