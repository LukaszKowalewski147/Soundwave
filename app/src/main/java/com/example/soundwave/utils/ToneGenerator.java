package com.example.soundwave.utils;

import com.example.soundwave.Tone;
import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.components.FundamentalFrequencyComponent;
import com.example.soundwave.components.OvertonesComponent;
import com.example.soundwave.model.entity.Overtone;

public class ToneGenerator {
    private final SampleRate sampleRate;
    private final EnvelopeComponent envelopeComponent;
    private final FundamentalFrequencyComponent fundamentalFrequencyComponent;
    private final OvertonesComponent overtonesComponent;

    private final int samplesNumber;
    private final double[] samples;
    private final byte[] outputSound;

    public ToneGenerator(SampleRate sampleRate, EnvelopeComponent eC,
                         FundamentalFrequencyComponent ffc, OvertonesComponent oC) {
        this.sampleRate = sampleRate;
        this.envelopeComponent = eC;
        this.fundamentalFrequencyComponent = ffc;
        this.overtonesComponent = oC;

        samplesNumber = (int) Math.ceil(eC.getTotalDurationInSeconds() * sampleRate.sampleRate);
        samples = new double[samplesNumber];
        outputSound = new byte[2 * samplesNumber];      // 2 bytes of data for 16bit sample
    }

    public Tone generateTone() {
        int sampleRateInHz = sampleRate.sampleRate;
        double fundamentalFrequency = fundamentalFrequencyComponent.getFundamentalFrequency();

        // add fundamental frequency data
        for (int i = 0; i < samplesNumber; ++i) {
            samples[i] = Math.sin(2 * Math.PI * i / (sampleRateInHz / fundamentalFrequency));
        }

        if (overtonesComponent.getOvertones() != null)
            addOvertonesData(sampleRateInHz);

        compressToMasterVolume();
        applyEnvelope();
        fadeOutFlatZero();
        convertTo16BitPCM();

        return new Tone(sampleRate, envelopeComponent, fundamentalFrequencyComponent, overtonesComponent, outputSound);
    }

    private void addOvertonesData(int sampleRateInHz) {
        for (Overtone overtone : overtonesComponent.getOvertones()) {

            // !!! - Overtone output amplitude is calculated with 10^(x/20) where x is amplitude in dB from user input
            double overtoneAmplitude = Math.pow(10.0d, overtone.getAmplitude()/20.0d);
            double overtoneFrequency = overtone.getFrequency();

            for (int i = 0; i < samplesNumber; ++i)
                samples[i] += overtoneAmplitude * (Math.sin(2 * Math.PI * i / (sampleRateInHz / overtoneFrequency)));
        }
    }

    private void compressToMasterVolume() {
        double masterVolume = fundamentalFrequencyComponent.getMasterVolume() / 100.0d; // master volume in %
        double maxVolumeSample = samples[0];

        for (int i = 1; i < samplesNumber; ++i) {
            if (samples[i] > maxVolumeSample)
                maxVolumeSample = samples[i];
        }

        double compressionRate = maxVolumeSample / masterVolume;

        // compress to target volume
        for (int i = 0; i < samplesNumber; ++i) {
            samples[i] = samples[i] / compressionRate;
        }
    }

