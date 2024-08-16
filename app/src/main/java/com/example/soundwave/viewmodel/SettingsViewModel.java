package com.example.soundwave.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.soundwave.components.Music;
import com.example.soundwave.components.Tone;
import com.example.soundwave.model.repository.SoundwaveRepo;
import com.example.soundwave.utils.ToneParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsViewModel extends AndroidViewModel {

    private final SoundwaveRepo repository;
    private final LiveData<List<Tone>> allTones;
    private final LiveData<List<Music>> allMusic;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public SettingsViewModel(@NonNull Application application) {
        super(application);

        repository = new SoundwaveRepo(application);

        allTones = Transformations.map(repository.getAllTones(), input -> {
            List<Tone> tones = new ArrayList<>();
            for (com.example.soundwave.model.entity.Tone dbTone : input) {
                tones.add(new ToneParser().parseToneFromDb(dbTone));
            }
            return tones;
        });

        allMusic = Transformations.map(repository.getAllMusic(), input -> {
            List<Music> music = new ArrayList<>();
            for (com.example.soundwave.model.entity.Music dbMusic : input) {
                music.add(new ToneParser().parseMusicFromDb(dbMusic));
            }
            return music;
        });
    }

    public LiveData<List<Tone>> getAllTones() {
        return allTones;
    }

    public LiveData<List<Music>> getAllMusic() {
        return allMusic;
    }

    public int getNumberOfDbTones() {
        List<Tone> tones = allTones.getValue();

        if (tones != null)
            return tones.size();

        return 0;
    }

    public int getNumberOfDbMusic() {
        List<Music> music = allMusic.getValue();

        if (music != null)
            return music.size();

        return 0;
    }

    public LiveData<Boolean> deleteAllDatabaseTones() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        executorService.execute(() -> {
            try {
                repository.deleteAllTones();
                result.postValue(true);
            } catch (Exception e) {
                result.postValue(false);
            }
        });
        return result;
    }

    public LiveData<Boolean> deleteAllDatabaseMusic() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        executorService.execute(() -> {
            try {
                repository.deleteAllMusic();
                result.postValue(true);
            } catch (Exception e) {
                result.postValue(false);
            }
        });
        return result;
    }

    public float getDirectorySizeInMB(String directoryPath) {
        File directory = new File(directoryPath);
        long sizeInBytes = getDirectorySize(directory);

        return sizeInBytes / (1024f * 1024f);   // conversion to MB
    }

    private long getDirectorySize(File directory) {
        long length = 0;

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        length += file.length();
                    } else {
                        length += getDirectorySize(file);
                    }
                }
            }
        }
        return length;
    }
}
