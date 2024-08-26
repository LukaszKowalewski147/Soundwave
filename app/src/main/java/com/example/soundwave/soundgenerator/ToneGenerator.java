package com.example.soundwave.soundgenerator;

import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.components.FundamentalFrequencyComponent;
import com.example.soundwave.components.OvertonesComponent;
import com.example.soundwave.components.sound.Overtone;
import com.example.soundwave.components.sound.Tone;
import com.example.soundwave.utils.EnvelopeCalculator;
import com.example.soundwave.utils.SampleRate;
import com.example.soundwave.utils.UnitsConverter;

import java.util.ArrayList;

public class ToneGenerator extends SoundListenableGenerator {

    private final int durationMilliseconds;

    public ToneGenerator(SampleRate sampleRate, double durationSeconds) {
        super(sampleRate, durationSeconds);
        this.durationMilliseconds = UnitsConverter.convertSecondsToMilliseconds(durationSeconds);
    }

    public Tone generateTone(EnvelopeComponent ec, FundamentalFrequencyComponent ffc, OvertonesComponent oc) {
        int sampleRateInHz = sampleRate.sampleRate;
        double fundamentalFrequency = ffc.getFundamentalFrequency();

        // add fundamental frequency data
        for (int i = 0; i < samplesNumber; ++i) {
            samples[i] = Math.sin(2 * Math.PI * i / (sampleRateInHz / fundamentalFrequency));
        }

        if (oc.getOvertones() != null)
            addOvertonesData(sampleRateInHz, oc.getActiveOvertones());

        compressToMasterVolume(ffc.getMasterVolume());
        applyEnvelope(ec);
        fadeOutFlatZero();
        convertTo16BitPCM();

        return new Tone(sampleRate, ec, ffc, oc, durationMilliseconds, samples, pcmData);
    }

    private void addOvertonesData(int sampleRateInHz, ArrayList<Overtone> overtones) {
        for (Overtone overtone : overtones) {

            // !!! - Overtone output amplitude is calculated with 10^(x/20) where x is amplitude in dB from user input
            double overtoneAmplitude = Math.pow(10.0d, overtone.getAmplitude() / 20.0d);
            double overtoneFrequency = overtone.getFrequency();

            for (int i = 0; i < samplesNumber; ++i)
                samples[i] += overtoneAmplitude * (Math.sin(2 * Math.PI * i / (sampleRateInHz / overtoneFrequency)));
        }
    }

    private void applyEnvelope(EnvelopeComponent ec) {
        double sustainLevel = ec.getSustainLevelPercent() / 100.0d;
        double envelopeDurationMilliseconds = ec.getEnvelopeDurationMilliseconds();

        int attackMilliseconds = ec.getAttackDurationMilliseconds();
        int decayMilliseconds = ec.getDecayDurationMilliseconds();
        int sustainMilliseconds = ec.getSustainDurationMilliseconds();
        int releaseMilliseconds = ec.getReleaseDurationMilliseconds();

        double envelopeToToneRatio = envelopeDurationMilliseconds / durationMilliseconds;   // ratio between envelope and tone duration
        double timePercentMultiplier = 100 / envelopeDurationMilliseconds * envelopeToToneRatio;   // phase percent of envelope times ratio

        double attackTimePercent = attackMilliseconds * timePercentMultiplier;
        double decayTimePercent = decayMilliseconds * timePercentMultiplier;
        double sustainTimePercent = sustainMilliseconds * timePercentMultiplier;
        double releaseTimePercent = releaseMilliseconds * timePercentMultiplier;

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
                if (i == samplesNumber)
                    return;

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
                if (i == samplesNumber)
                    return;

                samples[i] *= calculator.getMultiplier(sampleIndex);
                ++sampleIndex;
            }
        }

        //  Sustain phase
        if (sustainSamples > 0) {
            startSample = attackSamples + decaySamples;
            endSample = startSample + sustainSamples;

            for (int i = startSample; i < endSample; ++i) {
                if (i == samplesNumber)
                    return;

                samples[i] *= sustainLevel;
            }
        }

        //  Release phase
        if (releaseSamples > 0) {
            startSample = attackSamples + decaySamples + sustainSamples;
            endSample = startSample + releaseSamples;
            EnvelopeCalculator calculator = new EnvelopeCalculator(EnvelopeComponent.EnvelopePhase.RELEASE, releaseSamples, sustainLevel);
            int sampleIndex = 0;

            for (int i = startSample; i < endSample; ++i) {
                if (i == samplesNumber)
                    return;

                samples[i] *= calculator.getMultiplier(sampleIndex);
                ++sampleIndex;
            }
        }

        // if envelope is shorter than tone - set the rest of the samples to zero
        startSample = attackSamples + decaySamples + sustainSamples + releaseSamples;
        for (int i = startSample; i < samplesNumber; ++i) {
            samples[i] = 0;
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
            pcmData[index++] *= fader;
            pcmData[index++] *= fader;
            fader += faderStep;
        }
    }

    private void fadeOut() {
        int index = (samplesNumber * 2) - 1;
        int fadeDuration = getFadeDuration();
        double faderStep = getFaderStep();
        double fader = 0.0d;

        for (int i = samplesNumber - 1; i > samplesNumber - fadeDuration; --i) {
            pcmData[index--] *= fader;
            pcmData[index--] *= fader;
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
