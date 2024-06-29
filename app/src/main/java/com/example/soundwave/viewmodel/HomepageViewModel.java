package com.example.soundwave.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.soundwave.model.entity.Tone;
import com.example.soundwave.model.repository.SoundwaveRepo;

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

    public void deleteTone(Tone tone) {
        repository.delete(tone);
    }
}
