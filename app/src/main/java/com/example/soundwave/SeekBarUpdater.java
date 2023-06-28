package com.example.soundwave;

import android.os.Handler;
import android.widget.EditText;
import android.widget.SeekBar;

public class SeekBarUpdater implements Runnable {

    private final Handler handler;
    private final SeekBar barToUpdate;
    private final EditText frequencyTxt;
    private final Options.Operation operation;

    public SeekBarUpdater(Handler handler, SeekBar frequencyBar, EditText frequencyTxt, Options.Operation operation) {
        this.handler = handler;
        this.barToUpdate = frequencyBar;
        this.frequencyTxt = frequencyTxt;
        this.operation = operation;
    }

    public SeekBarUpdater(Handler handler, SeekBar durationBar, Options.Operation operation) {
        this.handler = handler;
        this.barToUpdate = durationBar;
        this.frequencyTxt = null;
        this.operation = operation;
    }

    @Override
    public void run() {
        int refreshRate = getRefreshRate();
        while (getState() == Options.ButtonLongPressState.PRESSED) {
            applyChanges();
            try {
                Thread.sleep(refreshRate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void applyChanges() {
        switch (operation) {
            case FREQUENCY_DECREMENT:
            case FREQUENCY_INCREMENT:
                changeFrequency();
                break;
            case DURATION_DECREMENT:
            case DURATION_INCREMENT:
                changeDuration();
                break;
        }
    }

    private void changeFrequency() {
        int progress = Integer.parseInt(frequencyTxt.getText().toString());
        switch (operation) {
            case FREQUENCY_DECREMENT:
                --progress;
                if (progress < Constants.FREQUENCY_MIN.value)
                    return;
                break;
            case FREQUENCY_INCREMENT:
                ++progress;
                if (progress > Constants.FREQUENCY_MAX.value)
                    return;
                break;
        }
        int finalProgress = progress;
        handler.post(new Runnable() {
            public void run() {
                barToUpdate.setProgress(UnitsConverter.convertFrequencyToProgressBarPosition(finalProgress));
                frequencyTxt.setText(String.valueOf(finalProgress));
            }
        });
    }

    private void changeDuration() {
        int progress = barToUpdate.getProgress();
        switch (operation) {
            case DURATION_DECREMENT:
                --progress;
                if (progress < 1)
                    return;
                break;
            case DURATION_INCREMENT:
                ++progress;
                if (progress > Constants.DURATION_MAX.value)
                    return;
                break;
        }
        int finalProgress = progress;
        handler.post(new Runnable() {
            public void run() {
                barToUpdate.setProgress(finalProgress);
            }
        });
    }

    private Options.ButtonLongPressState getState() {
        Options.ButtonLongPressState state = Options.ButtonLongPressState.RELEASED;
        switch (operation) {
            case FREQUENCY_DECREMENT:
                state = Options.buttonDecrementFrequencyState;
                break;
            case DURATION_DECREMENT:
                state = Options.buttonDecrementDurationState;
                break;
            case FREQUENCY_INCREMENT:
                state = Options.buttonIncrementFrequencyState;
                break;
            case DURATION_INCREMENT:
                state = Options.buttonIncrementDurationState;
                break;
        }
        return state;
    }

    private int getRefreshRate() {
        int refreshRate = Constants.SEEKBAR_CHANGE_REFRESH_RATE.value;
        if (operation == Options.Operation.DURATION_DECREMENT || operation == Options.Operation.DURATION_INCREMENT)
            refreshRate *= 2;
        return refreshRate;
    }
}
