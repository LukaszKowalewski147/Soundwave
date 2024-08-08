package com.example.soundwave.utils;

import com.example.soundwave.components.Music;
import com.example.soundwave.components.Tone;
import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.components.FundamentalFrequencyComponent;
import com.example.soundwave.components.OvertonesComponent;
import com.example.soundwave.Overtone;
import com.example.soundwave.components.Track;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToneGenerator {

    private final SampleRate sampleRate;

    private int samplesNumber;
    private double[] samples;
    private byte[] outputSound;
    private final int durationInMs;

    public ToneGenerator(SampleRate sampleRate, double durationInSeconds) {
        this.sampleRate = sampleRate;

        this.samplesNumber = (int) Math.ceil(durationInSeconds * sampleRate.sampleRate);
        this.samples = new double[samplesNumber];
        this.outputSound = new byte[2 * samplesNumber];      // 2 bytes of data for 16bit sample

        this.durationInMs = UnitsConverter.convertSecondsToMs(durationInSeconds);
    }

    // Constructor to generate track and music
    public ToneGenerator(SampleRate sampleRate, int samplesNumber) {
        this.sampleRate = sampleRate;

        this.samplesNumber = samplesNumber;
        this.samples = new double[samplesNumber];
        this.outputSound = new byte[2 * samplesNumber];      // 2 bytes of data for 16bit sample

        this.durationInMs = 0;
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

        return new Tone(sampleRate, ec, ffc, oc, durationInMs, samples, outputSound);
    }

    public Tone generateSilence() {
        Arrays.fill(samples, 0.0d);

        convertTo16BitPCM();

        return new Tone(sampleRate, durationInMs, samples);
    }

    public Track generateTrack(List<Tone> tones) {
        tones = prepareTonesForTrackGeneration(tones);
        reassignSamplesNumber(tones);

        int lastSampleIndex = 0;
        int toneIndex = 0;
        int toneSampleIndex = 0;

        Tone tone = tones.get(toneIndex);
        double[] toneSamples = tone.getSamples();

        for (int i = lastSampleIndex; i < samples.length; ++i) {
            samples[i] = toneSamples[toneSampleIndex++];
            if (toneSampleIndex == toneSamples.length && ++toneIndex < tones.size()) {
                toneSampleIndex = 0;
                tone = tones.get(toneIndex);
                toneSamples = tone.getSamples();
            }
        }

        return new Track(sampleRate, samples);
    }

    public Music generateMusic(List<Track> tracks) {
        Arrays.fill(samples, 0.0d);     // Initialize samples with zeros.

        double[] trackSamples;

        for (Track track : tracks) {
            trackSamples = track.getSamples();

            for (int i = 0; i < samples.length; ++i) {
                if (i < trackSamples.length)
                    samples[i] += trackSamples[i];
                else
                    break;
            }
        }

        compressToMasterVolume(95);     //compressing to higher values than 95 induces cracking sound
        convertTo16BitPCM();

        return new Music(sampleRate, outputSound);
    }

    private List<Tone> prepareTonesForTrackGeneration(List<Tone> tones) {
        List<Tone> resampledTones = new ArrayList<>();

        for (Tone tone : tones) {
            if (tone.getSampleRate() != sampleRate)
                tone = resampleTone(tone);
            resampledTones.add(tone);
        }

        return resampledTones;
    }

    private Tone resampleTone(Tone tone) {
        ToneGenerator generator = new ToneGenerator(sampleRate, tone.getDurationInSeconds());

        if (tone.getFundamentalFrequencyComponent() == null)
            return generator.generateSilence();

        return generator.generateTone(tone.getEnvelopeComponent(), tone.getFundamentalFrequencyComponent(), tone.getOvertonesComponent());
    }

    private void reassignSamplesNumber(List<Tone> tones) {
        int trackSamplesNumber = 0;

        for (Tone tone : tones)
            trackSamplesNumber += tone.getSamplesNumber();

        this.samplesNumber = trackSamplesNumber;
        this.samples = new double[trackSamplesNumber];
        this.outputSound = new byte[2 * trackSamplesNumber];      // 2 bytes of data for 16bit sample
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
        double envelopeDurationInMs = ec.getEnvelopeDurationInMs();

        int attackInMs = ec.getAttackDuration();
        int decayInMs = ec.getDecayDuration();
        int sustainInMs = ec.getSustainDuration();
        int releaseInMs = ec.getReleaseDuration();

        double envelopeToToneRatio = envelopeDurationInMs / durationInMs;   // ratio between envelope and tone duration
        double timePercentMultiplier =  100 / envelopeDurationInMs * envelopeToToneRatio;   // phase percent of envelope times ratio

        double attackTimePercent = attackInMs * timePercentMultiplier;
        double decayTimePercent = decayInMs * timePercentMultiplier;
        double sustainTimePercent = sustainInMs * timePercentMultiplier;
        double releaseTimePercent = releaseInMs * timePercentMultiplier;

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
