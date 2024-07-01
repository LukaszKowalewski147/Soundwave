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
    private Tone currentlyPlayingTone;
    private AudioPlayer currentAudioPlayer;

    public HomepageViewModel(@NonNull Application application) {
        super(application);
        repository = new SoundwaveRepo(application);
        allTones = repository.getAllTones();
    }

    public LiveData<List<Tone>> getAllTones() {
        return allTones;
    }

    public LiveData<Boolean> getIsTonePlaying() {
        return isTonePlaying;
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

    public void playStopTone(Tone tone) {
        if (isTonePlaying(tone)) {
            // Stop currently playing tone
            if (currentAudioPlayer != null) {
                currentAudioPlayer.stop();
                currentAudioPlayer = null;
                isTonePlaying.postValue(false);
            }
            currentlyPlayingTone = null;
        } else {
            // Stop the previous tone if it's playing
            if (currentAudioPlayer != null) {
                currentAudioPlayer.stop();
            }

            // Play the new tone
            com.example.soundwave.Tone toneToPlay = new ToneParser(tone).parseToneFromDb();
            currentAudioPlayer = new AudioPlayer(toneToPlay);
            currentAudioPlayer.load();
            currentAudioPlayer.play();
            currentlyPlayingTone = tone;

            isTonePlaying.postValue(true);
            new Handler().postDelayed(() -> {
                if (isTonePlaying(tone)) {
                    currentlyPlayingTone = null;
                    isTonePlaying.postValue(false);
                }
            }, toneToPlay.getDurationInMilliseconds());
        }
    }

    public boolean downloadTone(Tone tone) {
        com.example.soundwave.Tone toneToDownload = new ToneParser(tone).parseToneFromDb();
        WavCreator wavCreator = new WavCreator(toneToDownload);
        wavCreator.saveSound();
        return wavCreator.isSuccess();
    }

    public boolean isTonePlaying(Tone tone) {
        return currentlyPlayingTone != null && currentlyPlayingTone.equals(tone);
    }
}
