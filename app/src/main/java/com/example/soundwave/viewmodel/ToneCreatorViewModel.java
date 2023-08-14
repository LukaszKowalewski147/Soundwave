package com.example.soundwave.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.soundwave.SineWave;
import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.model.entity.Overtone;
import com.example.soundwave.model.entity.Tone;
import com.example.soundwave.model.repository.SoundwaveRepo;
import com.example.soundwave.utils.Config;
import com.example.soundwave.utils.Options;
import com.example.soundwave.utils.PresetEnvelope;
import com.example.soundwave.utils.PresetOvertones;
import com.example.soundwave.utils.UnitsConverter;

public class ToneCreatorViewModel extends AndroidViewModel {

    private SoundwaveRepo repository;

    private MutableLiveData<EnvelopeComponent> envelopeComponent = new MutableLiveData<>();

    private MutableLiveData<Integer> fundamentalFrequency = new MutableLiveData<>();
    private MutableLiveData<Integer> fundamentalFrequencyBar = new MutableLiveData<>();
    private MutableLiveData<Integer> masterVolume = new MutableLiveData<>();
    private MutableLiveData<Tone> tone = new MutableLiveData<>();
    private MutableLiveData<Overtone[]> overtones = new MutableLiveData<>();

    private int frequency;
    private int masterVolumeBar;
    private String scale;
    private boolean overtonesActivator;

    public ToneCreatorViewModel(@NonNull Application application) {
        super(application);
        repository = new SoundwaveRepo(application);
        initializeDefaultValues();
    }

    public LiveData<EnvelopeComponent> getEnvelopeComponent() {
        return envelopeComponent;
    }

    public LiveData<Integer> getFundamentalFrequency() {
        return fundamentalFrequency;
    }

    public LiveData<Integer> getFundamentalFrequencyBar() {
        return fundamentalFrequencyBar;
    }

    public LiveData<Integer> getMasterVolume() {
        return masterVolume;
    }

    public LiveData<Overtone[]> getOvertones() {
        return overtones;
    }

    public int getEnvelopePresetPosition() {
        return UnitsConverter.convertPresetEnvelopeToPosition(Options.envelopePreset);
    }

    public String getScale() {
        return scale;
    }

    public int getMasterVolumeBar() {
        return masterVolumeBar;
    }

