package com.example.soundwave.viewmodel;

import android.app.Application;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.soundwave.OvertoneManager;
import com.example.soundwave.model.repository.SoundwaveRepo;
import com.example.soundwave.utils.Config;
import com.example.soundwave.utils.SuffixManager;
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

    public LiveData<Integer> getFundamentalFrequency() {
        return fundamentalFrequency;
    }

    public LiveData<Integer> getFundamentalFrequencyBar() {
        return fundamentalFrequencyBar;
    }

    public LiveData<String> getScale() {
        return scale;
    }

    public LiveData<Integer> getMasterVolume() {
        return masterVolume;
    }

    public LiveData<Integer> getMasterVolumeBar() {
        return masterVolumeBar;
    }

    public void updateEnvelopeAttack(String input) {
        int userAttackDuration;
        try {
            userAttackDuration = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            envelopeAttack.setValue(Config.ENVELOPE_ATTACK_DEFAULT.value);
            return;
        }
        if (userAttackDuration >= 0 && userAttackDuration <= Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeAttack.setValue(userAttackDuration);
            return;
        }
        if (userAttackDuration > Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeAttack.setValue(Config.ENVELOPE_MAX_PARAMETER_TIME.value);
            return;
        }
        envelopeAttack.setValue(0);
    }

    public void updateEnvelopeDecay(String input) {
        int userDecayDuration;
        try {
            userDecayDuration = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            envelopeDecay.setValue(Config.ENVELOPE_DECAY_DEFAULT.value);
            return;
        }
        if (userDecayDuration >= 0 && userDecayDuration <= Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeDecay.setValue(userDecayDuration);
            return;
        }
        if (userDecayDuration > Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeDecay.setValue(Config.ENVELOPE_MAX_PARAMETER_TIME.value);
            return;
        }
        envelopeDecay.setValue(0);
    }

    public void updateEnvelopeSustainLevel(String input) {
        int userSustainLevel;
        try {
            userSustainLevel = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            envelopeSustainLevel.setValue(Config.ENVELOPE_SUSTAIN_LEVEL_DEFAULT.value);
            return;
        }
        if (userSustainLevel >= 0 && userSustainLevel <= 100) {
            envelopeSustainLevel.setValue(userSustainLevel);
            return;
        }
        if (userSustainLevel > 100) {
            envelopeSustainLevel.setValue(100);
            return;
        }
        envelopeSustainLevel.setValue(0);
    }

    public void updateEnvelopeSustainDuration(String input) {
        int userSustainDuration;
        try {
            userSustainDuration = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            envelopeSustainDuration.setValue(Config.ENVELOPE_SUSTAIN_DURATION_DEFAULT.value);
            return;
        }
        if (userSustainDuration >= 0 && userSustainDuration <= Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeSustainDuration.setValue(userSustainDuration);
            return;
        }
        if (userSustainDuration > Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeSustainDuration.setValue(Config.ENVELOPE_MAX_PARAMETER_TIME.value);
            return;
        }
        envelopeSustainDuration.setValue(0);
    }

    public void updateEnvelopeRelease(String input) {
        int userReleaseDuration;
        try {
            userReleaseDuration = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            envelopeRelease.setValue(Config.ENVELOPE_RELEASE_DEFAULT.value);
            return;
        }
        if (userReleaseDuration >= 0 && userReleaseDuration <= Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeRelease.setValue(userReleaseDuration);
            return;
        }
        if (userReleaseDuration > Config.ENVELOPE_MAX_PARAMETER_TIME.value) {
            envelopeRelease.setValue(Config.ENVELOPE_MAX_PARAMETER_TIME.value);
            return;
        }
        envelopeRelease.setValue(0);
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
        if (userFrequency < Config.FREQUENCY_MIN.value) {
            setFundamentalFrequencyBar(Config.FREQUENCY_MIN.value);
            setScale(Config.FREQUENCY_MIN.value);
            return;
        }
        int frequencyToSet = Math.min(userFrequency, Config.FREQUENCY_MAX.value);
        setFrequencyComplex(frequencyToSet);
    }

    public void updateFundamentalFrequencySeekBarPosition(int progress) {
        fundamentalFrequency.setValue(UnitsConverter.convertSeekBarProgressToFrequency(progress));
        setScale(fundamentalFrequency.getValue());
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

    private void initializeOvertoneManagers() {
        overtoneManagers = new OvertoneManager[Config.OVERTONES_NUMBER.value];
        for (int i = 0; i < overtoneManagers.length; ++i) {
            int overtoneIndex = i + 1;
            int overtoneFrequency = fundamentalFrequencyInHz * (overtoneIndex + 1);
            String ID = "overtone_" + overtoneIndex;
            int resID = context.getResources().getIdentifier(ID, "id", context.getPackageName());
            View overtoneView = toneView.findViewById(resID);
            overtoneManagers[i] = new OvertoneManager(overtoneView, overtoneIndex, overtoneFrequency, getPresetForTone(), index);
        }
    }

    private void initializeDefaultValues() {
        envelopeAttack.setValue(SuffixManager.addMillisecondSuffix(Config.ENVELOPE_ATTACK_DEFAULT.value));
        envelopeDecay.setValue(SuffixManager.addMillisecondSuffix(Config.ENVELOPE_DECAY_DEFAULT.value));
        envelopeSustainLevel.setValue(SuffixManager.addPercentSuffix(Config.ENVELOPE_SUSTAIN_LEVEL_DEFAULT.value));
        envelopeSustainDuration.setValue(SuffixManager.addMillisecondSuffix(Config.ENVELOPE_SUSTAIN_DURATION_DEFAULT.value));
        envelopeRelease.setValue(SuffixManager.addMillisecondSuffix(Config.ENVELOPE_RELEASE_DEFAULT.value));
        //fundamentalFrequency.setValue();
        fundamentalFrequencySeekBarPosition.setValue(Config.FREQUENCY_PROGRESS_BAR_DEFAULT.value);
        //scale.setValue();
        //masterVolume.setValue();
        masterVolumeSeekBarPosition.setValue(100);
    }

    private void setFrequencyComplex(int frequency) {
        fundamentalFrequency.setValue(frequency);
        setFundamentalFrequencyBar(frequency);
        setScale(frequency);
    }

    private void setFundamentalFrequencyBar(int frequency) {
        int frequencyBarPosition = UnitsConverter.convertFrequencyToSeekBarProgress(frequency);
        fundamentalFrequencyBar.setValue(frequencyBarPosition);
    }

    private void setScale(int frequency) {
        scale.setValue("B#4");
        //TODO: calculate and set proper scale based on frequency parameter
    }
}
