package com.example.soundwave.model.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import com.example.soundwave.model.dao.ToneDao;
import com.example.soundwave.model.entity.Tone;
import com.example.soundwave.model.local.SoundwaveDatabase;

import java.util.List;

public class SoundwaveRepo {

    private ToneDao toneDao;
    private LiveData<List<Tone>> allTones;

    //private MusicDao musicDao;
    //private LiveData<List<Music>> allMusic;

    public SoundwaveRepo(Application application) {
        SoundwaveDatabase database = SoundwaveDatabase.getInstance(application);

        toneDao = database.toneDao();
        allTones = toneDao.getAllTones();

        //musicDao = database.musicDao();
        //allMusic = musicDao.getAllMusic();
    }

    public void insert(Tone tone) {
        new InsertToneAsyncTask(toneDao).execute(tone);
    }

    public void update(Tone tone) {
        new UpdateToneAsyncTask(toneDao).execute(tone);
    }

    public void delete(Tone tone) {
        new DeleteToneAsyncTask(toneDao).execute(tone);
    }

    public void deleteAllTones() {
        new DeleteAllTonesAsyncTask(toneDao).execute();
    }

    public LiveData<List<Tone>> getAllTones() {
        return allTones;
    }
/*
    public void insert(Music music) {
        new InsertMusicAsyncTask(musicDao).execute(music);
    }

    public void update(Music music) {
        new UpdateMusicAsyncTask(musicDao).execute(music);
    }

    public void delete(Music music) {
        new DeleteMusicAsyncTask(musicDao).execute(music);
    }

    public void deleteAllMusic() {
        new DeleteAllMusicAsyncTask(musicDao).execute();
    }

    public LiveData<List<Music>> getAllMusic() {
        return allMusic;
    }
*/
    private static class InsertToneAsyncTask extends AsyncTask<Tone, Void, Void> {
        private ToneDao toneDao;

        private InsertToneAsyncTask(ToneDao toneDao) {
            this.toneDao = toneDao;
        }

        @Override
        protected Void doInBackground(Tone... tones) {
            toneDao.insert(tones[0]);
            return null;
        }
    }

    private static class UpdateToneAsyncTask extends AsyncTask<Tone, Void, Void> {
        private ToneDao toneDao;

        private UpdateToneAsyncTask(ToneDao toneDao) {
            this.toneDao = toneDao;
        }

        @Override
        protected Void doInBackground(Tone... tones) {
            toneDao.update(tones[0]);
            return null;
        }
    }

    private static class DeleteToneAsyncTask extends AsyncTask<Tone, Void, Void> {
        private ToneDao toneDao;

        private DeleteToneAsyncTask(ToneDao toneDao) {
            this.toneDao = toneDao;
        }

        @Override
        protected Void doInBackground(Tone... tones) {
            toneDao.delete(tones[0]);
            return null;
        }
    }

    private static class DeleteAllTonesAsyncTask extends AsyncTask<Void, Void, Void> {
        private ToneDao toneDao;

        private DeleteAllTonesAsyncTask(ToneDao toneDao) {
            this.toneDao = toneDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            toneDao.deleteAllTones();
            return null;
        }
    }
/*
    private static class InsertMusicAsyncTask extends AsyncTask<Music, Void, Void> {
        private MusicDao musicDao;

        private InsertMusicAsyncTask(MusicDao musicDao) {
            this.musicDao = musicDao;
        }

        @Override
        protected Void doInBackground(Music... music) {
            musicDao.insert(music[0]);
            return null;
        }
    }

    private static class UpdateMusicAsyncTask extends AsyncTask<Music, Void, Void> {
        private MusicDao musicDao;

        private UpdateMusicAsyncTask(MusicDao musicDao) {
            this.musicDao = musicDao;
        }

        @Override
        protected Void doInBackground(Music... music) {
            musicDao.update(music[0]);
            return null;
        }
    }

    private static class DeleteMusicAsyncTask extends AsyncTask<Music, Void, Void> {
        private MusicDao musicDao;

        private DeleteMusicAsyncTask(MusicDao musicDao) {
            this.musicDao = musicDao;
        }

        @Override
        protected Void doInBackground(Music... music) {
            musicDao.delete(music[0]);
            return null;
        }
    }

    private static class DeleteAllMusicAsyncTask extends AsyncTask<Void, Void, Void> {
        private MusicDao musicDao;

        private DeleteAllMusicAsyncTask(MusicDao musicDao) {
            this.musicDao = musicDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            musicDao.deleteAllMusic();
            return null;
        }
    }*/
}
