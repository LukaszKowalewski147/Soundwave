package com.example.soundwave.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.soundwave.model.entity.Tone;
import com.example.soundwave.model.repository.SoundwaveRepo;
import com.example.soundwave.utils.AudioPlayer;
import com.example.soundwave.utils.ToneParser;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomepageViewModel extends AndroidViewModel {

    private SoundwaveRepo repository;
    private LiveData<List<Tone>> allTones;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public HomepageViewModel(@NonNull Application application) {
        super(application);
        repository = new SoundwaveRepo(application);
        allTones = repository.getAllTones();
    }

    public LiveData<List<Tone>> getAllTones() {
        return allTones;
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
        ToneParser parser = new ToneParser(tone);
        com.example.soundwave.Tone toneToPlay = parser.parseToneFromDb();
        AudioPlayer player = new AudioPlayer(toneToPlay);
        player.load();
        //player.stop();
       // player.reload();
        player.play();
    }
}