    private void applyEnvelope() {
        double sustainLevel = envelopeComponent.getSustainLevel() / 100.0d;
        double totalTimeInMilliseconds = envelopeComponent.getTotalDurationInMilliseconds();

        int attackInMilliseconds = envelopeComponent.getAttackDuration();
        int decayInMilliseconds = envelopeComponent.getDecayDuration();
        int sustainInMilliseconds = envelopeComponent.getSustainDuration();
        int releaseInMilliseconds = envelopeComponent.getReleaseDuration();

        double attackTimePercent = attackInMilliseconds * 100 / totalTimeInMilliseconds;
        double decayTimePercent = decayInMilliseconds * 100 / totalTimeInMilliseconds;
        double sustainTimePercent = sustainInMilliseconds * 100 / totalTimeInMilliseconds;
        double releaseTimePercent = releaseInMilliseconds * 100 / totalTimeInMilliseconds;

        int attackSamples = (int) Math.round(samplesNumber * attackTimePercent / 100);
        int decaySamples = (int) Math.round(samplesNumber * decayTimePercent / 100);
        int sustainSamples = (int) Math.round(samplesNumber * sustainTimePercent / 100);
        int releaseSamples = (int) Math.round(samplesNumber * releaseTimePercent / 100);

        double step;
        int startSample = 0;
        int endSample;

        //  Attack phase
        if (attackSamples > 0) {
            step = 1.0 / attackSamples;
            endSample = attackSamples;
            double attackFactor = 0.0d;

            for (int i = startSample; i < endSample; ++i) {
                samples[i] *= attackFactor;
                attackFactor = i * step;
            }
        }

        //  Decay phase
        if (decaySamples > 0) {
            startSample = attackSamples;
            endSample = startSample + decaySamples;
            EnvelopeCalculator calculator = new EnvelopeCalculator(EnvelopeComponent.EnvelopePhase.DECAY, decaySamples, sustainLevel);
            int sampleIndex = 0;
            for (int i = startSample; i < endSample; ++i) {
                samples[i] *= calculator.getMultiplier(sampleIndex);
                ++sampleIndex;
            }
        }

        //  Sustain phase
        if (sustainSamples > 0) {
            startSample = attackSamples + decaySamples;
            endSample = startSample + sustainSamples;

            for (int i = startSample; i < endSample; ++i) {
                samples[i] *= sustainLevel;
            }
        }

        //  Release phase
        if (releaseSamples > 0) {
            startSample = attackSamples + decaySamples + sustainSamples;
            EnvelopeCalculator calculator = new EnvelopeCalculator(EnvelopeComponent.EnvelopePhase.RELEASE, releaseSamples, sustainLevel);
            int sampleIndex = 0;

            for (int i = startSample; i < samplesNumber; ++i) {
                samples[i] *= calculator.getMultiplier(sampleIndex);
                ++sampleIndex;
            }
        }
    }

    private void convertTo16BitPCM() {
        int index = 0;
        for (final double dVal : samples) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            outputSound[index++] = (byte) (val & 0x00ff);
            outputSound[index++] = (byte) ((val & 0xff00) >>> 8);
        }
    }

    private void fadeOutFlatZero() {
        int lastCrossingIndex = getLastCrossingIndex();

        for (int i = lastCrossingIndex; i < samplesNumber; ++i)
           samples[i] = 0;
    }

    private int getLastCrossingIndex() {
        boolean isNegative = samples[samplesNumber - 1] < 0;

        if (isNegative) {
            for (int i = samplesNumber - 2; i > 0; --i) {
                if (samples[i] >= 0)
                    return i + 1;
            }
        } else {
            for (int i = samplesNumber - 2; i > 0; --i) {
                if (samples[i] < 0)
                    return i + 1;
            }
        }
        return samplesNumber - 1;
    }

    private void fadeIn() {
        int index = 0;
        int fadeDuration = getFadeDuration();
        double faderStep = getFaderStep();
        double fader = 0.0d;

        for (int i = 0; i < fadeDuration; ++i) {
            outputSound[index++] *= fader;
            outputSound[index++] *= fader;
            fader += faderStep;
        }
    }

    private void fadeOut() {
        int index = (samplesNumber * 2) - 1;
        int fadeDuration = getFadeDuration();
        double faderStep = getFaderStep();
        double fader = 0.0d;

        for (int i = samplesNumber - 1; i > samplesNumber - fadeDuration; --i) {
            outputSound[index--] *= fader;
            outputSound[index--] *= fader;
            fader += faderStep;
        }
    }

    private double getFaderStep() {
        int fadeDuration = getFadeDuration();
        return 1.0d / fadeDuration;
    }

    private int getFadeDuration() {
        return sampleRate.sampleRate / 1000; // 0.001s
    }
}
