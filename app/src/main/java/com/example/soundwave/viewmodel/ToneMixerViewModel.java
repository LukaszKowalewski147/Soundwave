package com.example.soundwave.viewmodel;

import android.animation.ValueAnimator;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.soundwave.components.ControlPanelComponent;
import com.example.soundwave.components.sound.Music;
import com.example.soundwave.components.sound.Silence;
import com.example.soundwave.components.sound.Tone;
import com.example.soundwave.components.sound.Track;
import com.example.soundwave.components.sound.Trackable;
import com.example.soundwave.model.repository.SoundwaveRepo;
import com.example.soundwave.utils.DatabaseParser;
import com.example.soundwave.utils.Options;
import com.example.soundwave.utils.SampleRate;
import com.example.soundwave.utils.TrackData;
import com.example.soundwave.utils.TrackToneData;
import com.example.soundwave.audioplayer.AudioStaticPlayer;
import com.example.soundwave.soundgenerator.MusicGenerator;
import com.example.soundwave.soundgenerator.SilenceGenerator;
import com.example.soundwave.soundgenerator.TrackGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ToneMixerViewModel extends AndroidViewModel {

    private final SoundwaveRepo repository;
    private final LiveData<List<Tone>> allTones;
    private final MutableLiveData<Music> music = new MutableLiveData<>();
    private final MutableLiveData<ControlPanelComponent> controlPanelComponent = new MutableLiveData<>();

    private ValueAnimator track1animator;
    private ValueAnimator track2animator;
    private ValueAnimator track3animator;
    private ValueAnimator track4animator;
    private ValueAnimator track5animator;

    private AudioStaticPlayer audioPlayer;
    private boolean anyChange;

    public ToneMixerViewModel(@NonNull Application application) {
        super(application);
        repository = new SoundwaveRepo(application);

        allTones = Transformations.map(repository.getAllTones(), input -> {
            List<Tone> tones = new ArrayList<>();
            for (com.example.soundwave.model.entity.Tone dbTone : input) {
                tones.add(new DatabaseParser().parseToneFromDb(dbTone));
            }
            return tones;
        });

        initializeDefaultValues();
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

    private void initializeDefaultValues() {
        audioPlayer = null;
        setControlPanelComponentDefault();
        anyChange = false;
    }

    public LiveData<List<Tone>> getAllTones() {
        return allTones;
    }

    public LiveData<Music> getMusic() {
        return music;
    }

    public LiveData<ControlPanelComponent> getControlPanelComponent() {
        return controlPanelComponent;
    }

    public boolean getAnyChange() {
        return anyChange;
    }

    public void addToneToTrack() {
        setAnyChange();
    }

    public void removeToneFromTrack() {
        setAnyChange();
    }

    public boolean generateMusic(List<List<Trackable>> tracksData) {
        if (areTracksEmpty(tracksData)) {
            setControlPanelComponentDefault();
            anyChange = false;
            return true;
        }

        List<Track> tracks = new ArrayList<>();
        SampleRate sampleRate = getLowestSampleRate(tracksData);

        for (List<Trackable> trackTones : tracksData) {
            if (!trackTones.isEmpty())
                tracks.add(generateTrack(sampleRate, trackTones));
        }

        MusicGenerator generator = new MusicGenerator(sampleRate, getMusicSamplesNumber(tracks));
        Music newMusic = generator.generateMusic(tracks);

        audioPlayer = new AudioStaticPlayer();
        boolean loadingSuccessful = audioPlayer.loadSound(newMusic);

        if (loadingSuccessful) {
            music.setValue(newMusic);
            setControlPanelComponentMusicGenerated();
            return true;
        }
        return false;
    }

    public void playStopMusic() {
        HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStates = getControlPanelButtonsStates();
        if (buttonsStates.get(ControlPanelComponent.Button.PLAY_STOP) == ControlPanelComponent.ButtonState.STANDARD) {
            audioPlayer.stop();
            audioPlayer.reload();
            audioPlayer.play();

            setControlPanelComponentPlayMusic(buttonsStates);

            Thread thread = new Thread() {
                /** @noinspection CallToPrintStackTrace*/
                public void run() {
                    int waitingTime = Objects.requireNonNull(music.getValue()).getDurationMilliseconds();
                    try {
                        Thread.sleep(waitingTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStatesInThread = getControlPanelButtonsStates();
                    if (buttonsStatesInThread.get(ControlPanelComponent.Button.PLAY_STOP) == ControlPanelComponent.ButtonState.SECOND_FUNCTION)
                        setControlPanelComponentStopMusic(buttonsStatesInThread);
                }
            };
            thread.start();
            return;
        }
        audioPlayer.stop();

        setControlPanelComponentStopMusic(buttonsStates);
    }

    public void saveMusic(String musicName) {
        Music baseMusic = Objects.requireNonNull(music.getValue());
        baseMusic.setName(musicName);

        com.example.soundwave.model.entity.Music musicEntity =
                new DatabaseParser().parseMusicToDbEntity(baseMusic);

        repository.insert(musicEntity);

        setControlPanelComponentSaved();
        anyChange = false;
    }

    public Silence generateSilenceTone(double durationInSeconds) {
        return new SilenceGenerator(SampleRate.RATE_192_KHZ, durationInSeconds).generateSilence();
    }

    private boolean areTracksEmpty(List<List<Trackable>> tracksData) {
        for (List<Trackable> trackTones : tracksData) {
            if (!trackTones.isEmpty())
                return false;
        }
        return true;
    }

    private Track generateTrack(SampleRate sampleRate, List<Trackable> tones) {
        TrackGenerator generator = new TrackGenerator(sampleRate, getTrackSamplesNumber(tones));
        return generator.generateTrack(tones);
    }

    private SampleRate getLowestSampleRate(List<List<Trackable>> tracksData) {
        SampleRate lowestSampleRate = SampleRate.RATE_192_KHZ;

        for (List<Trackable> trackTones : tracksData) {
            for (Trackable tone : trackTones) {
                if (tone instanceof Silence)
                    continue;

                SampleRate currentSampleRate = tone.getSampleRate();

                if (currentSampleRate.sampleRate < lowestSampleRate.sampleRate)
                    lowestSampleRate = currentSampleRate;
                if (lowestSampleRate == SampleRate.RATE_44_1_KHZ)
                    return SampleRate.RATE_44_1_KHZ;
            }
        }

        return lowestSampleRate;
    }

    private int getTrackSamplesNumber(List<Trackable> tones) {
        int trackSamplesNumber = 0;

        for (Trackable tone : tones)
            trackSamplesNumber += tone.getSamplesNumber();

        return trackSamplesNumber;
    }

    private int getMusicSamplesNumber(List<Track> tracks) {
        int highestTrackSamplesNumber = 0;

        for (Track track : tracks) {
            int trackSamplesNumber = track.getNumberOfSamples();
            if (highestTrackSamplesNumber < trackSamplesNumber)
                highestTrackSamplesNumber = trackSamplesNumber;
        }

        return highestTrackSamplesNumber;   // music samples number = highest track samples number
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

    //  CONTROL PANEL
    private HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> getControlPanelButtonsStates() {
        return Objects.requireNonNull(controlPanelComponent.getValue()).getButtonsStates();
    }

    private void setControlPanelComponentDefault() {
        controlPanelComponent.setValue(new ControlPanelComponent(
                ControlPanelComponent.ButtonState.INACTIVE,
                ControlPanelComponent.ButtonState.INACTIVE,
                ControlPanelComponent.ButtonState.INACTIVE,
                ControlPanelComponent.ButtonState.INACTIVE));
    }

    private void setControlPanelComponentMusicGenerated() {
        controlPanelComponent.setValue(new ControlPanelComponent(
                ControlPanelComponent.ButtonState.INACTIVE,
                ControlPanelComponent.ButtonState.STANDARD,
                ControlPanelComponent.ButtonState.STANDARD,
                ControlPanelComponent.ButtonState.STANDARD));
    }

    private void setControlPanelComponentSaved() {
        HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStates = getControlPanelButtonsStates();
        ControlPanelComponent.ButtonState generateBtnState = buttonsStates.get(ControlPanelComponent.Button.GENERATE);
        ControlPanelComponent.ButtonState playStopBtnState = buttonsStates.get(ControlPanelComponent.Button.PLAY_STOP);
        ControlPanelComponent.ButtonState resetBtnState = buttonsStates.get(ControlPanelComponent.Button.RESET);

        controlPanelComponent.setValue(new ControlPanelComponent(
                generateBtnState,
                playStopBtnState,
                ControlPanelComponent.ButtonState.DONE,
                resetBtnState));
    }

    private void setControlPanelComponentPlayMusic(HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStates) {
        controlPanelComponent.setValue(new ControlPanelComponent(
                buttonsStates.get(ControlPanelComponent.Button.GENERATE),
                ControlPanelComponent.ButtonState.SECOND_FUNCTION,
                buttonsStates.get(ControlPanelComponent.Button.SAVE),
                buttonsStates.get(ControlPanelComponent.Button.RESET)));
    }

    private void setControlPanelComponentStopMusic(HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStates) {
        controlPanelComponent.postValue(new ControlPanelComponent(
                buttonsStates.get(ControlPanelComponent.Button.GENERATE),
                ControlPanelComponent.ButtonState.STANDARD,
                buttonsStates.get(ControlPanelComponent.Button.SAVE),
                buttonsStates.get(ControlPanelComponent.Button.RESET)));
    }

    private void setControlPanelComponentAnyChange(HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStates) {
        controlPanelComponent.setValue(new ControlPanelComponent(
                ControlPanelComponent.ButtonState.STANDARD,
                buttonsStates.get(ControlPanelComponent.Button.PLAY_STOP),
                buttonsStates.get(ControlPanelComponent.Button.SAVE),
                ControlPanelComponent.ButtonState.STANDARD));
    }

    private void setAnyChange() {
        anyChange = true;
        HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStates = getControlPanelButtonsStates();
        ControlPanelComponent.ButtonState generateBtnState = buttonsStates.get(ControlPanelComponent.Button.GENERATE);
        ControlPanelComponent.ButtonState resetBtnState = buttonsStates.get(ControlPanelComponent.Button.RESET);

        if (generateBtnState == ControlPanelComponent.ButtonState.STANDARD &&
                resetBtnState == ControlPanelComponent.ButtonState.STANDARD)
            return;

        setControlPanelComponentAnyChange(buttonsStates);
    }

    public int getMusicDurationMilliseconds() {
        return Objects.requireNonNull(music.getValue()).getDurationMilliseconds();
    }
}
