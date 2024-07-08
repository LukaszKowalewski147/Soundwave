package com.example.soundwave.utils;

import com.example.soundwave.components.Music;
import com.example.soundwave.components.Tone;
import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.components.FundamentalFrequencyComponent;
import com.example.soundwave.components.OvertonesComponent;
import com.example.soundwave.Overtone;
import com.example.soundwave.components.Track;

import java.util.ArrayList;
import java.util.List;

public class ToneGenerator {

    private final SampleRate sampleRate;

    private final int samplesNumber;
    private final double[] samples;
    private final byte[] outputSound;

    public ToneGenerator(SampleRate sampleRate, double totalDurationInSeconds) {
        this.sampleRate = sampleRate;

        samplesNumber = (int) Math.ceil(totalDurationInSeconds * sampleRate.sampleRate);
        samples = new double[samplesNumber];
        outputSound = new byte[2 * samplesNumber];      // 2 bytes of data for 16bit sample
    }

    public Tone generateTone(EnvelopeComponent ec, FundamentalFrequencyComponent ffc, OvertonesComponent oc) {
        int sampleRateInHz = sampleRate.sampleRate;
        double fundamentalFrequency = ffc.getFundamentalFrequency();

        // add fundamental frequency data
        for (int i = 0; i < samplesNumber; ++i) {
            samples[i] = Math.sin(2 * Math.PI * i / (sampleRateInHz / fundamentalFrequency));
        }

        if (oc.getOvertones() != null)
            addOvertonesData(sampleRateInHz, oc.getOvertones());

        compressToMasterVolume(ffc.getMasterVolume());
        applyEnvelope(ec);
        fadeOutFlatZero();
        convertTo16BitPCM();

        return new Tone(sampleRate, ec, ffc, oc, outputSound);
    }

    public Track generateTrack(List<Tone> tones) {
        int lastSampleIndex = 0;
        int toneIndex = 0;
        int toneSampleIndex = 0;

        byte[] toneSamples = tones.get(toneIndex).getSamples();

        for (int i = lastSampleIndex; i < outputSound.length; ++i) {
            outputSound[i] = toneSamples[toneSampleIndex++];
            if (toneSampleIndex == toneSamples.length && ++toneIndex < tones.size()) {
                toneSampleIndex = 0;
                toneSamples = tones.get(toneIndex).getSamples();
            }
        }

        return new Track(sampleRate, outputSound);
    }

    public Music generateMusic(List<Track> tracks) {
        int lastSampleIndex = 0;
        int trackIndex = 0;
        int trackSampleIndex = 0;

        byte[] trackSamples = tracks.get(0).getSamples();

        for (int i = lastSampleIndex; i < outputSound.length; ++i) {
            outputSound[i] = trackSamples[trackSampleIndex++];
            if (trackSampleIndex == trackSamples.length && ++trackIndex < tracks.size()) {
                trackSampleIndex = 0;
                trackSamples = tracks.get(trackIndex).getSamples();
            }
        }

        return new Music(sampleRate, outputSound);
    }

    private void addOvertonesData(int sampleRateInHz, ArrayList<Overtone> overtones) {
        for (Overtone overtone : overtones) {

            // !!! - Overtone output amplitude is calculated with 10^(x/20) where x is amplitude in dB from user input
            double overtoneAmplitude = Math.pow(10.0d, overtone.getAmplitude()/20.0d);
            double overtoneFrequency = overtone.getFrequency();

            for (int i = 0; i < samplesNumber; ++i)
                samples[i] += overtoneAmplitude * (Math.sin(2 * Math.PI * i / (sampleRateInHz / overtoneFrequency)));
        }
    }

    private void compressToMasterVolume(int masterVolume) {
        double masterVolumePercent = masterVolume / 100.0d; // master volume in %
        double maxVolumeSample = samples[0];

        for (int i = 1; i < samplesNumber; ++i) {
            if (samples[i] > maxVolumeSample)
                maxVolumeSample = samples[i];
        }

        double compressionRate = maxVolumeSample / masterVolumePercent;

        // compress to target volume
        for (int i = 0; i < samplesNumber; ++i) {
            samples[i] = samples[i] / compressionRate;
        }
    }

    private void applyEnvelope(EnvelopeComponent ec) {
        double sustainLevel = ec.getSustainLevel() / 100.0d;
        double totalTimeInMilliseconds = ec.getTotalDurationInMilliseconds();

        int attackInMilliseconds = ec.getAttackDuration();
        int decayInMilliseconds = ec.getDecayDuration();
        int sustainInMilliseconds = ec.getSustainDuration();
        int releaseInMilliseconds = ec.getReleaseDuration();

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
