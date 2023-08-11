package com.example.soundwave.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.soundwave.OvertoneManager;
import com.example.soundwave.SineWave;
import com.example.soundwave.model.entity.Overtone;
import com.example.soundwave.model.entity.Tone;
import com.example.soundwave.model.repository.SoundwaveRepo;
import com.example.soundwave.utils.Config;
import com.example.soundwave.utils.Options;
import com.example.soundwave.utils.Preset;
import com.example.soundwave.utils.UnitsConverter;

public class ToneCreatorViewModel extends AndroidViewModel {

    private SoundwaveRepo repository;

    private MutableLiveData<Tone> tone = new MutableLiveData<>();
    private MutableLiveData<Overtone[]> overtones = new MutableLiveData<>();
    private MutableLiveData<Integer> fundamentalFrequency = new MutableLiveData<>();
    private MutableLiveData<Integer> fundamentalFrequencyBar = new MutableLiveData<>();
    private MutableLiveData<Integer> masterVolume = new MutableLiveData<>();
    private String scale;

    private int envelopeAttack;
    private int envelopeDecay;
    private int envelopeSustainLevel;
    private int envelopeSustainDuration;
    private int envelopeRelease;
    private int frequency;
    private int masterVolumeBar;
    private boolean overtonesActivator;

    public ToneCreatorViewModel(@NonNull Application application) {
        super(application);
        repository = new SoundwaveRepo(application);
        initializeDefaultValues();
    }

    public int getEnvelopeAttack() {
        return envelopeAttack;
    }

    public int getEnvelopeDecay() {
        return envelopeDecay;
    }

    public int getEnvelopeSustainLevel() {
        return envelopeSustainLevel;
    }

    public int getEnvelopeSustainDuration() {
        return envelopeSustainDuration;
    }

    public int getEnvelopeRelease() {
        return envelopeRelease;
    }

    public LiveData<Integer> getFundamentalFrequency() {
        return fundamentalFrequency;
    }

    public LiveData<Integer> getMasterVolume() {
        return masterVolume;
    }

    public String getScale() {
        return scale;
    }

    public LiveData<Integer> getFundamentalFrequencyBar() {
        return fundamentalFrequencyBar;
    }

    public int getMasterVolumeBar() {
        return masterVolumeBar;
    }

    public LiveData<Overtone[]> getOvertones() {
        return overtones;
    }

    public SineWave[] getSineWaves() {
        int activeOvertonesNumber = getActiveOvertonesNumber();
        SineWave[] sineWaves = new SineWave[activeOvertonesNumber + 1];
        sineWaves[0] = new SineWave(fundamentalFrequency.getValue(), getAmplitude());
        if (activeOvertonesNumber > 0) {
            SineWave[] overtones = getOvertonesSineWaves(activeOvertonesNumber);
            System.arraycopy(overtones, 0, sineWaves, 1, overtones.length);
        }
        return sineWaves;
    }

    private SineWave[] getOvertonesSineWaves(int activeOvertonesNumber) {
        /*
        SineWave[] overtones = new SineWave[activeOvertonesNumber];
        int overtoneIndex = 0;

        if (overtone1.getValue().isActive()) {
            overtones[overtoneIndex] = overtone1.getValue().getSineWave();
            ++overtoneIndex;
        }

        for (OvertoneManager overtoneManager : overtoneManagers) {
            if (overtoneManager.isActive()) {
                overtones[overtoneIndex] = overtoneManager.getSineWave();
                ++overtoneIndex;
            }
        }
        return overtones;*/
        return null;
    }

    private int getActiveOvertonesNumber() {
        int activeOvertonesNumber = 0;

        if (!overtonesActivator)
            return activeOvertonesNumber;

        return activeOvertonesNumber;
    }

    private double getAmplitude() {
        return (double) masterVolume.getValue() / 100.0d;
    }

    public void updateEnvelopeAttack(String input) {
        int userAttackDuration;
        try {
            userAttackDuration = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            envelopeAttack = Config.ENVELOPE_ATTACK_DEFAULT.value;
            return;
        }
        if (userAttackDuration >= 0 && userAttackDuration <= Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeAttack = userAttackDuration;
            return;
        }
        if (userAttackDuration > Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeAttack = Config.ENVELOPE_MAX_PARAMETER_TIME.value;
            return;
        }
        envelopeAttack = 0;
    }

