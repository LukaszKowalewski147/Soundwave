package com.example.soundwave.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.soundwave.components.MixerComponent;
import com.example.soundwave.components.Music;
import com.example.soundwave.components.Tone;
import com.example.soundwave.components.Track;
import com.example.soundwave.model.repository.SoundwaveRepo;
import com.example.soundwave.utils.AudioPlayer;
import com.example.soundwave.utils.SampleRate;
import com.example.soundwave.utils.ToneGenerator;
import com.example.soundwave.utils.ToneParser;

import java.util.ArrayList;
import java.util.List;

public class ToneMixerViewModel extends AndroidViewModel {

    private final SoundwaveRepo repository;
    private final LiveData<List<Tone>> allTones;
    private final MutableLiveData<Music> music = new MutableLiveData<>();;
    private AudioPlayer audioPlayer;

    public ToneMixerViewModel(@NonNull Application application) {
        super(application);
        repository = new SoundwaveRepo(application);

        allTones = Transformations.map(repository.getAllTones(), input -> {
            List<Tone> tones = new ArrayList<>();
            for (com.example.soundwave.model.entity.Tone dbTone : input) {
                tones.add(new ToneParser().parseToneFromDb(dbTone));
            }
            return tones;
        });
    }

    @Override
    protected void onCleared() {
        if (audioPlayer != null) {
            audioPlayer.stop();
            audioPlayer = null;
        }
        repository.shutdownExecutorService();
        super.onCleared();
    }

    public LiveData<List<Tone>> getAllTones() {
        return allTones;
    }

    public LiveData<Music> getMusic() {
        return music;
    }

    public void generateMusic(MixerComponent mc) {
        SampleRate sampleRate = getLowestSampleRate(mc);
        List<Track> tracks = new ArrayList<>();

        List<Tone> track1Tones = mc.getTrack1Tones();
        List<Tone> track2Tones = mc.getTrack2Tones();
        List<Tone> track3Tones = mc.getTrack3Tones();
        List<Tone> track4Tones = mc.getTrack4Tones();
        List<Tone> track5Tones = mc.getTrack5Tones();

        if (!track1Tones.isEmpty())
            tracks.add(generateTrack(sampleRate, track1Tones));

        if (!track2Tones.isEmpty())
            tracks.add(generateTrack(sampleRate, track2Tones));

        if (!track3Tones.isEmpty())
            tracks.add(generateTrack(sampleRate, track3Tones));

        if (!track4Tones.isEmpty())
            tracks.add(generateTrack(sampleRate, track4Tones));

        if (!track5Tones.isEmpty())
            tracks.add(generateTrack(sampleRate, track5Tones));

        ToneGenerator generator = new ToneGenerator(sampleRate, getTrackDuration(track1Tones)); // TODO: get highestTrackDuration
        Music newMusic = generator.generateMusic(tracks);

        audioPlayer = new AudioPlayer();
        audioPlayer.loadMusic(newMusic);

        music.setValue(newMusic);
    }

    public void playStopMusic() {
        audioPlayer.stop();
        audioPlayer.reload();
        audioPlayer.play();
    }

    private Track generateTrack(SampleRate sampleRate, List<Tone> tones) {
        ToneGenerator generator = new ToneGenerator(sampleRate, getTrackDuration(tones));
        return generator.generateTrack(tones);
    }

    private SampleRate getLowestSampleRate(MixerComponent mc) {
        SampleRate lowestSampleRate = SampleRate.RATE_192_KHZ;
        List<Tone>[] trackTones = new List[5];

        trackTones[0] = mc.getTrack1Tones();
        trackTones[1] = mc.getTrack2Tones();
        trackTones[2] = mc.getTrack3Tones();
        trackTones[3] = mc.getTrack4Tones();
        trackTones[4] = mc.getTrack5Tones();

        for (List<Tone> trackTone : trackTones) {
            for (Tone tone : trackTone) {
                SampleRate currentSampleRate = tone.getSampleRate();

                if (currentSampleRate.sampleRate < lowestSampleRate.sampleRate)
                    lowestSampleRate = currentSampleRate;
                if (lowestSampleRate == SampleRate.RATE_44_1_KHZ)
                    return SampleRate.RATE_44_1_KHZ;
            }
        }

        return lowestSampleRate;
    }

    private double getTrackDuration(List<Tone> tones) {
        double totalDurationInSeconds = 0.0d;
        for (Tone tone : tones) {
            totalDurationInSeconds += tone.getDurationInSeconds();
        }
        return totalDurationInSeconds;
    }
}
