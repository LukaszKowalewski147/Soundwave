package com.example.soundwave.components;

public class EnvelopeComponent {
    private final int attackDuration;
    private final int decayDuration;
    private final int sustainLevel;
    private final int sustainDuration;
    private final int releaseDuration;
    private final int totalDuration;

    public enum EnvelopeParameters {
        ATTACK_DURATION,
        DECAY_DURATION,
        SUSTAIN_LEVEL,
        SUSTAIN_DURATION,
        RELEASE_DURATION
    }

    public EnvelopeComponent(int attackDuration, int decayDuration, int sustainLevel, int sustainDuration, int releaseDuration) {
        this.attackDuration = attackDuration;
        this.decayDuration = decayDuration;
        this.sustainLevel = sustainLevel;
        this.sustainDuration = sustainDuration;
        this.releaseDuration = releaseDuration;
        this.totalDuration = calculateTotalDuration();
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

    public int getTotalDuration() {
        return totalDuration;
    }

    private int calculateTotalDuration() {
        return attackDuration + decayDuration + sustainDuration + releaseDuration;
    }
}
