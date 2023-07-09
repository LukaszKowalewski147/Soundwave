package com.example.soundwave;

import java.util.Arrays;

public class SoundGenerator {
    private final SampleRate sampleRate;
    private final short duration;               // in s
    private final int samplesNumber;
    private final double[] samples;
    private final byte[] outputSound;

    public SoundGenerator(short duration, SampleRate sampleRate) {
        this.duration = duration;
        this.sampleRate = sampleRate;

        samplesNumber = duration * sampleRate.sampleRate;
        samples = new double[samplesNumber];
        outputSound = new byte[2 * samplesNumber];      // 2 bytes of data for 16bit sample
    }

    public Sound generateSound(Tone[] tones) {
        Arrays.fill(samples, 0.0d);
        double soundVolume = tones[0].getVolume();

        for (Tone tone : tones) {
            double[] toneData = tone.getSamples();
            for (int i = 0; i < samplesNumber; ++i) {
                samples[i] += toneData[i];
            }
            if (tone.getVolume() > soundVolume)
                soundVolume = tone.getVolume();
        }

        // find max volume in samples
        double maxVolumeSample = samples[0];
        for (int i = 1; i < samplesNumber; ++i) {
            if (samples[i] > maxVolumeSample)
                maxVolumeSample = samples[i];
        }
        
        double compressionRate = maxVolumeSample / soundVolume;

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
        return new Sound(outputSound, tones.length, duration, sampleRate);
    }

    public static Tone generateTone(SineWave[] sineWaves) {
        int sampleRateInHz = Options.soundSampleRate;
        int duration = Options.soundDuration;
        int samplesNum = duration * sampleRateInHz;

        int[] frequencies = new int[sineWaves.length];
        double[] amplitudes = new double[sineWaves.length];
        double[] sampls = new double[samplesNum];

        for (int i = 0; i < sineWaves.length; ++i) {
            frequencies[i] = sineWaves[i].getFrequency();
            amplitudes[i] = sineWaves[i].getAmplitude();
        }

        double volume = amplitudes[0];

        // add fundamental frequency data
        for (int i = 0; i < samplesNum; ++i) {
            sampls[i] = volume * (Math.sin(2 * Math.PI * i / (sampleRateInHz / (double) frequencies[0])));
        }

        // add harmonics data
        if (sineWaves.length > 1) {
            for (int i = 1; i < sineWaves.length; ++i) {
                for (int j = 0; j < samplesNum; ++j) {
                    sampls[j] += volume * amplitudes[i] * (Math.sin(2 * Math.PI * j / (sampleRateInHz / (double) frequencies[i])));
                }
            }
        }

        // find max volume in samples
        double maxVolumeSample = sampls[0];
        for (int i = 1; i < samplesNum; ++i) {
           if (sampls[i] > maxVolumeSample)
               maxVolumeSample = sampls[i];
        }

        double compressionRate = maxVolumeSample / volume;

        // compress to target volume
        for (int i = 0; i < samplesNum; ++i) {
            sampls[i] = sampls[i] / compressionRate;
        }

        return new Tone(sampls, frequencies[0], volume);
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
