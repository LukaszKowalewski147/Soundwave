package com.example.soundwave.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.example.soundwave.SeekBarUpdater;
import com.example.soundwave.utils.Config;
import com.example.soundwave.R;
import com.example.soundwave.utils.Options;
import com.example.soundwave.utils.Preset;
import com.example.soundwave.utils.UnitsConverter;
import com.example.soundwave.databinding.FragmentToneCreatorBinding;
import com.example.soundwave.viewmodel.ToneCreatorViewModel;

public class ToneCreatorFragment extends Fragment {

    private FragmentToneCreatorBinding binding;
    private ToneCreatorViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToneCreatorBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ToneCreatorViewModel.class);
        initializeDefaultLayout();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initializeObservers() {
        viewModel.getEnvelopeAttack().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                binding.toneCreatorEnvelopeAttack.setText(String.valueOf(viewModel.getEnvelopeAttack().getValue()));
            }
        });

        viewModel.getEnvelopeDecay().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                binding.toneCreatorEnvelopeDecay.setText(String.valueOf(viewModel.getEnvelopeDecay().getValue()));
            }
        });

        viewModel.getEnvelopeSustainLevel().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                binding.toneCreatorEnvelopeSustainLevel.setText(String.valueOf(viewModel.getEnvelopeSustainLevel().getValue()));
            }
        });

        viewModel.getEnvelopeSustainDuration().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                binding.toneCreatorEnvelopeSustainDuration.setText(String.valueOf(viewModel.getEnvelopeSustainDuration().getValue()));
            }
        });

        viewModel.getEnvelopeRelease().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                binding.toneCreatorEnvelopeRelease.setText(String.valueOf(viewModel.getEnvelopeRelease().getValue()));
            }
        });

        viewModel.getFundamentalFrequency().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                binding.toneCreatorFundamentalFrequencyInput.setText(String.valueOf(viewModel.getFundamentalFrequency().getValue()));
            }
        });

        viewModel.getFundamentalFrequencyBar().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                binding.toneCreatorFundamentalFrequencyBar.setProgress(viewModel.getFundamentalFrequencyBar().getValue());
            }
        });

        viewModel.getScale().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                binding.toneCreatorScaleInput.setText(String.valueOf(viewModel.getScale().getValue()));
            }
        });

        viewModel.getMasterVolume().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                binding.toneCreatorMasterVolumeInput.setText(String.valueOf(viewModel.getMasterVolume().getValue()));
            }
        });

        viewModel.getMasterVolumeBar().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                binding.toneCreatorMasterVolumeBar.setProgress(viewModel.getMasterVolumeBar().getValue());
            }
        });
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