    public void updateEnvelopeDecay(String input) {
        int userDecayDuration;
        try {
            userDecayDuration = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            envelopeDecay = Config.ENVELOPE_DECAY_DEFAULT.value;
            return;
        }
        if (userDecayDuration >= 0 && userDecayDuration <= Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeDecay = userDecayDuration;
            return;
        }
        if (userDecayDuration > Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeDecay = Config.ENVELOPE_MAX_PARAMETER_TIME.value;
            return;
        }
        envelopeDecay = 0;
    }

    public void updateEnvelopeSustainLevel(String input) {
        int userSustainLevel;
        try {
            userSustainLevel = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            envelopeSustainLevel = Config.ENVELOPE_SUSTAIN_LEVEL_DEFAULT.value;
            return;
        }
        if (userSustainLevel >= 0 && userSustainLevel <= 100) {
            envelopeSustainLevel = userSustainLevel;
            return;
        }
        if (userSustainLevel > 100) {
            envelopeSustainLevel = 100;
            return;
        }
        envelopeSustainLevel = 0;
    }

    public void updateEnvelopeSustainDuration(String input) {
        int userSustainDuration;
        try {
            userSustainDuration = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            envelopeSustainDuration = Config.ENVELOPE_SUSTAIN_DURATION_DEFAULT.value;
            return;
        }
        if (userSustainDuration >= 0 && userSustainDuration <= Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeSustainDuration = userSustainDuration;
            return;
        }
        if (userSustainDuration > Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeSustainDuration = Config.ENVELOPE_MAX_PARAMETER_TIME.value;
            return;
        }
        envelopeSustainDuration = 0;
    }

    public void updateEnvelopeRelease(String input) {
        int userReleaseDuration;
        try {
            userReleaseDuration = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            envelopeRelease = Config.ENVELOPE_RELEASE_DEFAULT.value;
            return;
        }
        if (userReleaseDuration >= 0 && userReleaseDuration <= Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeRelease = userReleaseDuration;
            return;
        }
        if (userReleaseDuration > Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeRelease = Config.ENVELOPE_MAX_PARAMETER_TIME.value;
            return;
        }
        envelopeRelease = 0;
    }

    public void updateFundamentalFrequency(String input) {
        int userFrequency;
        try {
            userFrequency = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            setFrequencyComplex(Config.FREQUENCY_DEFAULT.value);
            return;
        }
        if (userFrequency == frequency)                                     // TODO: endless update loop breaker
            return;
        if (userFrequency < Config.FREQUENCY_MIN.value) {
            setFundamentalFrequencyBar(Config.FREQUENCY_MIN.value);
            setScale(Config.FREQUENCY_MIN.value);
            return;
        }
        int frequencyToSet = Math.min(userFrequency, Config.FREQUENCY_MAX.value);
        setFrequencyComplex(frequencyToSet);
    }

    public void updateFundamentalFrequencySeekBarPosition(int progress) {
        setFrequencyComplex(UnitsConverter.convertSeekBarProgressToFrequency(progress));
    }

    public void decrementOnceFundamentalFrequency() {
        int frequency = fundamentalFrequency.getValue();
        if (--frequency >= Config.FREQUENCY_MIN.value)
            setFrequencyComplex(frequency);
    }

    public void decrementConstantlyFundamentalFrequency() {
        /*
        Options.buttonDecrementFrequencyState = Options.ButtonLongPressState.PRESSED;
        SeekBarUpdater seekBarUpdater = new SeekBarUpdater(new Handler(), fundamentalFrequencyInput, Options.Operation.FREQUENCY_DECREMENT);
        Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
        seekBarUpdaterThread.start();
        */
        //TODO: find a way to do this :/
    }

    public void incrementOnceFundamentalFrequency() {
        int frequency = fundamentalFrequency.getValue();
        if (++frequency <= Config.FREQUENCY_MAX.value)
            setFrequencyComplex(frequency);
    }

