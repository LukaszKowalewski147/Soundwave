package com.example.soundwave.viewmodel;

import android.app.Application;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.soundwave.model.entity.Tone;
import com.example.soundwave.model.repository.SoundwaveRepo;
import com.example.soundwave.utils.AudioPlayer;
import com.example.soundwave.utils.ToneParser;
import com.example.soundwave.utils.WavCreator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomepageViewModel extends AndroidViewModel {

    private SoundwaveRepo repository;
    private LiveData<List<Tone>> allTones;
    private MutableLiveData<Boolean> isTonePlaying = new MutableLiveData<>();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AudioPlayer currentAudioPlayer;         // null if no tone is playing
    private int currentlyPlayingTonePosition;       //   -1 if no tone is playing
    private int lastPlayedTonePosition;             //   -1 if no tone is playing

    public HomepageViewModel(@NonNull Application application) {
        super(application);
        repository = new SoundwaveRepo(application);
        allTones = repository.getAllTones();
        currentlyPlayingTonePosition = -1;
        lastPlayedTonePosition = -1;
    }

    public LiveData<List<Tone>> getAllTones() {
        return allTones;
    }

    public LiveData<Boolean> getIsTonePlaying() {
        return isTonePlaying;
    }

    public int getLastPlayedTonePosition() {
        return lastPlayedTonePosition;
    }

    public LiveData<Boolean> renameTone(Tone tone, String toneNewName) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        executorService.execute(() -> {
            try {
                tone.setName(toneNewName);
                repository.update(tone);
                result.postValue(true);
            } catch (Exception e) {
                result.postValue(false);
            }
        });

        return result;
    }

    public LiveData<Boolean> deleteTone(Tone tone) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        executorService.execute(() -> {
            try {
                repository.delete(tone);
                result.postValue(true);
            } catch (Exception e) {
                result.postValue(false);
            }
        });

        return result;
    }

    public boolean downloadTone(Tone tone) {
        com.example.soundwave.Tone toneToDownload = new ToneParser(tone).parseToneFromDb();
        WavCreator wavCreator = new WavCreator(toneToDownload);
        wavCreator.saveSound();
        return wavCreator.isSuccess();
    }

    public boolean isTonePlaying(int position) {
        return currentlyPlayingTonePosition == position;
    }

    public boolean isAnyTonePlaying() {
        return currentAudioPlayer != null;
    }

    public void playTone(Tone tone, int position) {
        com.example.soundwave.Tone toneToPlay = new ToneParser(tone).parseToneFromDb();
        currentAudioPlayer = new AudioPlayer(toneToPlay);
        currentAudioPlayer.load();
        currentAudioPlayer.play();

        currentlyPlayingTonePosition = position;
        lastPlayedTonePosition = position;
        isTonePlaying.postValue(true);

        new Handler().postDelayed(() -> {
            if (isTonePlaying(position)) {
                stopTonePlaying();
                isTonePlaying.postValue(false);
            }
        }, toneToPlay.getDurationInMilliseconds());
    }

    public int stopTonePlaying() {
        if (currentAudioPlayer != null) {
            currentAudioPlayer.stop();
            currentAudioPlayer = null;
        }
        currentlyPlayingTonePosition = -1;

        return lastPlayedTonePosition;
    }
}
