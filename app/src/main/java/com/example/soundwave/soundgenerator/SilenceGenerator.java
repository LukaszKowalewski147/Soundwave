package com.example.soundwave.soundgenerator;

import com.example.soundwave.components.sound.Silence;
import com.example.soundwave.utils.SampleRate;
import com.example.soundwave.utils.UnitsConverter;

public class SilenceGenerator extends SoundGenerator {

    private final int durationMilliseconds;

    public SilenceGenerator(SampleRate sampleRate, double durationSeconds) {
        super(sampleRate, durationSeconds);
        this.durationMilliseconds = UnitsConverter.convertSecondsToMilliseconds(durationSeconds);
    }

    public Silence generateSilence() {
        return new Silence(sampleRate, durationMilliseconds, samples);
    }
}
