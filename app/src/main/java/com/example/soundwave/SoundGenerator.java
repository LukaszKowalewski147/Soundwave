package com.example.soundwave;

public class SoundGenerator {
    private final int numberOfSamples;
    private final int frequency;        // in Hz
    private final short duration;         // in s
    private final double sample[];
    private final byte outputSound[];

    public SoundGenerator(int frequency, short duration) {
        this.frequency = frequency;
        this.duration = duration;

        numberOfSamples = duration * Constants.SAMPLE_RATE.value;
        sample = new double[numberOfSamples];
        outputSound = new byte[2 * numberOfSamples];
    }

    public Tone generateTone() {
        for (int i = 0; i < numberOfSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i/(Constants.SAMPLE_RATE.value/(double)frequency));
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
        return new Tone(outputSound, frequency, duration);
    }
}
