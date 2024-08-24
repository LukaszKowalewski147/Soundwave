package com.example.soundwave.utils.soundgenerator;

import com.example.soundwave.components.sound.Tone;
import com.example.soundwave.components.sound.Track;
import com.example.soundwave.components.sound.Trackable;
import com.example.soundwave.utils.SampleRate;

import java.util.ArrayList;
import java.util.List;

public class TrackGenerator extends SoundGenerator {

    public TrackGenerator(SampleRate sampleRate, int samplesNumber) {
        super(sampleRate, samplesNumber);
    }

    public Track generateTrack(List<Trackable> tones) {
        tones = prepareTonesForTrackGeneration(tones);
        reassignSamplesNumber(tones);

        int lastSampleIndex = 0;
        int toneIndex = 0;
        int toneSampleIndex = 0;

        Trackable tone = tones.get(toneIndex);
        double[] toneSamples = tone.getSamples();

        for (int i = lastSampleIndex; i < samples.length; ++i) {
            samples[i] = toneSamples[toneSampleIndex++];
            if (toneSampleIndex == toneSamples.length && ++toneIndex < tones.size()) {
                toneSampleIndex = 0;
                tone = tones.get(toneIndex);
                toneSamples = tone.getSamples();
            }
        }

        return new Track(sampleRate, samples);
    }

    private List<Trackable> prepareTonesForTrackGeneration(List<Trackable> tones) {
        List<Trackable> resampledTones = new ArrayList<>();

        for (Trackable tone : tones) {
            if (tone.getSampleRate() != sampleRate)
                tone = resampleTone(tone);
            resampledTones.add(tone);
        }
        return resampledTones;
    }

    private Trackable resampleTone(Trackable trackable) {
        if (trackable instanceof Tone) {
            Tone tone = (Tone) trackable;
            ToneGenerator generator = new ToneGenerator(sampleRate, trackable.getDurationSeconds());
            return generator.generateTone(tone.getEnvelopeComponent(), tone.getFundamentalFrequencyComponent(), tone.getOvertonesComponent());
        }
        SilenceGenerator generator = new SilenceGenerator(sampleRate, trackable.getDurationSeconds());
        return generator.generateSilence();
    }

    private void reassignSamplesNumber(List<Trackable> tones) {
        int trackSamplesNumber = 0;

        for (Trackable tone : tones)
            trackSamplesNumber += tone.getSamplesNumber();

        samplesNumber = trackSamplesNumber;
        samples = new double[trackSamplesNumber];
        pcmData = new byte[2 * trackSamplesNumber];      // 2 bytes of data for 16bit sample
    }
}
