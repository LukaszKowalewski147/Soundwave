package com.example.soundwave.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.soundwave.components.FundamentalFrequencyComponent;
import com.example.soundwave.utils.AudioPlayer;
import com.example.soundwave.utils.Config;
import com.example.soundwave.utils.SampleRate;
import com.example.soundwave.utils.UnitsConverter;

import java.util.Objects;

public class ToneStreamingViewModel extends AndroidViewModel {
    private final String TAG = "ToneCreatorViewModel";

    private final MutableLiveData<FundamentalFrequencyComponent> fundamentalFrequencyComponent = new MutableLiveData<>();

    private AudioPlayer audioPlayer;

    public ToneStreamingViewModel(@NonNull Application application) {
        super(application);

        initializeDefaultValues();
    }

    public LiveData<FundamentalFrequencyComponent> getFundamentalFrequencyComponent() {
        return fundamentalFrequencyComponent;
    }

    public void startPlayback() {
        audioPlayer.startPlaying();
    }

    public void stopPlayback() {
        audioPlayer.stopPlaying();
    }

    public void updateNoteName(int noteIndex) {
        int frequency = FundamentalFrequencyComponent.getFrequencyOutOfNoteIndex(noteIndex);
        int volume = Objects.requireNonNull(fundamentalFrequencyComponent.getValue()).getMasterVolume();

        setFrequencyComplex(frequency, volume);
    }

    public void updateFundamentalFrequency(String input) {
        input = input.trim();
        if (input.isEmpty())            // letting the user delete all digits and type from scratch
            return;

        FundamentalFrequencyComponent ffc = Objects.requireNonNull(fundamentalFrequencyComponent.getValue());
        int userFrequency;

        try {
            userFrequency = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Fundamental frequency: user input (" + input + ") - not integer");
            setFrequencyComplex(Config.FREQUENCY_DEFAULT.value, Objects.requireNonNull(fundamentalFrequencyComponent.getValue()).getMasterVolume());
            return;
        }

        if (userFrequency == ffc.getFundamentalFrequency() || userFrequency < Config.FREQUENCY_MIN.value)
            return;

        int frequencyToSet = Math.min(userFrequency, Config.FREQUENCY_MAX.value);
        setFrequencyComplex(frequencyToSet, ffc.getMasterVolume());
    }

    public void updateFundamentalFrequencySeekBarPosition(int progress) {
        int frequency = UnitsConverter.convertSeekBarProgressToFrequency(progress);

        setFrequencyComplex(frequency, 100);
    }

    public void decrementOnceFundamentalFrequency() {
        FundamentalFrequencyComponent ffc = Objects.requireNonNull(fundamentalFrequencyComponent.getValue());
        int frequency = ffc.getFundamentalFrequency();

        if (--frequency >= Config.FREQUENCY_MIN.value)
            setFrequencyComplex(frequency, ffc.getMasterVolume());
    }

    public void incrementOnceFundamentalFrequency() {
        FundamentalFrequencyComponent ffc = Objects.requireNonNull(fundamentalFrequencyComponent.getValue());
        int frequency = ffc.getFundamentalFrequency();

        if (++frequency <= Config.FREQUENCY_MAX.value)
            setFrequencyComplex(frequency, ffc.getMasterVolume());
    }

    public void validateFundamentalFrequencyInput(String input) {
        FundamentalFrequencyComponent ffc = Objects.requireNonNull(fundamentalFrequencyComponent.getValue());
        int displayFrequency;

        try {
            displayFrequency = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Fundamental frequency: user input (" + input + ") - not integer");
            setFrequencyComplex(Config.FREQUENCY_DEFAULT.value, ffc.getMasterVolume());
            return;
        }

        if (displayFrequency < Config.FREQUENCY_MIN.value)
            setFrequencyComplex(Config.FREQUENCY_MIN.value, ffc.getMasterVolume());
    }

    private void setFrequencyComplex(int frequency, int volume) {
        fundamentalFrequencyComponent.setValue(new FundamentalFrequencyComponent(frequency, volume));
        setStreamFrequency();
    }

    private void setStreamFrequency() {
        audioPlayer.setStreamFrequency(Objects.requireNonNull(fundamentalFrequencyComponent.getValue()).getFundamentalFrequency());
    }

    private void initializeDefaultValues() {
        fundamentalFrequencyComponent.setValue(new FundamentalFrequencyComponent(
                Config.FREQUENCY_DEFAULT.value, Config.MASTER_VOLUME_DEFAULT.value));

        audioPlayer = new AudioPlayer(SampleRate.RATE_44_1_KHZ.sampleRate);
        setStreamFrequency();
    }
}
