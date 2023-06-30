package com.example.soundwave;

public class SoundGenerator {
    private final int samplesNumber;
    private final int frequency;            // in Hz
    private final short duration;           // in s
    private final double[] samples;
    private final byte[] outputSound;
    private final SampleRate sampleRate;

    public SoundGenerator(int frequency, short duration, SampleRate sampleRate) {
        this.frequency = frequency;
        this.duration = duration;
        this.sampleRate = sampleRate;

        samplesNumber = duration * sampleRate.sampleRate;
        samples = new double[samplesNumber];
        outputSound = new byte[2 * samplesNumber];      // 2 bytes of data for 16bit sample
    }

    public Tone generateTone() {
        for (int i = 0; i < samplesNumber; ++i) {
            samples[i] = Math.sin(2 * Math.PI * i/(sampleRate.sampleRate /(double)frequency));
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
        return new Tone(outputSound, frequency, duration, sampleRate);
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
