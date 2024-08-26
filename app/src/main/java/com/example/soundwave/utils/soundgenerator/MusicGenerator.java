package com.example.soundwave.utils.soundgenerator;

import com.example.soundwave.components.sound.Music;
import com.example.soundwave.components.sound.Track;
import com.example.soundwave.utils.SampleRate;

import java.util.List;

public class MusicGenerator extends SoundListenableGenerator {

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
}
