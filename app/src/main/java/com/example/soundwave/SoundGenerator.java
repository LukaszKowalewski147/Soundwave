package com.example.soundwave;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

public class SoundGenerator {
    private AudioTrack audioTrack;
    private final int sampleRate = 44100;   // in Hz (CD quality)
    private final int numberOfSamples;
    private final int frequency;         // in Hz
    private final double sample[];
    private final byte outputSound[];

    public SoundGenerator(int frequency, int duration) {
        this.frequency = frequency;
        numberOfSamples = duration * sampleRate;
        sample = new double[numberOfSamples];
        outputSound = new byte[2 * numberOfSamples];

        genTone();
    }

    public void play() {
        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(outputSound.length)
                .build();
        audioTrack.write(outputSound, 0, outputSound.length);
        audioTrack.play();
    }

    public void stop() {
        audioTrack.stop();
        audioTrack.release();
    }

    private void genTone() {
        for (int i = 0; i < numberOfSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i/(sampleRate/(double)frequency));
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
    }
}
