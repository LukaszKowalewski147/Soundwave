package com.example.soundwave;

import android.os.Handler;
import android.widget.EditText;

public class SeekBarUpdater implements Runnable {

    private final Handler handler;
    private final EditText toUpdateTxt;
    private final Options.Operation operation;

    public SeekBarUpdater(Handler handler, EditText toUpdateTxt, Options.Operation operation) {
        this.handler = handler;
        this.toUpdateTxt = toUpdateTxt;
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
        int frequency = Integer.parseInt(toUpdateTxt.getText().toString());
        switch (operation) {
            case FREQUENCY_DECREMENT:
                --frequency;
                if (frequency < Constants.FREQUENCY_MIN.value)
                    return;
                break;
            case FREQUENCY_INCREMENT:
                ++frequency;
                if (frequency > Constants.FREQUENCY_MAX.value)
                    return;
                break;
        }
        int finalProgress = frequency;
        handler.post(new Runnable() {
            public void run() {
                toUpdateTxt.setText(String.valueOf(finalProgress));
            }
        });
    }

    private void changeDuration() {
        int duration = Integer.parseInt(toUpdateTxt.getText().toString());
        switch (operation) {
            case DURATION_DECREMENT:
                --duration;
                if (duration < Constants.DURATION_MIN.value)
                    return;
                break;
            case DURATION_INCREMENT:
                ++duration;
                if (duration > Constants.DURATION_MAX.value)
                    return;
                break;
        }
        int finalProgress = duration;
        handler.post(new Runnable() {
            public void run() {
                toUpdateTxt.setText(String.valueOf(finalProgress));
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
