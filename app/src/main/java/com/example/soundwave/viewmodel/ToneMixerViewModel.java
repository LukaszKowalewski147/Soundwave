package com.example.soundwave.viewmodel;

import android.animation.ValueAnimator;
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
import com.example.soundwave.utils.Options;
import com.example.soundwave.utils.SampleRate;
import com.example.soundwave.utils.ToneGenerator;
import com.example.soundwave.utils.ToneParser;
import com.example.soundwave.utils.TrackData;
import com.example.soundwave.utils.TrackToneData;

import java.util.ArrayList;
import java.util.List;

public class ToneMixerViewModel extends AndroidViewModel {

    private final SoundwaveRepo repository;
    private final LiveData<List<Tone>> allTones;
    private final MutableLiveData<Music> music = new MutableLiveData<>();
    private AudioPlayer audioPlayer;

    private ValueAnimator track1animator;
    private ValueAnimator track2animator;
    private ValueAnimator track3animator;
    private ValueAnimator track4animator;
    private ValueAnimator track5animator;

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

        double musicDuration = getMusicDuration(tracks);

        ToneGenerator generator = new ToneGenerator(sampleRate, musicDuration);
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

    private double getMusicDuration(List<Track> tracks) {
        double longestTrackDurationInSeconds = 0.0d;

        for (Track track : tracks) {
            double trackDurationInSeconds = track.getTrackDurationInSeconds();
            if (longestTrackDurationInSeconds < trackDurationInSeconds)
                longestTrackDurationInSeconds = trackDurationInSeconds;
        }

        return longestTrackDurationInSeconds;   // music duration = longest track duration
    }

    //  TONE ADDING MANAGEMENT

    public int[] getToneWithSilenceParameters(TrackToneData toneShadow, TrackData track) {
        if (toneOnTheRightPresent(toneShadow, track))
            return getToneWithSilenceAroundParameters(toneShadow, track);
        else
            return getToneWithSilenceBeforeParameters(toneShadow, track);
    }

    public boolean toneOnTheRightPresent(TrackToneData toneShadow, TrackData track) {
        if (track.getChildCount() == 0)
            return false;

        TrackToneData child = track.getChildAt(track.getChildCount() - 1);
        return child.getLeft() > toneShadow.getRight();
    }

    public int getOldSilenceToneIndex(TrackToneData toneShadow, TrackData track) {
        for (int i = 0; i < track.getChildCount(); i++) {
            TrackToneData child = track.getChildAt(i);
            int childLeft = child.getLeft();
            int childRight = child.getRight();

            if (childLeft <= toneShadow.getLeft() && childRight >= toneShadow.getRight())
                return i;
        }
        return -1;
    }

    private int[] getToneWithSilenceAroundParameters(TrackToneData toneShadow, TrackData track) {
        int oldSilenceUnderToneIndex = getOldSilenceToneIndex(toneShadow, track);
        int oldSilenceUnderToneWidth = track.getChildAt(oldSilenceUnderToneIndex).getWidth();
        int oldSilenceUnderToneLeftEdge = track.getChildAt(oldSilenceUnderToneIndex).getLeft();
        int beforeSilenceWidth = toneShadow.getLeft() - oldSilenceUnderToneLeftEdge;
        int afterSilenceWidth = oldSilenceUnderToneWidth - toneShadow.getWidth() - beforeSilenceWidth;

        return new int[]{oldSilenceUnderToneIndex, beforeSilenceWidth, afterSilenceWidth};
    }

    private int[] getToneWithSilenceBeforeParameters(TrackToneData toneShadow, TrackData track) {
        int dropIndex = track.getChildCount();
        int rightEdgeOfLeftChild = findNearestLeftChildRightEdge(toneShadow, track);
        int beforeSilenceWidth = toneShadow.getLeft() - rightEdgeOfLeftChild;

        return new int[]{dropIndex, beforeSilenceWidth, 0};
    }

    private int findNearestLeftChildRightEdge(TrackToneData toneShadow, TrackData track) {
        int rightEdgeOfLeftChild = Options.trackPaddingStart;
        int leftEdge = toneShadow.getLeft();

        for (TrackToneData child : track.getChildren()) {
            int childRightEdge = child.getRight();
            if (childRightEdge < leftEdge) {
                rightEdgeOfLeftChild = childRightEdge;
                continue;
            }
            break;
        }
        return rightEdgeOfLeftChild;
    }

    //  ANIMATIONS

    public void stopPreviousTrackAnimation(int trackNumber) {
        switch (trackNumber) {
            case 1:
                if (track1animator != null && track1animator.isRunning())
                    track1animator.cancel();
                break;
            case 2:
                if (track2animator != null && track2animator.isRunning())
                    track2animator.cancel();
                break;
            case 3:
                if (track3animator != null && track3animator.isRunning())
                    track3animator.cancel();
                break;
            case 4:
                if (track4animator != null && track4animator.isRunning())
                    track4animator.cancel();
                break;
            case 5:
                if (track5animator != null && track5animator.isRunning())
                    track5animator.cancel();
                break;
        }
    }

    public void setCurrentTrackAnimation(int trackNumber, ValueAnimator animator) {
        switch (trackNumber) {
            case 1:
                track1animator = animator;
                break;
            case 2:
                track2animator = animator;
                break;
            case 3:
                track3animator = animator;
                break;
            case 4:
                track4animator = animator;
                break;
            case 5:
                track5animator = animator;
                break;
        }
    }
}
