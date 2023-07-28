package com.example.soundwave.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.soundwave.model.entity.Tone;
import com.example.soundwave.model.repository.SoundwaveRepo;

import java.util.List;

public class ToneCreatorViewModel extends AndroidViewModel {

    private SoundwaveRepo repository;

    private MutableLiveData<Integer> envelopeAttack = new MutableLiveData<>();
    private MutableLiveData<Integer> envelopeDecay = new MutableLiveData<>();
    private MutableLiveData<Integer> envelopeSustainLevel = new MutableLiveData<>();
    private MutableLiveData<Integer> envelopeSustainDuration = new MutableLiveData<>();
    private MutableLiveData<Integer> envelopeRelease = new MutableLiveData<>();
    private MutableLiveData<Integer> fundamentalFrequency = new MutableLiveData<>();
    private MutableLiveData<Integer> fundamentalFrequencyBar = new MutableLiveData<>();
    private MutableLiveData<String> scale = new MutableLiveData<>();
    private MutableLiveData<Integer> masterVolume = new MutableLiveData<>();
    private MutableLiveData<Integer> masterVolumeBar = new MutableLiveData<>();

    public ToneCreatorViewModel(@NonNull Application application) {
        super(application);
        repository = new SoundwaveRepo(application);
        initializeDefaultValues();
    }

    public LiveData<Integer> getEnvelopeAttack() {
        return envelopeAttack;
    }

    public LiveData<Integer> getEnvelopeDecay() {
        return envelopeDecay;
    }

    public LiveData<Integer> getEnvelopeSustainLevel() {
        return envelopeSustainLevel;
    }

    public LiveData<Integer> getEnvelopeSustainDuration() {
        return envelopeSustainDuration;
    }

    public LiveData<Integer> getEnvelopeRelease() {
        return envelopeRelease;
    }

    public LiveData<List<Tone>> getAllTones() {
        return allTones;
    }
}
