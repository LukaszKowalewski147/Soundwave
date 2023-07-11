package com.example.soundwave;

import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class OvertoneManager {

    private final View overtoneView;
    private final int overtoneIndex;
    private final int toneIndex;

    private Switch activator;
    private SeekBar amplitudeBar;
    private TextView amplitudeInput;
    private TextView indexView;
    private TextView frequency;

    public OvertoneManager(View overtoneView, int index, int frequency, Preset preset, int toneIndex) {
        this.overtoneView = overtoneView;
        this.overtoneIndex = index - 1;
        this.toneIndex = toneIndex;
        initializeUIElements();
        initializeUIValues(index, frequency, preset);
        initializeUIListeners();
    }

    public boolean isActive() {
        return activator.isChecked();
    }

    public SineWave getSineWave() {
        int frequency = Integer.parseInt(this.frequency.getText().toString());
        double amplitude = getAmplitude();
        return new SineWave(frequency, amplitude);
    }

    private double getAmplitude() {
        int displayAmplitude = Integer.parseInt(amplitudeInput.getText().toString());
        return (double) displayAmplitude / 100.0d;
    }

    private void initializeUIElements() {
        activator = overtoneView.findViewById(R.id.activator);
        amplitudeBar = overtoneView.findViewById(R.id.master_volume_bar);
        amplitudeInput = overtoneView.findViewById(R.id.master_volume_input);
        indexView = overtoneView.findViewById(R.id.index);
        frequency = overtoneView.findViewById(R.id.frequency);
    }

    private void initializeUIValues(int index, int frequency, Preset preset) {
        this.indexView.setText(getDisplayIndex(index));
        this.frequency.setText(String.valueOf(frequency));
        amplitudeBar.setProgress(preset.amplitudes[overtoneIndex]);
        amplitudeInput.setText(String.valueOf(amplitudeBar.getProgress()));
    }

    private void initializeUIListeners() {
        amplitudeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                amplitudeInput.setText(String.valueOf(progress));
                if (fromUser) {
                    switch (toneIndex) {
                        case 1:
                            Options.tone1Preset = Preset.FLAT;
                            Preset.FLAT.amplitudes[overtoneIndex] = progress;
                            break;
                        case 2:
                            Options.tone2Preset = Preset.FLAT;
                            Preset.FLAT.amplitudes[overtoneIndex] = progress;
                            break;
                    }
                    if (progress == 0) activator.setChecked(false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private String getDisplayIndex(int index) {
        String displayIndex = index + "th";
        switch (index) {
            case 1:
                displayIndex = index + "st";
                return displayIndex;
            case 2:
                displayIndex = index + "nd";
                return displayIndex;
            case 3:
                displayIndex = index + "rd";
                return displayIndex;
        }
        return displayIndex;
    }
}
