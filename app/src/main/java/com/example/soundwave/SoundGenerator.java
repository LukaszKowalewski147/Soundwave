package com.example.soundwave;

import java.util.Arrays;

public class SoundGenerator {
    private final SampleRate sampleRate;
    private final Tone[] tones;
    private final short duration;               // in s
    private final int samplesNumber;
    private final double[] samples;
    private final byte[] outputSound;

    public SoundGenerator(Tone[] tones, short duration, SampleRate sampleRate) {
        this.tones = tones;
        this.duration = duration;
        this.sampleRate = sampleRate;

        samplesNumber = duration * sampleRate.sampleRate;
        samples = new double[samplesNumber];
        outputSound = new byte[2 * samplesNumber];      // 2 bytes of data for 16bit sample
    }

    public Sound generateSound() {
        Arrays.fill(samples, 0.0d);
        double sampleRateInHz = sampleRate.sampleRate;

        for (Tone tone : tones) {
            int frequency = tone.getFundamentalFrequency();
            for (int j = 0; j < samplesNumber; ++j) {
                samples[j] += Math.sin(2 * Math.PI * j / (sampleRateInHz / frequency));
            }
        }

        for (int i = 0; i < samplesNumber; ++i) {
            samples[i] = samples[i] / tones.length;
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
        return new Sound(outputSound, 405, duration, sampleRate);
    }

    public void generateSoundSample() {
       // TODO: generate sound sample for streaming mode
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
