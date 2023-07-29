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
        binding.toneCreatorEnvelopeAttack.setText("100");
        binding.toneCreatorEnvelopeDecay.setText("250");
        binding.toneCreatorEnvelopeSustainLevel.setText("40");
        binding.toneCreatorEnvelopeSustainDuration.setText("1000");
        binding.toneCreatorEnvelopeRelease.setText("200");

        //  Fundamental frequency
        int displayFrequency = UnitsConverter.convertSeekBarProgressToFrequency(Config.FREQUENCY_PROGRESS_BAR_DEFAULT.value);
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

    private void initializeUIListeners() {
        binding.toneCreatorEnvelopeAttack.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateEnvelopeAttack(s.toString());
            }
        });

        binding.toneCreatorEnvelopeDecay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateEnvelopeDecay(s.toString());
            }
        });

        binding.toneCreatorEnvelopeSustainLevel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateEnvelopeSustainLevel(s.toString());
            }
        });

        binding.toneCreatorEnvelopeSustainDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateEnvelopeSustainDuration(s.toString());
            }
        });

        binding.toneCreatorEnvelopeRelease.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateEnvelopeRelease(s.toString());
            }
        });

        binding.toneCreatorFundamentalFrequencyInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateFundamentalFrequency(s.toString());
            }
        });

        binding.toneCreatorFundamentalFrequencyInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    viewModel.validateFundamentalFrequencyInput(binding.toneCreatorFundamentalFrequencyInput.getText().toString());
            }
        });

        binding.toneCreatorFundamentalFrequencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateFundamentalFrequencySeekBarPosition(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.toneCreatorFrequencyDecrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.decrementOnceFundamentalFrequency();
            }
        });

        binding.toneCreatorFrequencyDecrementBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                viewModel.decrementConstantlyFundamentalFrequency();
                return true;
            }
        });

        binding.toneCreatorFrequencyDecrementBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    viewModel.validateFundamentalFrequencyInput(binding.toneCreatorFundamentalFrequencyInput.getText().toString());
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    Options.buttonDecrementFrequencyState = Options.ButtonLongPressState.RELEASED;
                }
                return false;
            }
        });

        binding.toneCreatorFrequencyIncrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.incrementOnceFundamentalFrequency();
            }
        });

        binding.toneCreatorFrequencyIncrementBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                viewModel.incrementConstantlyFundamentalFrequency();
                return true;
            }
        });

        binding.toneCreatorFrequencyIncrementBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    viewModel.validateFundamentalFrequencyInput(binding.toneCreatorFundamentalFrequencyInput.getText().toString());
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    Options.buttonIncrementFrequencyState = Options.ButtonLongPressState.RELEASED;
                }
                return false;
            }
        });

        binding.toneCreatorMasterVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                viewModel.updateMasterVolumeSeekBarPosition(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.toneCreatorOvertonesActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.toneCreatorOvertonesLayout.setVisibility(View.VISIBLE);
                    binding.toneCreatorOvertonesPreset.setVisibility(View.VISIBLE);
                    return;
                }
                binding.toneCreatorOvertonesLayout.setVisibility(View.GONE);
                binding.toneCreatorOvertonesPreset.setVisibility(View.GONE);
            }
        });

        binding.toneCreatorOvertonesPreset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Preset targetOvertonesPreset = Options.convertStringToPreset(overtonesPreset.getSelectedItem().toString());
                Preset currentOvertonesPreset = getPresetForTone();
                if (targetOvertonesPreset == currentOvertonesPreset)
                    return;
                Options.tone1Preset = targetOvertonesPreset;

                initializeOvertoneManagers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateToneDetails();
                loadSound();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSound();
            }
        });

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                managePlayPauseActivity();
            }
        });

        replayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPlayback();
            }
        });

        loopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageLoopButton();
            }
        });

        playbackBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        playbackModesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String spinnerPlaybackMode = playbackModesSpinner.getSelectedItem().toString();
                Options.PlaybackMode selectedPlaybackMode = Options.PlaybackMode.STATIC;
                if (spinnerPlaybackMode.equals("Stream"))
                    selectedPlaybackMode = Options.PlaybackMode.STREAM;
                setPlaybackMode(selectedPlaybackMode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}