    public int getOvertonesPresetPosition() {
        return UnitsConverter.convertPresetOvertonesToPosition(Options.overtonePreset);
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

    public void updateEnvelopePreset(int position) {
        PresetEnvelope targetEnvelopePreset = UnitsConverter.convertPositionToPresetEnvelope(position);
        if (targetEnvelopePreset == Options.envelopePreset)
            return;
        setEnvelopePreset(targetEnvelopePreset);
    }

    public void updateEnvelopeParameter(EnvelopeComponent.EnvelopeParameters parameter, String input) {
        input = input.trim();
        if (input.isEmpty())            // letting the user delete all digits and type from scratch
            return;

        int inputValue;
        int minValue = Config.ENVELOPE_PARAMETER_MIN_DURATION.value;
        int maxValue = Config.ENVELOPE_PARAMETER_MAX_DURATION.value;
        if (parameter == EnvelopeComponent.EnvelopeParameters.SUSTAIN_LEVEL) {
            minValue = Config.ENVELOPE_PARAMETER_MIN_LEVEL.value;
            maxValue = Config.ENVELOPE_PARAMETER_MAX_LEVEL.value;
        }

        try {
            inputValue = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            setEnvelopeParameter(parameter, minValue);
            setEnvelopePreset(PresetEnvelope.CUSTOM);
            return;
        }

        if (inputValue > maxValue) {
            setEnvelopeParameter(parameter, maxValue);
            return;
        }
        if (inputValue < minValue) {
            setEnvelopeParameter(parameter, minValue);
            return;
        }
        if (isEnvelopeValueDifferent(parameter, inputValue))
            setEnvelopeParameter(parameter, inputValue);
    }

    private boolean isEnvelopeValueDifferent(EnvelopeComponent.EnvelopeParameters parameter, int value) {
        switch (parameter) {
            case ATTACK_DURATION:
                return value != envelopeComponent.getValue().getAttackDuration();
            case DECAY_DURATION:
                return value != envelopeComponent.getValue().getDecayDuration();
            case SUSTAIN_LEVEL:
                return value != envelopeComponent.getValue().getSustainLevel();
            case SUSTAIN_DURATION:
                return value != envelopeComponent.getValue().getSustainDuration();
            case RELEASE_DURATION:
                return value != envelopeComponent.getValue().getReleaseDuration();
        }
        return true;
    }

    private void setEnvelopeParameter(EnvelopeComponent.EnvelopeParameters parameter, int value) {
        int currentAttackDuration = envelopeComponent.getValue().getAttackDuration();
        int currentDecayDuration = envelopeComponent.getValue().getDecayDuration();
        int currentSustainLevel = envelopeComponent.getValue().getSustainLevel();
        int currentSustainDuration = envelopeComponent.getValue().getSustainDuration();
        int currentReleaseDuration = envelopeComponent.getValue().getReleaseDuration();
        switch (parameter) {
            case ATTACK_DURATION:
                envelopeComponent.setValue(new EnvelopeComponent(value, currentDecayDuration, currentSustainLevel, currentSustainDuration, currentReleaseDuration));
                break;
            case DECAY_DURATION:
                envelopeComponent.setValue(new EnvelopeComponent(currentAttackDuration, value, currentSustainLevel, currentSustainDuration, currentReleaseDuration));
                break;
            case SUSTAIN_LEVEL:
                envelopeComponent.setValue(new EnvelopeComponent(currentAttackDuration, currentDecayDuration, value, currentSustainDuration, currentReleaseDuration));
                break;
            case SUSTAIN_DURATION:
                envelopeComponent.setValue(new EnvelopeComponent(currentAttackDuration, currentDecayDuration, currentSustainLevel, value, currentReleaseDuration));
                break;
            case RELEASE_DURATION:
                envelopeComponent.setValue(new EnvelopeComponent(currentAttackDuration, currentDecayDuration, currentSustainLevel, currentSustainDuration, value));
        }
        setEnvelopePreset(PresetEnvelope.CUSTOM);
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
        if (userFrequency == frequency)
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

    public void updateOvertonesPreset(int position) {
        PresetOvertones targetOvertonesPreset = UnitsConverter.convertPositionToPresetOvertones(position);
        if (targetOvertonesPreset == Options.overtonePreset)
            return;

        Options.overtonePreset = targetOvertonesPreset;

        Overtone[] newPresetOvertones = overtones.getValue();
        for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i)
            newPresetOvertones[i] = new Overtone(i, newPresetOvertones[i].getFrequency(), targetOvertonesPreset.amplitudes[i], newPresetOvertones[i].isActive());
        overtones.setValue(newPresetOvertones);
    }

    public void updateOvertoneAmplitude(int index, int amplitude) {
        Options.overtonePreset = PresetOvertones.CUSTOM;
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

    public String getIndexWithSuffix(int index) {
        switch (index) {
            case 1:
                return index + "st";
            case 2:
                return index + "nd";
            case 3:
                return index + "rd";
        }
        return index + "th";
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

    private void initializeDefaultValues() {
        setEnvelopePreset(PresetEnvelope.FLAT);
        setFrequencyComplex(Config.FREQUENCY_DEFAULT.value);
        setMasterVolume(100);
        setDefaultOvertones();
        setTone();
    }

    private void setTone() {
        tone.setValue(new Tone(fundamentalFrequency.getValue(), masterVolume.getValue()));
    }

    private void setDefaultOvertones() {
        Options.overtonePreset = PresetOvertones.FLAT;
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

    private void setEnvelopePreset(PresetEnvelope preset) {
        Options.envelopePreset = preset;

        if (preset == PresetEnvelope.CUSTOM) {
            PresetEnvelope.CUSTOM.values[0] = envelopeComponent.getValue().getAttackDuration();
            PresetEnvelope.CUSTOM.values[1] = envelopeComponent.getValue().getDecayDuration();
            PresetEnvelope.CUSTOM.values[2] = envelopeComponent.getValue().getSustainLevel();
            PresetEnvelope.CUSTOM.values[3] = envelopeComponent.getValue().getSustainDuration();
            PresetEnvelope.CUSTOM.values[4] = envelopeComponent.getValue().getReleaseDuration();
            return;
        }

        envelopeComponent.setValue(new EnvelopeComponent(preset.values[0], preset.values[1], preset.values[2], preset.values[3], preset.values[4]));
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
