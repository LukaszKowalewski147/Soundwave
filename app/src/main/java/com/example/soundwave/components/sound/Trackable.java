package com.example.soundwave.components.sound;

import com.example.soundwave.utils.SampleRate;

public interface Trackable {

    SampleRate getSampleRate();

    double[] getSamples();

    double getDurationSeconds();

    int getSamplesNumber();
}
