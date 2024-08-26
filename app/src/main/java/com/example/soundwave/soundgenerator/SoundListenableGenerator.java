package com.example.soundwave.soundgenerator;

import com.example.soundwave.utils.SampleRate;

public abstract class SoundListenableGenerator extends SoundGenerator {

    public SoundListenableGenerator(SampleRate sampleRate, double durationSeconds) {
        super(sampleRate, durationSeconds);
    }

    public SoundListenableGenerator(SampleRate sampleRate, int samplesNumber) {
        super(sampleRate, samplesNumber);
    }

    void compressToMasterVolume(int masterVolume) {
        double masterVolumePercent = masterVolume / 100.0d;     // master volume in %
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

    void convertTo16BitPCM() {
        int index = 0;
        for (final double dVal : samples) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            pcmData[index++] = (byte) (val & 0x00ff);
            pcmData[index++] = (byte) ((val & 0xff00) >>> 8);
        }
    }
}
