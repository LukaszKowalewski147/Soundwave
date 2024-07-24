package com.example.soundwave.viewmodel;

import android.app.Application;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.soundwave.components.Tone;
import com.example.soundwave.model.repository.SoundwaveRepo;
import com.example.soundwave.utils.AudioPlayer;
import com.example.soundwave.utils.ToneParser;
import com.example.soundwave.utils.WavCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomepageTonesViewModel extends AndroidViewModel {

    private final SoundwaveRepo repository;
    private final LiveData<List<Tone>> allTones;
    private final MutableLiveData<Boolean> isTonePlaying = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private AudioPlayer currentAudioPlayer;         // null if no tone is playing
    private int currentlyPlayingTonePosition;       //   -1 if no tone is playing
    private int lastPlayedTonePosition;             //   -1 if no tone is playing

    private Handler handler;
    private Runnable runnable;

    public HomepageTonesViewModel(@NonNull Application application) {
        super(application);
        repository = new SoundwaveRepo(application);

        allTones = Transformations.map(repository.getAllTones(), input -> {
            List<Tone> tones = new ArrayList<>();
            for (com.example.soundwave.model.entity.Tone dbTone : input) {
                tones.add(new ToneParser().parseToneFromDb(dbTone));
            }
            return tones;
        });

        currentlyPlayingTonePosition = -1;
        lastPlayedTonePosition = -1;
    }

    @Override
    protected void onCleared() {
        repository.shutdownExecutorService();
        super.onCleared();
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

        tone.setName(toneNewName);

        com.example.soundwave.model.entity.Tone toneToUpdate = new ToneParser().parseToneToDbEntity(tone);
        toneToUpdate.setId(tone.getId());

        executorService.execute(() -> {
            try {
                repository.update(toneToUpdate);
                result.postValue(true);
            } catch (Exception e) {
                result.postValue(false);
            }
        });

        return result;
    }

    public LiveData<Boolean> deleteTone(Tone tone) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        com.example.soundwave.model.entity.Tone toneToDelete = new ToneParser().parseToneToDbEntity(tone);
        toneToDelete.setId(tone.getId());

        executorService.execute(() -> {
            try {
                repository.delete(toneToDelete);
                result.postValue(true);
            } catch (Exception e) {
                result.postValue(false);
            }
        });

        return result;
    }

    public boolean downloadTone(Tone tone) {
        WavCreator wavCreator = new WavCreator(tone);
        wavCreator.download();
        return wavCreator.isSuccess();
    }

    public boolean isTonePlaying(int position) {
        return currentlyPlayingTonePosition == position;
    }

    public boolean isAnyTonePlaying() {
        return currentAudioPlayer != null;
    }

    public void playTone(Tone tone, int position) {
        currentAudioPlayer = new AudioPlayer();
        currentAudioPlayer.loadTone(tone);
        currentAudioPlayer.play();

        currentlyPlayingTonePosition = position;
        lastPlayedTonePosition = position;
        isTonePlaying.postValue(true);

        handler = new Handler();
        runnable = () -> {
            if (isTonePlaying(position)) {
                stopTonePlaying();
                isTonePlaying.postValue(false);
            }
        };

        handler.postDelayed(runnable, tone.getDurationInMilliseconds());
    }

    public int stopTonePlaying() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }

        if (currentAudioPlayer != null) {
            currentAudioPlayer.stop();
            currentAudioPlayer = null;
        }

        currentlyPlayingTonePosition = -1;

        return lastPlayedTonePosition;
    }
}
