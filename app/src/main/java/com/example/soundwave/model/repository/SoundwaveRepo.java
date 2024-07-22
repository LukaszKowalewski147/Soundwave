package com.example.soundwave.model.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.soundwave.model.dao.MusicDao;
import com.example.soundwave.model.dao.ToneDao;
import com.example.soundwave.model.entity.Music;
import com.example.soundwave.model.entity.Tone;
import com.example.soundwave.model.local.SoundwaveDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundwaveRepo {

    private final ExecutorService executorService;

    private final ToneDao toneDao;
    private final LiveData<List<Tone>> allTones;

    private MusicDao musicDao;
    private LiveData<List<Music>> allMusic;

    public SoundwaveRepo(Application application) {
        SoundwaveDatabase database = SoundwaveDatabase.getInstance(application);

        executorService = Executors.newSingleThreadExecutor();

        toneDao = database.toneDao();
        allTones = toneDao.getAllTones();

        musicDao = database.musicDao();
        allMusic = musicDao.getAllMusic();
    }

    public void insert(Tone tone) {
        executeDatabaseTask(() -> toneDao.insert(tone));
    }

    public void update(Tone tone) {
        executeDatabaseTask(() -> toneDao.update(tone));
    }

    public void delete(Tone tone) {
        executeDatabaseTask(() -> toneDao.delete(tone));
    }

    public void deleteAllTones() {
        executeDatabaseTask(toneDao::deleteAllTones);
    }

    public LiveData<List<Tone>> getAllTones() {
        return allTones;
    }

    public void insert(Music music) {
        executeDatabaseTask(() -> musicDao.insert(music));
    }

    public void update(Music music) {
        executeDatabaseTask(() -> musicDao.update(music));
    }

    public void delete(Music music) {
        executeDatabaseTask(() -> musicDao.delete(music));
    }

    public void deleteAllMusic() {
        executeDatabaseTask(() -> musicDao.deleteAllMusic());
    }

    public LiveData<List<Music>> getAllMusic() {
        return allMusic;
    }

    // Performs a database operation in the background
    private void executeDatabaseTask(Runnable task) {
        executorService.execute(task);
    }

    // Close executorService
    public void shutdownExecutorService() {
        executorService.shutdown();
    }
}