    public void incrementConstantlyFundamentalFrequency() {
        /*
        Options.buttonIncrementFrequencyState = Options.ButtonLongPressState.PRESSED;
        SeekBarUpdater seekBarUpdater = new SeekBarUpdater(new Handler(), fundamentalFrequencyInput, Options.Operation.FREQUENCY_INCREMENT);
        Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
        seekBarUpdaterThread.start();
        */
        //TODO: find a way to do this :/
    }

    public void updateMasterVolumeSeekBarPosition(int progress) {
        masterVolume.setValue(progress);
    }

    public void validateFundamentalFrequencyInput(String input) {
        int displayFrequency;
        try {
            displayFrequency = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            setFrequencyComplex(Config.FREQUENCY_DEFAULT.value);
            return;
        }
        if (displayFrequency < Config.FREQUENCY_MIN.value)
            setFrequencyComplex(Config.FREQUENCY_MIN.value);
    }

    public void updateOvertonesState(boolean isActive) {
        overtonesActivator = isActive;
    }

    public void updateOvertonesPreset(Preset preset) {
        Options.overtonePreset = preset;
        Overtone[] newPresetOvertones = overtones.getValue();
        for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i)
            newPresetOvertones[i] = new Overtone(i, newPresetOvertones[i].getFrequency(), Options.overtonePreset.amplitudes[i], newPresetOvertones[i].isActive());
        overtones.setValue(newPresetOvertones);
    }

    public void updateOvertoneAmplitude(int index, int amplitude) {
        Overtone[] updatedOvertones = overtones.getValue();
        for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i) {
            if (i == index)
                updatedOvertones[i] = new Overtone(i, updatedOvertones[i].getFrequency(), amplitude, updatedOvertones[i].isActive());
        }
        overtones.setValue(updatedOvertones);
    }

    public void updateOvertoneState(int index, boolean isActive) {
        Overtone[] updatedOvertones = overtones.getValue();
        for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i) {
            if (i == index)
                updatedOvertones[i] = new Overtone(i, updatedOvertones[i].getFrequency(), updatedOvertones[i].getAmplitude(), isActive);
        }
        overtones.setValue(updatedOvertones);
    }

    public void generateTone() {

    }

    public void playTone() {

    }

    public void saveTone() {

    }

    public void resetTone() {

    }

    private void initializeDefaultValues() {
        setFrequencyComplex(Config.FREQUENCY_DEFAULT.value);
        setMasterVolume(100);
        setDefaultOvertones();
        setTone();
    }

    private void setTone() {
        tone.setValue(new Tone(fundamentalFrequency.getValue(), masterVolume.getValue()));
    }

    private void setDefaultOvertones() {
        Options.overtonePreset = Preset.FLAT;
        Overtone[] defaultOvertones = new Overtone[Config.OVERTONES_NUMBER.value];
        for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i) {
            int overtoneFrequency = fundamentalFrequency.getValue() * (i + 2);
            defaultOvertones[i] = new Overtone(i, overtoneFrequency, Options.overtonePreset.amplitudes[i], true);
        }
        overtones.setValue(defaultOvertones);
    }

    private void updateOvertonesFrequency() {
        Overtone[] updatedOvertones = overtones.getValue();
        if (updatedOvertones == null)
            return;
        for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i) {
            int overtoneFrequency = fundamentalFrequency.getValue() * (i + 2);
            updatedOvertones[i] = new Overtone(i, overtoneFrequency, updatedOvertones[i].getAmplitude(), updatedOvertones[i].isActive());
        }
        overtones.setValue(updatedOvertones);
    }

    private void setMasterVolume(int volume) {
        masterVolume.setValue(volume);
        masterVolumeBar = volume;
    }

    private void setFrequencyComplex(int frequency) {
        this.frequency = frequency;
        setFundamentalFrequencyBar(frequency);
        setScale(frequency);
        fundamentalFrequency.setValue(frequency);
        updateOvertonesFrequency();
    }

    private void setFundamentalFrequencyBar(int frequency) {
        fundamentalFrequencyBar.setValue(UnitsConverter.convertFrequencyToSeekBarProgress(frequency));
    }

    private void setScale(int frequency) {
        scale = "B#4";
        //TODO: calculate and set proper scale based on frequency parameter
    }
}
