package com.example.soundwave;

public class SoundGenerator {
    private final int samplesNumber;
    private final int frequency;            // in Hz
    private final short duration;           // in s
    private final double sample[];
    private final byte outputSound[];
    private SamplingRate samplingRate;

    public SoundGenerator(int frequency, short duration, SamplingRate samplingRate) {
        this.frequency = frequency;
        this.duration = duration;
        this.samplingRate = samplingRate;

        samplesNumber = duration * samplingRate.samplingRate;
        sample = new double[samplesNumber];
        outputSound = new byte[2 * samplesNumber];
    }

    public Tone generateTone() {
        for (int i = 0; i < samplesNumber; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i/(samplingRate.samplingRate/(double)frequency));
        }

        // convert to 16 bit pcm sound array
        int index = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            outputSound[index++] = (byte) (val & 0x00ff);
            outputSound[index++] = (byte) ((val & 0xff00) >>> 8);
        }
        fadeIn();
        fadeOut();
        return new Tone(outputSound, frequency, duration, samplingRate);
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
        return samplingRate.samplingRate / 50; // 0.02s
    }
}
