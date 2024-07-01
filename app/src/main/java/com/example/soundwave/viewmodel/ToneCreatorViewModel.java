package com.example.soundwave.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.soundwave.utils.AudioPlayer;
import com.example.soundwave.utils.WavCreator;
import com.example.soundwave.components.ControlPanelComponent;
import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.components.FundamentalFrequencyComponent;
import com.example.soundwave.components.OvertonesComponent;
import com.example.soundwave.Overtone;
import com.example.soundwave.Tone;
import com.example.soundwave.model.repository.SoundwaveRepo;
import com.example.soundwave.utils.Config;
import com.example.soundwave.utils.Options;
import com.example.soundwave.utils.PresetEnvelope;
import com.example.soundwave.utils.PresetOvertones;
import com.example.soundwave.utils.SampleRate;
import com.example.soundwave.utils.UnitsConverter;
import com.example.soundwave.utils.ToneGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ToneCreatorViewModel extends AndroidViewModel {

    private SoundwaveRepo repository;

    private MutableLiveData<SampleRate> sampleRate = new MutableLiveData<>();
    private MutableLiveData<EnvelopeComponent> envelopeComponent = new MutableLiveData<>();
    private MutableLiveData<FundamentalFrequencyComponent> fundamentalFrequencyComponent = new MutableLiveData<>();
    private MutableLiveData<ControlPanelComponent> controlPanelComponent = new MutableLiveData<>();
    private MutableLiveData<Overtone[]> overtones = new MutableLiveData<>();
    private MutableLiveData<Tone> tone = new MutableLiveData<>();

    private AudioPlayer audioPlayer;
    private boolean overtonesActivator;

    public ToneCreatorViewModel(@NonNull Application application) {
        super(application);
        repository = new SoundwaveRepo(application);
        initializeDefaultValues();
    }

    @Override
    protected void onCleared() {
        if (audioPlayer != null) {
            audioPlayer.stop();
            audioPlayer = null;
        }
        super.onCleared();
    }

    public LiveData<SampleRate> getSampleRate() {
        return sampleRate;
    }

    public LiveData<EnvelopeComponent> getEnvelopeComponent() {
        return envelopeComponent;
    }

    public LiveData<FundamentalFrequencyComponent> getFundamentalFrequencyComponent() {
        return fundamentalFrequencyComponent;
    }

    public LiveData<ControlPanelComponent> getControlPanelComponent() {
        return controlPanelComponent;
    }

    public LiveData<Overtone[]> getOvertones() {
        return overtones;
    }

    public LiveData<Tone> getTone() {
        return tone;
    }

    public int getEnvelopePresetPosition() {
        return UnitsConverter.convertPresetEnvelopeToPosition(Options.envelopePreset);
    }

    public int getOvertonesPresetPosition() {
        return UnitsConverter.convertPresetOvertonesToPosition(Options.lastOvertonePreset);
    }

    public void updateSampleRate(int position) {
        SampleRate newSampleRate = UnitsConverter.convertPositionToSampleRate(position);
        if (sampleRate.getValue() == newSampleRate)
            return;
        sampleRate.setValue(newSampleRate);
        setAnyChange();
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
                envelopeComponent.setValue(new EnvelopeComponent(PresetEnvelope.CUSTOM,
                        value, currentDecayDuration, currentSustainLevel, currentSustainDuration, currentReleaseDuration));
                break;
            case DECAY_DURATION:
                envelopeComponent.setValue(new EnvelopeComponent(PresetEnvelope.CUSTOM,
                        currentAttackDuration, value, currentSustainLevel, currentSustainDuration, currentReleaseDuration));
                break;
            case SUSTAIN_LEVEL:
                envelopeComponent.setValue(new EnvelopeComponent(PresetEnvelope.CUSTOM,
                        currentAttackDuration, currentDecayDuration, value, currentSustainDuration, currentReleaseDuration));
                break;
            case SUSTAIN_DURATION:
                envelopeComponent.setValue(new EnvelopeComponent(PresetEnvelope.CUSTOM,
                        currentAttackDuration, currentDecayDuration, currentSustainLevel, value, currentReleaseDuration));
                break;
            case RELEASE_DURATION:
                envelopeComponent.setValue(new EnvelopeComponent(PresetEnvelope.CUSTOM,
                        currentAttackDuration, currentDecayDuration, currentSustainLevel, currentSustainDuration, value));
        }
        setEnvelopePreset(PresetEnvelope.CUSTOM);
    }

    public void updateFundamentalFrequency(String input) {
        input = input.trim();
        if (input.isEmpty())            // letting the user delete all digits and type from scratch
            return;

        int userFrequency;
        try {
            userFrequency = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            setFrequencyComplex(Config.FREQUENCY_DEFAULT.value, fundamentalFrequencyComponent.getValue().getMasterVolume());
            return;
        }
        if (userFrequency == fundamentalFrequencyComponent.getValue().getFundamentalFrequency() || userFrequency < Config.FREQUENCY_MIN.value)
            return;
        int frequencyToSet = Math.min(userFrequency, Config.FREQUENCY_MAX.value);
        setFrequencyComplex(frequencyToSet, fundamentalFrequencyComponent.getValue().getMasterVolume());
    }

    public void updateNoteName(int noteIndex) {
        setFrequencyComplex(FundamentalFrequencyComponent.getFrequencyOutOfNoteIndex(noteIndex), fundamentalFrequencyComponent.getValue().getMasterVolume());
    }

    public void updateFundamentalFrequencySeekBarPosition(int progress) {
        setFrequencyComplex(UnitsConverter.convertSeekBarProgressToFrequency(progress), fundamentalFrequencyComponent.getValue().getMasterVolume());
    }

    public void decrementOnceFundamentalFrequency() {
        int frequency = fundamentalFrequencyComponent.getValue().getFundamentalFrequency();
        if (--frequency >= Config.FREQUENCY_MIN.value)
            setFrequencyComplex(frequency, fundamentalFrequencyComponent.getValue().getMasterVolume());
    }

    public void incrementOnceFundamentalFrequency() {
        int frequency = fundamentalFrequencyComponent.getValue().getFundamentalFrequency();
        if (++frequency <= Config.FREQUENCY_MAX.value)
            setFrequencyComplex(frequency, fundamentalFrequencyComponent.getValue().getMasterVolume());
    }

    public void updateMasterVolumeSeekBarPosition(int progress) {
        setFrequencyComplex(fundamentalFrequencyComponent.getValue().getFundamentalFrequency(), progress);
    }

    public void validateFundamentalFrequencyInput(String input) {
        int displayFrequency;
        try {
            displayFrequency = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            setFrequencyComplex(Config.FREQUENCY_DEFAULT.value, fundamentalFrequencyComponent.getValue().getMasterVolume());
            return;
        }
        if (displayFrequency < Config.FREQUENCY_MIN.value)
            setFrequencyComplex(Config.FREQUENCY_MIN.value, fundamentalFrequencyComponent.getValue().getMasterVolume());
    }

    public void updateOvertonesState(boolean isActive) {
        overtonesActivator = isActive;
        if (!isActive)
            Options.overtonePreset = PresetOvertones.NONE;
        else
            Options.overtonePreset = Options.lastOvertonePreset;
        setAnyChange();
    }

    public void updateOvertonesPreset(int position) {
        PresetOvertones targetOvertonesPreset = UnitsConverter.convertPositionToPresetOvertones(position);
        if (targetOvertonesPreset == Options.overtonePreset)
            return;

        Options.overtonePreset = targetOvertonesPreset;
        Options.lastOvertonePreset = targetOvertonesPreset;

        Overtone[] newPresetOvertones = overtones.getValue();
        for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i)
            newPresetOvertones[i] = new Overtone(i, newPresetOvertones[i].getFrequency(), targetOvertonesPreset.amplitudes[i], newPresetOvertones[i].isActive());
        overtones.setValue(newPresetOvertones);
        setAnyChange();
    }

    public void updateOvertoneAmplitude(int index, int progress) {
        Options.overtonePreset = PresetOvertones.CUSTOM;
        Options.lastOvertonePreset = PresetOvertones.CUSTOM;

        Overtone[] updatedOvertones = overtones.getValue();
        for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i) {
            if (i == index) {
                double amplitude = UnitsConverter.convertOvertoneDbSliderToHumanValue(progress);
                updatedOvertones[i] = new Overtone(i, updatedOvertones[i].getFrequency(), amplitude, updatedOvertones[i].isActive());
            }
        }
        overtones.setValue(updatedOvertones);
        setAnyChange();
    }

    public void updateOvertoneState(int index, boolean isActive) {
        Overtone[] updatedOvertones = overtones.getValue();
        for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i) {
            if (i == index)
                updatedOvertones[i] = new Overtone(i, updatedOvertones[i].getFrequency(), updatedOvertones[i].getAmplitude(), isActive);
        }
        overtones.setValue(updatedOvertones);
        setAnyChange();
    }

    public void generateTone(boolean editorMode) {
        OvertonesComponent oC = new OvertonesComponent(getAllOvertones(), Options.overtonePreset);
        ToneGenerator toneGenerator = new ToneGenerator(sampleRate.getValue(),
                envelopeComponent.getValue(), fundamentalFrequencyComponent.getValue(), oC);

        Tone newTone = toneGenerator.generateTone();
        if (editorMode)
            newTone.setId(tone.getValue().getId());

        audioPlayer = new AudioPlayer(newTone);
        audioPlayer.load();
        tone.setValue(newTone);
        controlPanelComponent.setValue(new ControlPanelComponent(
                ControlPanelComponent.ButtonState.INACTIVE,
                ControlPanelComponent.ButtonState.STANDARD,
                ControlPanelComponent.ButtonState.STANDARD,
                ControlPanelComponent.ButtonState.STANDARD));
    }

    public void playStopTone() {
        HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStates = getControlPanelButtonsStates();
        if (buttonsStates.get(ControlPanelComponent.Button.PLAY_STOP) == ControlPanelComponent.ButtonState.STANDARD) {
            audioPlayer.stop();
            audioPlayer.reload();
            audioPlayer.play();
            controlPanelComponent.setValue(new ControlPanelComponent(
                    buttonsStates.get(ControlPanelComponent.Button.GENERATE),
                    ControlPanelComponent.ButtonState.SECOND_FUNCTION,
                    buttonsStates.get(ControlPanelComponent.Button.SAVE),
                    buttonsStates.get(ControlPanelComponent.Button.RESET)));
            Thread thread = new Thread() {
                public void run() {
                    int waitingTime = tone.getValue().getDurationInMilliseconds();
                    try {
                        Thread.sleep(waitingTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStatesInThread = getControlPanelButtonsStates();
                    if (buttonsStatesInThread.get(ControlPanelComponent.Button.PLAY_STOP) == ControlPanelComponent.ButtonState.SECOND_FUNCTION)
                        controlPanelComponent.postValue(new ControlPanelComponent(
                                buttonsStatesInThread.get(ControlPanelComponent.Button.GENERATE),
                                ControlPanelComponent.ButtonState.STANDARD,
                                buttonsStatesInThread.get(ControlPanelComponent.Button.SAVE),
                                buttonsStatesInThread.get(ControlPanelComponent.Button.RESET)));
                }
            };
            thread.start();
            return;
        }
        audioPlayer.stop();
        controlPanelComponent.setValue(new ControlPanelComponent(
                buttonsStates.get(ControlPanelComponent.Button.GENERATE),
                ControlPanelComponent.ButtonState.STANDARD,
                buttonsStates.get(ControlPanelComponent.Button.SAVE),
                buttonsStates.get(ControlPanelComponent.Button.RESET)));
    }

    public void saveTone(String toneName, File filepathBase, boolean editorMode) {
        com.example.soundwave.model.entity.Tone toneEntity;
        Tone baseTone = tone.getValue();

        int toneFrequency = baseTone.getFundamentalFrequency();
        int toneVolume = baseTone.getMasterVolume();
        String envelopeComponent = baseTone.getEnvelopeComponent().toString();
        String overtonesPreset = baseTone.getOvertonesPreset().toString();

        String overtonesDetails = "";

        ArrayList<Overtone> overtones = baseTone.getOvertones();
        if (overtones != null) {
            StringBuilder overtonesDetailsBuilder = new StringBuilder();
            for (Overtone overtone : overtones) {
                overtonesDetailsBuilder.append(overtone.toString());
            }
            overtonesDetails = overtonesDetailsBuilder.toString();
        }
        String overtonesComponent = overtonesPreset + "!" + overtonesDetails;
        String sampleRate = UnitsConverter.convertSampleRateToStringVisible(baseTone.getSampleRate());

        toneEntity = new com.example.soundwave.model.entity.Tone(toneName, toneFrequency,
                toneVolume, envelopeComponent, overtonesComponent, sampleRate);

        if (editorMode) {
            toneEntity.setId(baseTone.getId());
            repository.update(toneEntity);
        }
        else
            repository.insert(toneEntity);

        WavCreator wavCreator = new WavCreator(tone.getValue(), filepathBase);
        wavCreator.saveSound();

        if (wavCreator.isSuccess()) {
            HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStates = getControlPanelButtonsStates();
            ControlPanelComponent.ButtonState generateBtnState = buttonsStates.get(ControlPanelComponent.Button.GENERATE);
            ControlPanelComponent.ButtonState playStopBtnState = buttonsStates.get(ControlPanelComponent.Button.PLAY_STOP);
            ControlPanelComponent.ButtonState resetBtnState = buttonsStates.get(ControlPanelComponent.Button.RESET);

            controlPanelComponent.setValue(new ControlPanelComponent(
                    generateBtnState,
                    playStopBtnState,
                    ControlPanelComponent.ButtonState.DONE,
                    resetBtnState));
        }
    }

    public void resetTone() {
        initializeDefaultValues();
        tone.setValue(null);
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

    public void loadEditedTone(Tone editedTone) {
        audioPlayer = new AudioPlayer(editedTone);
        audioPlayer.load();

        loadSampleRate(editedTone.getSampleRate());
        loadEnvelopeComponent(editedTone.getEnvelopeComponent());
        fundamentalFrequencyComponent.setValue(new FundamentalFrequencyComponent(
                editedTone.getFundamentalFrequency(), editedTone.getMasterVolume()));
        loadOvertonesComponent(editedTone.getOvertonesComponent());
        tone.setValue(editedTone);
        controlPanelComponent.setValue(new ControlPanelComponent(
                ControlPanelComponent.ButtonState.INACTIVE,
                ControlPanelComponent.ButtonState.STANDARD,
                ControlPanelComponent.ButtonState.INACTIVE,
                ControlPanelComponent.ButtonState.INACTIVE));
    }

    private void loadSampleRate(SampleRate editedSampleRate) {
        if (sampleRate.getValue() == editedSampleRate)
            return;
        sampleRate.setValue(editedSampleRate);
    }

    private void loadEnvelopeComponent(EnvelopeComponent editedEC) {
        PresetEnvelope editedPreset = editedEC.getEnvelopePreset();
        Options.envelopePreset = editedPreset;

        if (editedPreset == PresetEnvelope.CUSTOM) {
            PresetEnvelope.CUSTOM.values[0] = editedEC.getAttackDuration();
            PresetEnvelope.CUSTOM.values[1] = editedEC.getDecayDuration();
            PresetEnvelope.CUSTOM.values[2] = editedEC.getSustainLevel();
            PresetEnvelope.CUSTOM.values[3] = editedEC.getSustainDuration();
            PresetEnvelope.CUSTOM.values[4] = editedEC.getReleaseDuration();
        }

        envelopeComponent.setValue(new EnvelopeComponent(editedPreset,
                editedPreset.values[0], editedPreset.values[1], editedPreset.values[2],
                editedPreset.values[3], editedPreset.values[4]));
    }

    private void loadOvertonesComponent(OvertonesComponent editedOC) {
        PresetOvertones editedPreset = editedOC.getOvertonesPreset();
        Options.overtonePreset = editedPreset;
        Options.lastOvertonePreset = editedPreset;

        if (editedPreset != PresetOvertones.NONE) {
            ArrayList<Overtone> ocOvertones = editedOC.getOvertones();

            Overtone[] editedOvertones = ocOvertones.toArray(new Overtone[ocOvertones.size()]);
            for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i)
                editedOvertones[i] = new Overtone(i, editedOvertones[i].getFrequency(), editedOvertones[i].getAmplitude(), editedOvertones[i].isActive());
            overtones.setValue(editedOvertones);
        }
    }

    private ArrayList<Overtone> getAllOvertones() {
        if (!overtonesActivator)
            return null;

        return new ArrayList<>(Arrays.asList(overtones.getValue()));
    }

    private void initializeDefaultValues() {
        audioPlayer = null;
        if (controlPanelComponent.getValue() == null)
            controlPanelComponent.setValue(new ControlPanelComponent(
                    ControlPanelComponent.ButtonState.STANDARD,
                    ControlPanelComponent.ButtonState.INACTIVE,
                    ControlPanelComponent.ButtonState.INACTIVE,
                    ControlPanelComponent.ButtonState.INACTIVE));
        updateSampleRate(0);
        setEnvelopePreset(PresetEnvelope.FLAT);
        fundamentalFrequencyComponent.setValue(new FundamentalFrequencyComponent(
                Config.FREQUENCY_DEFAULT.value, Config.MASTER_VOLUME_DEFAULT.value));
        setDefaultOvertones();
        controlPanelComponent.setValue(new ControlPanelComponent(
                ControlPanelComponent.ButtonState.STANDARD,
                ControlPanelComponent.ButtonState.INACTIVE,
                ControlPanelComponent.ButtonState.INACTIVE,
                ControlPanelComponent.ButtonState.INACTIVE));
    }

    private void setDefaultOvertones() {
        Options.overtonePreset = PresetOvertones.FLAT;
        Overtone[] defaultOvertones = new Overtone[Config.OVERTONES_NUMBER.value];
        int fundamentalFrequency = fundamentalFrequencyComponent.getValue().getFundamentalFrequency();
        for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i) {
            int overtoneFrequency = fundamentalFrequency * (i + 2);
            defaultOvertones[i] = new Overtone(i, overtoneFrequency, Options.overtonePreset.amplitudes[i], true);
        }
        overtones.setValue(defaultOvertones);
        Options.overtonePreset = PresetOvertones.NONE;
    }

    private void updateOvertonesFrequency() {
        Overtone[] updatedOvertones = overtones.getValue();
        int fundamentalFrequency = fundamentalFrequencyComponent.getValue().getFundamentalFrequency();

        for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i) {
            int overtoneFrequency = fundamentalFrequency * (i + 2);
            updatedOvertones[i] = new Overtone(i, overtoneFrequency,
                    updatedOvertones[i].getAmplitude(), updatedOvertones[i].isActive());
        }
        overtones.setValue(updatedOvertones);
    }

    private void setEnvelopePreset(PresetEnvelope preset) {
        Options.envelopePreset = preset;
        setAnyChange();

        if (preset == PresetEnvelope.CUSTOM) {
            PresetEnvelope.CUSTOM.values[0] = envelopeComponent.getValue().getAttackDuration();
            PresetEnvelope.CUSTOM.values[1] = envelopeComponent.getValue().getDecayDuration();
            PresetEnvelope.CUSTOM.values[2] = envelopeComponent.getValue().getSustainLevel();
            PresetEnvelope.CUSTOM.values[3] = envelopeComponent.getValue().getSustainDuration();
            PresetEnvelope.CUSTOM.values[4] = envelopeComponent.getValue().getReleaseDuration();
            return;
        }

        envelopeComponent.setValue(new EnvelopeComponent(preset, preset.values[0],
                preset.values[1], preset.values[2], preset.values[3], preset.values[4]));
    }

    private void setFrequencyComplex(int frequency, int volume) {
        int oldFrequency = fundamentalFrequencyComponent.getValue().getFundamentalFrequency();
        fundamentalFrequencyComponent.setValue(new FundamentalFrequencyComponent(frequency, volume));
        if (frequency != oldFrequency)                      // if changing only volume
            updateOvertonesFrequency();                     // not necessary to update overtones
        setAnyChange();
    }

    public void setNoChange() {
        controlPanelComponent.setValue(new ControlPanelComponent(
                ControlPanelComponent.ButtonState.INACTIVE,
                ControlPanelComponent.ButtonState.STANDARD,
                ControlPanelComponent.ButtonState.INACTIVE,
                ControlPanelComponent.ButtonState.INACTIVE));
    }

    private void setAnyChange() {
        HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStates = getControlPanelButtonsStates();
        ControlPanelComponent.ButtonState generateBtnState = buttonsStates.get(ControlPanelComponent.Button.GENERATE);
        ControlPanelComponent.ButtonState resetBtnState = buttonsStates.get(ControlPanelComponent.Button.RESET);

        if (generateBtnState == ControlPanelComponent.ButtonState.STANDARD &&
                resetBtnState == ControlPanelComponent.ButtonState.STANDARD)
            return;

        controlPanelComponent.setValue(new ControlPanelComponent(
                ControlPanelComponent.ButtonState.STANDARD,
                buttonsStates.get(ControlPanelComponent.Button.PLAY_STOP),
                buttonsStates.get(ControlPanelComponent.Button.SAVE),
                ControlPanelComponent.ButtonState.STANDARD));
    }

    private HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> getControlPanelButtonsStates() {
        return controlPanelComponent.getValue().getButtonsStates();
    }
}
