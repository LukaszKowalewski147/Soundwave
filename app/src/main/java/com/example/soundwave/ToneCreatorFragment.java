package com.example.soundwave;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;

import com.example.soundwave.databinding.FragmentToneCreatorBinding;

public class ToneCreatorFragment extends Fragment {

    private FragmentToneCreatorBinding binding;

    public ToneCreatorFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.fragment_tone_creator, container, false);  - old
        binding = FragmentToneCreatorBinding.inflate(inflater, container, false);
        initializeDefaultLayout();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initializeDefaultLayout() {
        //  Spinners
        ArrayAdapter<CharSequence> sampleRatesAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.sample_rates_array, android.R.layout.simple_spinner_item);
        sampleRatesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.toneCreatorSampleRatesSpinner.setAdapter(sampleRatesAdapter);

        ArrayAdapter<CharSequence> overtonesPresetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.overtones_presets_array, android.R.layout.simple_spinner_item);
        overtonesPresetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.toneCreatorOvertonesPreset.setAdapter(overtonesPresetAdapter);

        //  Envelope
        binding.toneCreatorEnvelopeAttack.setText("100ms");
        binding.toneCreatorEnvelopeDecay.setText("250ms");
        binding.toneCreatorEnvelopeSustainLevel.setText("40%");
        binding.toneCreatorEnvelopeSustainDuration.setText("1000ms");
        binding.toneCreatorEnvelopeRelease.setText("200ms");

        //  Fundamental frequency
        int displayFrequency = UnitsConverter.convertSeekBarPositionToFrequency(Config.FREQUENCY_PROGRESS_BAR_DEFAULT.value);
        binding.toneCreatorFundamentalFrequencyInput.setText(String.valueOf(displayFrequency));
        binding.toneCreatorFundamentalFrequencyBar.setMax(Config.FREQUENCY_PROGRESS_BAR_MAX.value);
        binding.toneCreatorFundamentalFrequencyBar.setProgress(Config.FREQUENCY_PROGRESS_BAR_DEFAULT.value);
        binding.toneCreatorScaleInput.setText("C#4");
        // TODO: scale input

        //  Volume
        binding.toneCreatorMasterVolumeBar.setProgress(100);
        binding.toneCreatorMasterVolumeInput.setText(String.valueOf(binding.toneCreatorMasterVolumeBar.getProgress()));

        // Layout visibility
        binding.toneCreatorOvertonesLayout.setVisibility(View.GONE);
        binding.toneCreatorOvertonesPreset.setVisibility(View.GONE);
    }
}