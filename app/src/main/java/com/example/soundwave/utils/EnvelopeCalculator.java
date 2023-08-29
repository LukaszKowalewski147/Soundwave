package com.example.soundwave.utils;

import com.example.soundwave.components.EnvelopeComponent;

public class EnvelopeCalculator {
    private final EnvelopeComponent.EnvelopePhase phase;
    private final int samplesNumber;
    private final double sustainLevelFactor;
    private double rangeMaxValue;
    private double rangeMinValue;
    private double rangeMax;
    private double rangeMin;
    private double rangeWidth;

    public EnvelopeCalculator(EnvelopeComponent.EnvelopePhase phase, int samplesNumber, double sustainLevelFactor) {
        this.phase = phase;
        this.samplesNumber = samplesNumber;
        this.sustainLevelFactor = sustainLevelFactor;
        assignRangeValues();
    }

    public double getMultiplier(int sampleIndex) {
        double progressPercent = (++sampleIndex * 100) / (double) samplesNumber;
        double inverseProportion = calculateInverseProportion(progressPercent) - rangeMinValue;
        double rangePercentFraction = inverseProportion / rangeMaxValue;
        double multiplierComponent = rangePercentFraction * rangeWidth;
        return rangeMin + multiplierComponent;
    }

    private double calculateInverseProportion(double progressPercent) {
        int slopeFactor = 500;
        int xAxisShift = 10;

        return slopeFactor / (progressPercent + xAxisShift);
    }

    private void assignRangeValues() {
        rangeMaxValue = calculateInverseProportion(0.0d);
        rangeMinValue = calculateInverseProportion(100.0d);
        rangeMaxValue -= rangeMinValue;

        switch (phase) {
            case DECAY:
                rangeMax = 1.0d;
                rangeMin = sustainLevelFactor;
                break;
            case RELEASE:
                rangeMax = sustainLevelFactor;
                rangeMin = 0.0d;
        }
        rangeWidth = rangeMax - rangeMin;
    }
}
