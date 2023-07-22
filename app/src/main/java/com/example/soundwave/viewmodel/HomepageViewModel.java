package com.example.soundwave.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.soundwave.model.entity.Tone;
import com.example.soundwave.model.repository.SoundwaveRepo;

import java.util.List;

public class HomepageViewModel extends AndroidViewModel {

    private SoundwaveRepo repository;
    private LiveData<List<Tone>> allTones;

    public HomepageViewModel(@NonNull Application application) {
        super(application);
        repository = new SoundwaveRepo(application);
        allTones = repository.getAllTones();
    }

    public LiveData<List<Tone>> getAllTones() {
        return allTones;
    }
}
