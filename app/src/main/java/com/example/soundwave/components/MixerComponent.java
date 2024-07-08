package com.example.soundwave.components;

import java.util.List;

public class MixerComponent {
    private final List<Tone> track1Tones;
    private final List<Tone> track2Tones;
    private final List<Tone> track3Tones;
    private final List<Tone> track4Tones;
    private final List<Tone> track5Tones;

    public MixerComponent(List<Tone> track1Tones, List<Tone> track2Tones, List<Tone> track3Tones, List<Tone> track4Tones, List<Tone> track5Tones) {
        this.track1Tones = track1Tones;
        this.track2Tones = track2Tones;
        this.track3Tones = track3Tones;
        this.track4Tones = track4Tones;
        this.track5Tones = track5Tones;
    }

    public List<Tone> getTrack1Tones() {
        return track1Tones;
    }

    public List<Tone> getTrack2Tones() {
        return track2Tones;
    }

    public List<Tone> getTrack3Tones() {
        return track3Tones;
    }

    public List<Tone> getTrack4Tones() {
        return track4Tones;
    }

    public List<Tone> getTrack5Tones() {
        return track5Tones;
    }
}
