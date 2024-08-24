package com.example.soundwave.components.sound;

import com.example.soundwave.utils.SampleRate;

public interface Listenable {

    SampleRate getSampleRate();

    byte[] getPcmData();
}
