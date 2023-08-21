package com.example.soundwave.components;

import java.util.HashMap;

public class ControlPanelComponent {
    public enum Button {
        GENERATE,
        PLAY_STOP,
        SAVE,
        RESET
    }

    public enum ButtonState {
        STANDARD,
        INACTIVE,
        DONE,
        SECOND_FUNCTION
    }

    private final HashMap<Button, ButtonState> buttonsStates = new HashMap<>();

    public ControlPanelComponent(ButtonState generateBtnState, ButtonState playStopBtnState, ButtonState saveBtnState, ButtonState resetBtnState) {
        buttonsStates.put(Button.GENERATE, generateBtnState);
        buttonsStates.put(Button.PLAY_STOP, playStopBtnState);
        buttonsStates.put(Button.SAVE, saveBtnState);
        buttonsStates.put(Button.RESET, resetBtnState);
    }

    public HashMap<Button, ButtonState> getButtonsStates() {
        return buttonsStates;
    }
}
