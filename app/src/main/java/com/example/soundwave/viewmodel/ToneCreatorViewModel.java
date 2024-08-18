package com.example.soundwave.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.soundwave.utils.AudioPlayer;
import com.example.soundwave.utils.ToneParser;
import com.example.soundwave.components.ControlPanelComponent;
import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.components.FundamentalFrequencyComponent;
import com.example.soundwave.components.OvertonesComponent;
import com.example.soundwave.components.Overtone;
import com.example.soundwave.components.Tone;
import com.example.soundwave.model.repository.SoundwaveRepo;
import com.example.soundwave.utils.Config;
import com.example.soundwave.utils.Options;
import com.example.soundwave.utils.PresetEnvelope;
import com.example.soundwave.utils.PresetOvertones;
import com.example.soundwave.utils.SampleRate;
import com.example.soundwave.utils.UnitsConverter;
import com.example.soundwave.utils.ToneGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class ToneCreatorViewModel extends AndroidViewModel {
    private final String TAG = "ToneCreatorViewModel";

    private final SoundwaveRepo repository;

    private final MutableLiveData<SampleRate> sampleRate = new MutableLiveData<>();
    private final MutableLiveData<EnvelopeComponent> envelopeComponent = new MutableLiveData<>();
    private final MutableLiveData<Integer> toneDuration = new MutableLiveData<>();
    private final MutableLiveData<FundamentalFrequencyComponent> fundamentalFrequencyComponent = new MutableLiveData<>();
    private final MutableLiveData<ControlPanelComponent> controlPanelComponent = new MutableLiveData<>();
    private final MutableLiveData<Overtone[]> overtones = new MutableLiveData<>();
    private final MutableLiveData<Tone> tone = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> isDataLoading = new MutableLiveData<>(true);

    private AudioPlayer audioPlayer;
    private boolean overtonesActivator;
    private boolean anyChange;

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
        repository.shutdownExecutorService();
        super.onCleared();
    }

    public LiveData<SampleRate> getSampleRate() {
        return sampleRate;
    }

    public LiveData<EnvelopeComponent> getEnvelopeComponent() {
        return envelopeComponent;
    }

    public LiveData<Integer> getToneDuration() {
        return toneDuration;
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

    public LiveData<Boolean> getIsDataLoading() {
        return isDataLoading;
    }

    public boolean getAnyChange() {
        return anyChange;
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
            Log.w(TAG, "Envelope parameter: user input (" + input + ") - not integer");
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
        EnvelopeComponent ec = Objects.requireNonNull(envelopeComponent.getValue());

        switch (parameter) {
            case ATTACK_DURATION:
                return value != ec.getAttackDuration();
            case DECAY_DURATION:
                return value != ec.getDecayDuration();
            case SUSTAIN_LEVEL:
                return value != ec.getSustainLevel();
            case SUSTAIN_DURATION:
                return value != ec.getSustainDuration();
            case RELEASE_DURATION:
                return value != ec.getReleaseDuration();
        }
        return true;
    }

    private void setEnvelopeParameter(EnvelopeComponent.EnvelopeParameters parameter, int value) {
        EnvelopeComponent ec = Objects.requireNonNull(envelopeComponent.getValue());

        int currentAttackDuration = ec.getAttackDuration();
        int currentDecayDuration = ec.getDecayDuration();
        int currentSustainLevel = ec.getSustainLevel();
        int currentSustainDuration = ec.getSustainDuration();
        int currentReleaseDuration = ec.getReleaseDuration();

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

    public void updateToneDuration(String input) {
        input = input.trim();
        if (input.isEmpty())            // letting the user delete all digits and type from scratch
            return;

        int userDuration;

        try {
            userDuration = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Tone duration: user input (" + input + ") - not integer");
            setToneDuration(Config.TONE_DURATION_DEFAULT.value);
            return;
        }
        int duration = Math.max(Config.TONE_DURATION_MIN.value, Math.min(userDuration, Config.TONE_DURATION_MAX.value));    // clamp between min and max

        setToneDuration(duration);
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

    public void updateNoteName(int noteIndex) {
        int frequency = FundamentalFrequencyComponent.getFrequencyOutOfNoteIndex(noteIndex);
        int volume = Objects.requireNonNull(fundamentalFrequencyComponent.getValue()).getMasterVolume();

        setFrequencyComplex(frequency, volume);
    }

    public void updateFundamentalFrequencySeekBarPosition(int progress) {
        int frequency = UnitsConverter.convertSeekBarProgressToFrequency(progress);
        int volume = Objects.requireNonNull(fundamentalFrequencyComponent.getValue()).getMasterVolume();

        setFrequencyComplex(frequency, volume);
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

    public void updateMasterVolumeSeekBarPosition(int progress) {
        setFrequencyComplex(Objects.requireNonNull(fundamentalFrequencyComponent.getValue()).getFundamentalFrequency(), progress);
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

        Overtone[] newPresetOvertones = Objects.requireNonNull(overtones.getValue());

        for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i)
            newPresetOvertones[i] = new Overtone(i, newPresetOvertones[i].getFrequency(), targetOvertonesPreset.amplitudes[i], newPresetOvertones[i].isActive());

        overtones.setValue(newPresetOvertones);
        setAnyChange();
    }

    public void updateOvertoneAmplitude(int index, int progress) {
        Options.overtonePreset = PresetOvertones.CUSTOM;
        Options.lastOvertonePreset = PresetOvertones.CUSTOM;

        Overtone[] updatedOvertones = Objects.requireNonNull(overtones.getValue());

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
        Overtone[] updatedOvertones = Objects.requireNonNull(overtones.getValue());

        for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i) {
            if (i == index)
                updatedOvertones[i] = new Overtone(i, updatedOvertones[i].getFrequency(), updatedOvertones[i].getAmplitude(), isActive);
        }
        overtones.setValue(updatedOvertones);
        setAnyChange();
    }

    public boolean generateTone(boolean editorMode) {
        Tone newTone = getNewTone();

        if (editorMode)
            newTone.setId(Objects.requireNonNull(tone.getValue()).getId());

        audioPlayer = new AudioPlayer();
        boolean loadingSuccessful = audioPlayer.loadTone(newTone);

        if (loadingSuccessful) {
            tone.setValue(newTone);
            setControlPanelComponentToneGenerated();
            return true;
        }
        return false;
    }

    private Tone getNewTone() {
        SampleRate sr = Objects.requireNonNull(sampleRate.getValue());
        EnvelopeComponent ec = Objects.requireNonNull(envelopeComponent.getValue());
        FundamentalFrequencyComponent ffc = Objects.requireNonNull(fundamentalFrequencyComponent.getValue());
        OvertonesComponent oc = new OvertonesComponent(getAllOvertones(), Options.overtonePreset);
        double duration = UnitsConverter.convertMsToSeconds(Objects.requireNonNull(toneDuration.getValue()));

        ToneGenerator toneGenerator = new ToneGenerator(sr, duration);

        return toneGenerator.generateTone(ec, ffc, oc);
    }

    public void playStopTone() {
        HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStates = getControlPanelButtonsStates();
        if (buttonsStates.get(ControlPanelComponent.Button.PLAY_STOP) == ControlPanelComponent.ButtonState.STANDARD) {
            audioPlayer.stop();
            audioPlayer.reload();
            audioPlayer.play();

            setControlPanelComponentPlayTone(buttonsStates);

            Thread thread = new Thread(() -> {
                int waitingTime = Objects.requireNonNull(tone.getValue()).getDurationInMs();
                try {
                    Thread.sleep(waitingTime);
                } catch (InterruptedException e) {
                    Log.w(TAG, "Playing tone: could not wait " + waitingTime + "ms to end of playback");
                }
                HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStatesInThread = getControlPanelButtonsStates();
                if (buttonsStatesInThread.get(ControlPanelComponent.Button.PLAY_STOP) == ControlPanelComponent.ButtonState.SECOND_FUNCTION)
                    setControlPanelComponentStopTone(buttonsStatesInThread);
            });
            thread.start();
            return;
        }
        audioPlayer.stop();

        setControlPanelComponentStopTone(buttonsStates);
    }

    public void saveTone(String toneName, boolean editorMode) {
        Tone baseTone = Objects.requireNonNull(tone.getValue());
        baseTone.setName(toneName);

        com.example.soundwave.model.entity.Tone toneEntity = new ToneParser().parseToneToDbEntity(baseTone);

        if (editorMode) {
            toneEntity.setId(baseTone.getId());
            repository.update(toneEntity);
        } else
            repository.insert(toneEntity);

        setControlPanelComponentSaved();
        anyChange = false;
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

    public boolean loadEditedTone(Tone editedTone) {
        audioPlayer = new AudioPlayer();
        boolean loadingSuccessful = audioPlayer.loadTone(editedTone);

        loadSampleRate(editedTone.getSampleRate());
        loadEnvelopeComponent(editedTone.getEnvelopeComponent());
        setToneDuration(editedTone.getDurationInMs());
        fundamentalFrequencyComponent.setValue(new FundamentalFrequencyComponent(
                editedTone.getFundamentalFrequency(), editedTone.getMasterVolume()));
        loadOvertonesComponent(editedTone.getOvertonesComponent());
        tone.setValue(editedTone);

        setControlPanelComponentEditorDefault();

        anyChange = false;

        return loadingSuccessful;
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

        return new ArrayList<>(Arrays.asList(Objects.requireNonNull(overtones.getValue())));
    }

    private void initializeDefaultValues() {
        updateSampleRate(0);
        setEnvelopePreset(PresetEnvelope.FLAT);
        setToneDuration(Config.TONE_DURATION_DEFAULT.value);
        fundamentalFrequencyComponent.setValue(new FundamentalFrequencyComponent(
                Config.FREQUENCY_DEFAULT.value, Config.MASTER_VOLUME_DEFAULT.value));
        setDefaultOvertones();
        setControlPanelComponentDefault();

        audioPlayer = null;
        anyChange = false;

        isDataLoading.setValue(false);
    }

    private void setDefaultOvertones() {
        Options.overtonePreset = PresetOvertones.FLAT;
        Overtone[] defaultOvertones = new Overtone[Config.OVERTONES_NUMBER.value];
        int fundamentalFrequency = Objects.requireNonNull(fundamentalFrequencyComponent.getValue()).getFundamentalFrequency();

        for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i) {
            int overtoneFrequency = fundamentalFrequency * (i + 2);
            defaultOvertones[i] = new Overtone(i, overtoneFrequency, Options.overtonePreset.amplitudes[i], true);
        }
        overtones.setValue(defaultOvertones);
        Options.overtonePreset = PresetOvertones.NONE;
    }

    private void updateOvertonesFrequency() {
        Overtone[] updatedOvertones = Objects.requireNonNull(overtones.getValue());
        int fundamentalFrequency = Objects.requireNonNull(fundamentalFrequencyComponent.getValue()).getFundamentalFrequency();

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
            EnvelopeComponent ec = Objects.requireNonNull(envelopeComponent.getValue());

            PresetEnvelope.CUSTOM.values[0] = ec.getAttackDuration();
            PresetEnvelope.CUSTOM.values[1] = ec.getDecayDuration();
            PresetEnvelope.CUSTOM.values[2] = ec.getSustainLevel();
            PresetEnvelope.CUSTOM.values[3] = ec.getSustainDuration();
            PresetEnvelope.CUSTOM.values[4] = ec.getReleaseDuration();
            return;
        }

        envelopeComponent.setValue(new EnvelopeComponent(preset, preset.values[0],
                preset.values[1], preset.values[2], preset.values[3], preset.values[4]));
    }

    private void setToneDuration(int duration) {
        toneDuration.setValue(duration);
        setAnyChange();
    }

    private void setFrequencyComplex(int frequency, int volume) {
        int oldFrequency = Objects.requireNonNull(fundamentalFrequencyComponent.getValue()).getFundamentalFrequency();
        fundamentalFrequencyComponent.setValue(new FundamentalFrequencyComponent(frequency, volume));

        if (frequency != oldFrequency)                      // if changing only volume
            updateOvertonesFrequency();                     // not necessary to update overtones

        setAnyChange();
    }

    public void setNoChange() {
        setControlPanelComponentEditorDefault();
        anyChange = false;
    }

    private void setAnyChange() {
        if (Boolean.TRUE.equals(isDataLoading.getValue()))
            return;

        anyChange = true;
        HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStates = getControlPanelButtonsStates();
        ControlPanelComponent.ButtonState generateBtnState = buttonsStates.get(ControlPanelComponent.Button.GENERATE);
        ControlPanelComponent.ButtonState resetBtnState = buttonsStates.get(ControlPanelComponent.Button.RESET);

        if (generateBtnState == ControlPanelComponent.ButtonState.STANDARD &&
                resetBtnState == ControlPanelComponent.ButtonState.STANDARD)
            return;

        setControlPanelComponentAnyChange(buttonsStates);
    }

    private HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> getControlPanelButtonsStates() {
        return Objects.requireNonNull(controlPanelComponent.getValue()).getButtonsStates();
    }

    private void setControlPanelComponentDefault() {
        controlPanelComponent.setValue(new ControlPanelComponent(
                ControlPanelComponent.ButtonState.STANDARD,
                ControlPanelComponent.ButtonState.INACTIVE,
                ControlPanelComponent.ButtonState.INACTIVE,
                ControlPanelComponent.ButtonState.INACTIVE));
    }

    private void setControlPanelComponentEditorDefault() {
        controlPanelComponent.setValue(new ControlPanelComponent(
                ControlPanelComponent.ButtonState.INACTIVE,
                ControlPanelComponent.ButtonState.STANDARD,
                ControlPanelComponent.ButtonState.INACTIVE,
                ControlPanelComponent.ButtonState.INACTIVE));
    }

    private void setControlPanelComponentToneGenerated() {
        controlPanelComponent.setValue(new ControlPanelComponent(
                ControlPanelComponent.ButtonState.INACTIVE,
                ControlPanelComponent.ButtonState.STANDARD,
                ControlPanelComponent.ButtonState.STANDARD,
                ControlPanelComponent.ButtonState.STANDARD));
    }

    private void setControlPanelComponentSaved() {
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

    private void setControlPanelComponentPlayTone(HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStates) {
        controlPanelComponent.setValue(new ControlPanelComponent(
                buttonsStates.get(ControlPanelComponent.Button.GENERATE),
                ControlPanelComponent.ButtonState.SECOND_FUNCTION,
                buttonsStates.get(ControlPanelComponent.Button.SAVE),
                buttonsStates.get(ControlPanelComponent.Button.RESET)));
    }

    private void setControlPanelComponentStopTone(HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStates) {
        controlPanelComponent.postValue(new ControlPanelComponent(
                buttonsStates.get(ControlPanelComponent.Button.GENERATE),
                ControlPanelComponent.ButtonState.STANDARD,
                buttonsStates.get(ControlPanelComponent.Button.SAVE),
                buttonsStates.get(ControlPanelComponent.Button.RESET)));
    }

    private void setControlPanelComponentAnyChange(HashMap<ControlPanelComponent.Button, ControlPanelComponent.ButtonState> buttonsStates) {
        controlPanelComponent.setValue(new ControlPanelComponent(
                ControlPanelComponent.ButtonState.STANDARD,
                buttonsStates.get(ControlPanelComponent.Button.PLAY_STOP),
                buttonsStates.get(ControlPanelComponent.Button.SAVE),
                ControlPanelComponent.ButtonState.STANDARD));
    }
}
