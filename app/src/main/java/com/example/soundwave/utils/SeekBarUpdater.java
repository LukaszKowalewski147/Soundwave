package com.example.soundwave.utils;

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
            changeFrequency();
            try {
                Thread.sleep(refreshRate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void changeFrequency() {
        int frequency = Integer.parseInt(toUpdateTxt.getText().toString());
        switch (operation) {
            case FREQUENCY_DECREMENT:
                --frequency;
                if (frequency < Config.FREQUENCY_MIN.value)
                    return;
                break;
            case FREQUENCY_INCREMENT:
                ++frequency;
                if (frequency > Config.FREQUENCY_MAX.value)
                    return;
                break;
        }
        int finalProgress = frequency;
        handler.post(() -> toUpdateTxt.setText(String.valueOf(finalProgress)));
    }

    private Options.ButtonLongPressState getState() {
        Options.ButtonLongPressState state = Options.ButtonLongPressState.RELEASED;
        switch (operation) {
            case FREQUENCY_DECREMENT:
                state = Options.buttonDecrementFrequencyState;
                break;
            case FREQUENCY_INCREMENT:
                state = Options.buttonIncrementFrequencyState;
                break;
        }
        return state;
    }

    private int getRefreshRate() {
        return Config.SEEKBAR_CHANGE_REFRESH_RATE.value;
    }
}
