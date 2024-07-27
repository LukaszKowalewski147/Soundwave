package com.example.soundwave.viewmodel;

import android.app.Application;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.soundwave.components.Music;
import com.example.soundwave.model.repository.SoundwaveRepo;
import com.example.soundwave.utils.AudioPlayer;
import com.example.soundwave.utils.ToneParser;
import com.example.soundwave.utils.WavCreator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomepageMusicViewModel extends AndroidViewModel {

    private final SoundwaveRepo repository;
    private final LiveData<List<Music>> allMusic;
    private final MutableLiveData<Boolean> isMusicPlaying = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private AudioPlayer currentAudioPlayer;         // null if no music is playing
    private int currentlyPlayingMusicPosition;      //   -1 if no music is playing
    private int lastPlayedMusicPosition;            //   -1 if no music is playing

    private Handler handler;
    private Runnable runnable;

    public HomepageMusicViewModel(@NonNull Application application) {
        super(application);

        repository = new SoundwaveRepo(application);

        allMusic = Transformations.map(repository.getAllMusic(), input -> {
            List<Music> musicList = new ArrayList<>();
            for (com.example.soundwave.model.entity.Music dbMusic : input) {
                musicList.add(new ToneParser().parseMusicFromDb(dbMusic));
            }
            return musicList;
        });

        currentlyPlayingMusicPosition = -1;
        lastPlayedMusicPosition = -1;
    }

    @Override
    protected void onCleared() {
        repository.shutdownExecutorService();
        super.onCleared();
    }

    public LiveData<List<Music>> getAllMusic() {
        return allMusic;
    }

    public LiveData<Boolean> getIsMusicPlaying() {
        return isMusicPlaying;
    }

    public int getLastPlayedMusicPosition() {
        return lastPlayedMusicPosition;
    }

    public LiveData<Boolean> renameMusic(Music music, String musicNewName) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        music.setName(musicNewName);

        com.example.soundwave.model.entity.Music musicToUpdate = new ToneParser().parseMusicToDbEntity(music);
        musicToUpdate.setId(music.getId());

        executorService.execute(() -> {
            try {
                repository.update(musicToUpdate);
                result.postValue(true);
            } catch (Exception e) {
                result.postValue(false);
            }
        });

        return result;
    }

    public LiveData<Boolean> deleteMusic(Music music) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        com.example.soundwave.model.entity.Music musicToDelete = new ToneParser().parseMusicToDbEntityForDeletion(music);
        String samplesFilepath = repository.getSamplesFilepath(music.getId());

        if (deleteMusicSamplesFromStorage(samplesFilepath)) {
            executorService.execute(() -> {
                try {
                    repository.delete(musicToDelete);
                    result.postValue(true);
                } catch (Exception e) {
                    result.postValue(false);
                }
            });
        } else
            result.postValue(false);

        return result;
    }

    public boolean downloadMusic(Music music) {
        WavCreator wavCreator = new WavCreator(music);
        wavCreator.download();
        return wavCreator.isSuccess();
    }

    public boolean isMusicPlaying(int position) {
        return currentlyPlayingMusicPosition == position;
    }

    public boolean isAnyMusicPlaying() {
        return currentAudioPlayer != null;
    }

    public boolean playMusic(Music music, int position) {
        currentAudioPlayer = new AudioPlayer();
        boolean loadingSuccessful = currentAudioPlayer.loadMusic(music);

        if (!loadingSuccessful)
            return false;

        currentAudioPlayer.play();

        currentlyPlayingMusicPosition = position;
        lastPlayedMusicPosition = position;
        isMusicPlaying.postValue(true);

        handler = new Handler();
        runnable = () -> {
            if (isMusicPlaying(position)) {
                stopMusicPlaying();
                isMusicPlaying.postValue(false);
            }
        };

        handler.postDelayed(runnable, music.getDurationInMilliseconds());
        return true;
    }

    public int stopMusicPlaying() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }

        if (currentAudioPlayer != null) {
            currentAudioPlayer.stop();
            currentAudioPlayer = null;
        }

        currentlyPlayingMusicPosition = -1;

        return lastPlayedMusicPosition;
    }

    private boolean deleteMusicSamplesFromStorage(String samplesFilepath) {
        File file = new File(samplesFilepath);

        if (file.exists())
            return file.delete();
        else
            return false;
    }
}
