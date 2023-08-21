package com.example.soundwave.utils;

import com.example.soundwave.Sound;
import com.example.soundwave.Tone;
import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.components.FundamentalFrequencyComponent;
import com.example.soundwave.model.entity.Overtone;

public class ToneGenerator {
    private final SampleRate sampleRate;
    private final EnvelopeComponent envelopeComponent;
    private final FundamentalFrequencyComponent fundamentalFrequencyComponent;
    private final Overtone[] overtones;

    private final int samplesNumber;
    private final double[] samples;
    private final byte[] outputSound;

    public ToneGenerator(SampleRate sampleRate, EnvelopeComponent envelopeComponent, FundamentalFrequencyComponent fundamentalFrequencyComponent, Overtone[] overtones) {
        this.sampleRate = sampleRate;
        this.envelopeComponent = envelopeComponent;
        this.fundamentalFrequencyComponent = fundamentalFrequencyComponent;
        this.overtones = overtones;

        samplesNumber = (int) Math.ceil(envelopeComponent.getTotalDurationInSeconds() * sampleRate.sampleRate);
        samples = new double[samplesNumber];
        outputSound = new byte[2 * samplesNumber];      // 2 bytes of data for 16bit sample
    }

    public ToneGenerator(SampleRate sampleRate, EnvelopeComponent envelopeComponent, FundamentalFrequencyComponent fundamentalFrequencyComponent) {
        this.sampleRate = sampleRate;
        this.envelopeComponent = envelopeComponent;
        this.fundamentalFrequencyComponent = fundamentalFrequencyComponent;
        this.overtones = null;

        samplesNumber = (int) Math.ceil(envelopeComponent.getTotalDurationInSeconds() * sampleRate.sampleRate);
        samples = new double[samplesNumber];
        outputSound = new byte[2 * samplesNumber];      // 2 bytes of data for 16bit sample
    }

    public Tone generateTone() {
        int sampleRateInHz = sampleRate.sampleRate;

        double masterVolume = fundamentalFrequencyComponent.getMasterVolume() / 100.0d;
        double fundamentalFrequency = fundamentalFrequencyComponent.getFundamentalFrequency();

        // add fundamental frequency data
        for (int i = 0; i < samplesNumber; ++i) {
            samples[i] = masterVolume * (Math.sin(2 * Math.PI * i / (sampleRateInHz / fundamentalFrequency)));
        }

        // add harmonics data
        if (overtones != null) {
            for (Overtone overtone : overtones) {
                int overtoneAmplitude = overtone.getAmplitude();
                double overtoneFrequency = overtone.getFrequency();

                for (int j = 0; j < samplesNumber; ++j) {
                    samples[j] += masterVolume * overtoneAmplitude * (Math.sin(2 * Math.PI * j / (sampleRateInHz / overtoneFrequency)));
                }
            }
        }

        // find max volume in samples
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

        // convert to 16 bit pcm sound array
        int index = 0;
        for (final double dVal : samples) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            outputSound[index++] = (byte) (val & 0x00ff);
            outputSound[index++] = (byte) ((val & 0xff00) >>> 8);
        }
        fadeIn();
        fadeOut();

        return new Tone(outputSound, (int) fundamentalFrequency, masterVolume, 1.0d, sampleRate);
    }

    public Sound generateSound(Tone tones) {
        //double[] toneData = tones.getSamples();
        //System.arraycopy(toneData, 0, samples, 0, samplesNumber);

        // convert to 16 bit pcm sound array
        int index = 0;
        for (final double dVal : samples) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            outputSound[index++] = (byte) (val & 0x00ff);
            outputSound[index++] = (byte) ((val & 0xff00) >>> 8);
        }
        fadeIn();
        fadeOut();
        int tmp_debug_volume = (int) (tones.getMasterVolume() * 100);
        return new Sound(outputSound, 1, (short) 1, sampleRate, tmp_debug_volume);
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
        return sampleRate.sampleRate / 50; // 0.02s
    }
}
