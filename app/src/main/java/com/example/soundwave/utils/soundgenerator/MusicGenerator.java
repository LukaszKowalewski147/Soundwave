package com.example.soundwave.utils.soundgenerator;

import com.example.soundwave.components.sound.Music;
import com.example.soundwave.components.sound.Track;
import com.example.soundwave.utils.SampleRate;

import java.util.List;

public class MusicGenerator extends SoundGenerator {

    public MusicGenerator(SampleRate sampleRate, int samplesNumber) {
        super(sampleRate, samplesNumber);
    }

    public Music generateMusic(List<Track> tracks) {
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

        return new Music(sampleRate, pcmData);
    }

    private void compressToMasterVolume(int masterVolume) {
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

    private void convertTo16BitPCM() {
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
