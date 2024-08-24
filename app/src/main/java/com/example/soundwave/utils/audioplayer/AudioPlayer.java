package com.example.soundwave.utils.audioplayer;

import android.media.AudioTrack;

public abstract class AudioPlayer {
    AudioTrack audioTrack;
    int sampleRate;
    int bufferSize;

    abstract void buildAudioTrack();

    public abstract void play();

    public abstract void stop();
}
