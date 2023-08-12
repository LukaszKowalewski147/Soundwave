package com.example.soundwave.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.soundwave.SineWave;
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

    private MutableLiveData<Integer> envelopeAttack = new MutableLiveData<>();
    private MutableLiveData<Integer> envelopeDecay = new MutableLiveData<>();
    private MutableLiveData<Integer> envelopeSustainLevel = new MutableLiveData<>();
    private MutableLiveData<Integer> envelopeSustainDuration = new MutableLiveData<>();
    private MutableLiveData<Integer> envelopeRelease = new MutableLiveData<>();

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
        setEnvelopeComplex(targetEnvelopePreset);
    }

    public void updateEnvelopeAttack(String input) {
        int userAttackDuration;
        try {
            userAttackDuration = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            if (input.isEmpty())            // letting the user delete all digits and type from scratch
                return;
            envelopeAttack.setValue(Config.ENVELOPE_ATTACK_DEFAULT.value);
            setEnvelopeComplex(PresetEnvelope.CUSTOM);
            return;
        }
        if (userAttackDuration == envelopeAttack.getValue().intValue())
            return;
        if (userAttackDuration >= 0 && userAttackDuration <= Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeAttack.setValue(userAttackDuration);
            setEnvelopeComplex(PresetEnvelope.CUSTOM);
            return;
        }
        if (userAttackDuration > Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeAttack.setValue(Config.ENVELOPE_MAX_PARAMETER_TIME.value);
            setEnvelopeComplex(PresetEnvelope.CUSTOM);
            return;
        }
        envelopeAttack.setValue(0);
        setEnvelopeComplex(PresetEnvelope.CUSTOM);
    }

    public void updateEnvelopeDecay(String input) {
        int userDecayDuration;
        try {
            userDecayDuration = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            if (input.isEmpty())            // letting the user delete all digits and type from scratch
                return;
            envelopeDecay.setValue(Config.ENVELOPE_DECAY_DEFAULT.value);
            setEnvelopeComplex(PresetEnvelope.CUSTOM);
            return;
        }
        if (userDecayDuration == envelopeDecay.getValue().intValue())
            return;
        if (userDecayDuration >= 0 && userDecayDuration <= Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeDecay.setValue(userDecayDuration);
            setEnvelopeComplex(PresetEnvelope.CUSTOM);
            return;
        }
        if (userDecayDuration > Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeDecay.setValue(Config.ENVELOPE_MAX_PARAMETER_TIME.value);
            setEnvelopeComplex(PresetEnvelope.CUSTOM);
            return;
        }
        envelopeDecay.setValue(0);
        setEnvelopeComplex(PresetEnvelope.CUSTOM);
    }

    public void updateEnvelopeSustainLevel(String input) {
        int userSustainLevel;
        try {
            userSustainLevel = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            if (input.isEmpty())            // letting the user delete all digits and type from scratch
                return;
            envelopeSustainLevel.setValue(Config.ENVELOPE_SUSTAIN_LEVEL_DEFAULT.value);
            setEnvelopeComplex(PresetEnvelope.CUSTOM);
            return;
        }
        if (userSustainLevel == envelopeSustainLevel.getValue().intValue())
            return;
        if (userSustainLevel >= 0 && userSustainLevel <= 100) {
            envelopeSustainLevel.setValue(userSustainLevel);
            setEnvelopeComplex(PresetEnvelope.CUSTOM);
            return;
        }
        if (userSustainLevel > 100) {
            envelopeSustainLevel.setValue(100);
            setEnvelopeComplex(PresetEnvelope.CUSTOM);
            return;
        }
        envelopeSustainLevel.setValue(0);
        setEnvelopeComplex(PresetEnvelope.CUSTOM);
    }

    public void updateEnvelopeSustainDuration(String input) {
        int userSustainDuration;
        try {
            userSustainDuration = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            if (input.isEmpty())            // letting the user delete all digits and type from scratch
                return;
            envelopeSustainDuration.setValue(Config.ENVELOPE_SUSTAIN_DURATION_DEFAULT.value);
            setEnvelopeComplex(PresetEnvelope.CUSTOM);
            return;
        }
        if (userSustainDuration == envelopeSustainDuration.getValue().intValue())
            return;
        if (userSustainDuration >= 0 && userSustainDuration <= Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeSustainDuration.setValue(userSustainDuration);
            setEnvelopeComplex(PresetEnvelope.CUSTOM);
            return;
        }
        if (userSustainDuration > Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeSustainDuration.setValue(Config.ENVELOPE_MAX_PARAMETER_TIME.value);
            setEnvelopeComplex(PresetEnvelope.CUSTOM);
            return;
        }
        envelopeSustainDuration.setValue(0);
        setEnvelopeComplex(PresetEnvelope.CUSTOM);
    }

    public void updateEnvelopeRelease(String input) {
        int userReleaseDuration;
        try {
            userReleaseDuration = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            if (input.isEmpty())            // letting the user delete all digits and type from scratch
                return;
            envelopeRelease.setValue(Config.ENVELOPE_RELEASE_DEFAULT.value);
            setEnvelopeComplex(PresetEnvelope.CUSTOM);
            return;
        }
        if (userReleaseDuration == envelopeRelease.getValue().intValue())
            return;
        if (userReleaseDuration >= 0 && userReleaseDuration <= Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeRelease.setValue(userReleaseDuration);
            setEnvelopeComplex(PresetEnvelope.CUSTOM);
            return;
        }
        if (userReleaseDuration > Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeRelease.setValue(Config.ENVELOPE_MAX_PARAMETER_TIME.value);
            setEnvelopeComplex(PresetEnvelope.CUSTOM);
            return;
        }
        envelopeRelease.setValue(0);
        setEnvelopeComplex(PresetEnvelope.CUSTOM);
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
        setEnvelopeComplex(PresetEnvelope.FLAT);
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

    private void setEnvelopeComplex(PresetEnvelope preset) {
        Options.envelopePreset = preset;

        if (preset == PresetEnvelope.CUSTOM) {
            setCustomEnvelopePresetValues();
            return;
        }
        envelopeAttack.setValue(preset.values[0]);
        envelopeDecay.setValue(preset.values[1]);
        envelopeSustainLevel.setValue(preset.values[2]);
        envelopeSustainDuration.setValue(preset.values[3]);
        envelopeRelease.setValue(preset.values[4]);
    }

    private void setCustomEnvelopePresetValues() {
        PresetEnvelope.CUSTOM.values[0] = envelopeAttack.getValue().intValue();
        PresetEnvelope.CUSTOM.values[1] = envelopeDecay.getValue().intValue();
        PresetEnvelope.CUSTOM.values[2] = envelopeSustainLevel.getValue().intValue();
        PresetEnvelope.CUSTOM.values[3] = envelopeSustainDuration.getValue().intValue();
        PresetEnvelope.CUSTOM.values[4] = envelopeRelease.getValue().intValue();
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
