package com.example.soundwave.viewmodel;

import android.app.Application;
import android.view.DragEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.soundwave.R;
import com.example.soundwave.components.MixerComponent;
import com.example.soundwave.components.Music;
import com.example.soundwave.components.Tone;
import com.example.soundwave.components.Track;
import com.example.soundwave.model.repository.SoundwaveRepo;
import com.example.soundwave.utils.AudioPlayer;
import com.example.soundwave.utils.SampleRate;
import com.example.soundwave.utils.ToneGenerator;
import com.example.soundwave.utils.ToneParser;
import com.example.soundwave.utils.TrackToneShadow;

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

    public List<Tone> getTonesFromTrack(LinearLayout track) {
        List<Tone> tones = new ArrayList<>();
        int childCount = track.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = track.getChildAt(i);
            Object tag = child.getTag(R.id.tag_tone);
            if (tag instanceof Tone)
                tones.add((Tone)tag);
        }
        return tones;
    }

    public boolean isToneDroppable(DragEvent event, LinearLayout track) {
        View tone = (View) event.getLocalState();
        int middleX = (int) event.getX();
        int width = tone.getWidth();
        int leftEdge = (int) Math.round(middleX - width / 2.0d);
        int rightEdge = leftEdge + width;

        for (int i = 0; i < track.getChildCount(); i++) {
            View child = track.getChildAt(i);

            Object tag = child.getTag(R.id.tag_silence_tone);
            if (tag == null || Boolean.TRUE.equals(tag))    //  Check if child is silence tone
                continue;

            if (tone.equals(child))
                continue;

            int childLeft = child.getLeft();
            int childRight = child.getRight();

            if (childRight >= leftEdge && childLeft <= rightEdge) {
                return false;
            }
        }
        return true;
    }

    public int[] getToneWithSilenceWidth(TrackToneShadow toneShadow, LinearLayout track) {
        if (isToneOnTheRight(toneShadow, track))
            return getToneWithSilenceAroundParameters(toneShadow, track);
        else
            return getToneWithSilenceBeforeParameters(toneShadow, track);
    }

    public boolean isToneOnTheRight(TrackToneShadow toneShadow, LinearLayout track) {
        if (track.getChildCount() == 0)
            return false;

        View child = track.getChildAt(track.getChildCount() - 1);
        return child.getLeft() > toneShadow.getRightEdge();
    }

    public View getOldSilenceTone(TrackToneShadow toneShadow, LinearLayout track) {
        for (int i = 0; i < track.getChildCount(); i++) {
            View child = track.getChildAt(i);
            int childLeft = child.getLeft();
            int childRight = child.getRight();

            if (childLeft <= toneShadow.getLeftEdge() && childRight >= toneShadow.getRightEdge())
                return child;
        }
        return null;
    }

    private int[] getToneWithSilenceAroundParameters(TrackToneShadow toneShadow, LinearLayout track) {
        View oldSilenceUnderTone = getOldSilenceTone(toneShadow, track);
        int oldSilenceUnderToneIndex = track.indexOfChild(oldSilenceUnderTone);
        int oldSilenceUnderToneWidth = track.getChildAt(oldSilenceUnderToneIndex).getWidth();
        int oldSilenceUnderToneLeftEdge = track.getChildAt(oldSilenceUnderToneIndex).getLeft();
        int beforeSilenceWidth = toneShadow.getLeftEdge() - oldSilenceUnderToneLeftEdge;
        int afterSilenceWidth = oldSilenceUnderToneWidth - toneShadow.getWidth() - beforeSilenceWidth;

        return new int[] {oldSilenceUnderToneIndex, beforeSilenceWidth, afterSilenceWidth};
    }

    private int[] getToneWithSilenceBeforeParameters(TrackToneShadow toneShadow, LinearLayout track) {
        int dropIndex = track.getChildCount();
        int rightEdgeOfLeftChild = findNearestLeftChildRightEdge(toneShadow, track);
        int beforeSilenceWidth = toneShadow.getLeftEdge() - rightEdgeOfLeftChild;

        return new int[] {dropIndex, beforeSilenceWidth, 0};
    }

    private int findNearestLeftChildRightEdge(TrackToneShadow toneShadow, LinearLayout track) {
        int rightEdgeOfLeftChild = toneShadow.getTrackPaddingStart();
        int leftEdge = toneShadow.getLeftEdge();

        for (int i = 0; i < track.getChildCount(); i++) {
            View child = track.getChildAt(i);
            int childRightEdge = child.getRight();
            if (childRightEdge < leftEdge) {
                rightEdgeOfLeftChild = childRightEdge;
                continue;
            }
            break;
        }
        return rightEdgeOfLeftChild;
    }
}
