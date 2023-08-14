package com.example.soundwave.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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

import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.databinding.OvertoneCreatorBinding;
import com.example.soundwave.model.entity.Overtone;
import com.example.soundwave.utils.Config;
import com.example.soundwave.R;
import com.example.soundwave.utils.Options;
import com.example.soundwave.databinding.FragmentToneCreatorBinding;
import com.example.soundwave.viewmodel.ToneCreatorViewModel;

public class ToneCreatorFragment extends Fragment {

    private ToneCreatorViewModel viewModel;
    private FragmentToneCreatorBinding binding;
    private OvertoneCreatorBinding[] overtoneBindings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToneCreatorBinding.inflate(inflater, container, false);
        initializeOvertoneBindings();
        viewModel = new ViewModelProvider(this).get(ToneCreatorViewModel.class);
        initializeDefaultLayout();
        initializeObservers();
        initializeUIListeners();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        overtoneBindings = null;
    }

    private void initializeOvertoneBindings() {
        overtoneBindings = new OvertoneCreatorBinding[Config.OVERTONES_NUMBER.value];
        overtoneBindings[0] = binding.toneCreatorOvertone0;
        overtoneBindings[1] = binding.toneCreatorOvertone1;
        overtoneBindings[2] = binding.toneCreatorOvertone2;
        overtoneBindings[3] = binding.toneCreatorOvertone3;
        overtoneBindings[4] = binding.toneCreatorOvertone4;
        overtoneBindings[5] = binding.toneCreatorOvertone5;
        overtoneBindings[6] = binding.toneCreatorOvertone6;
        overtoneBindings[7] = binding.toneCreatorOvertone7;
        overtoneBindings[8] = binding.toneCreatorOvertone8;
        overtoneBindings[9] = binding.toneCreatorOvertone9;
        overtoneBindings[10] = binding.toneCreatorOvertone10;
        overtoneBindings[11] = binding.toneCreatorOvertone11;
        overtoneBindings[12] = binding.toneCreatorOvertone12;
        overtoneBindings[13] = binding.toneCreatorOvertone13;
        overtoneBindings[14] = binding.toneCreatorOvertone14;
    }

    private void initializeObservers() {
        viewModel.getEnvelopeComponent().observe(getViewLifecycleOwner(), new Observer<EnvelopeComponent>() {
            @Override
            public void onChanged(EnvelopeComponent envelopeComponent) {
                int attackDuration = envelopeComponent.getAttackDuration();
                int decayDuration = envelopeComponent.getDecayDuration();
                int sustainLevel = envelopeComponent.getSustainLevel();
                int sustainDuration = envelopeComponent.getSustainDuration();
                int releaseDuration = envelopeComponent.getReleaseDuration();

                if (!binding.toneCreatorEnvelopeAttack.getText().toString().equals(String.valueOf(attackDuration)))
                    binding.toneCreatorEnvelopeAttack.setText(String.valueOf(attackDuration));
                if (!binding.toneCreatorEnvelopeDecay.getText().toString().equals(String.valueOf(decayDuration)))
                    binding.toneCreatorEnvelopeDecay.setText(String.valueOf(decayDuration));
                if (!binding.toneCreatorEnvelopeSustainLevel.getText().toString().equals(String.valueOf(sustainLevel)))
                    binding.toneCreatorEnvelopeSustainLevel.setText(String.valueOf(sustainLevel));
                if (!binding.toneCreatorEnvelopeSustainDuration.getText().toString().equals(String.valueOf(sustainDuration)))
                    binding.toneCreatorEnvelopeSustainDuration.setText(String.valueOf(sustainDuration));
                if (!binding.toneCreatorEnvelopeRelease.getText().toString().equals(String.valueOf(releaseDuration)))
                    binding.toneCreatorEnvelopeRelease.setText(String.valueOf(releaseDuration));
            }
        });

        viewModel.getFundamentalFrequency().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                if (!binding.toneCreatorFundamentalFrequencyInput.getText().toString().equals(String.valueOf(s)))
                    binding.toneCreatorFundamentalFrequencyInput.setText(String.valueOf(s));
            }
        });

        viewModel.getFundamentalFrequencyBar().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                binding.toneCreatorFundamentalFrequencyBar.setProgress(viewModel.getFundamentalFrequencyBar().getValue());
                binding.toneCreatorScaleInput.setText(String.valueOf(viewModel.getScale()));
            }
        });

        viewModel.getMasterVolume().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                binding.toneCreatorMasterVolumeInput.setText(String.valueOf(s));
                binding.toneCreatorMasterVolumeBar.setProgress(s);
            }
        });

        viewModel.getOvertones().observe(getViewLifecycleOwner(), new Observer<Overtone[]>() {
            @Override
            public void onChanged(Overtone[] overtones) {
                for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i) {
                    updateOvertoneView(i, overtones[i]);
                }
                binding.toneCreatorOvertonesPreset.setSelection(viewModel.getOvertonesPresetPosition());
            }
        });
    }

    private void updateOvertoneView(int overtoneIndex, Overtone overtone) {
        overtoneBindings[overtoneIndex].overtoneCreatorActivator.setChecked(overtone.isActive());
        overtoneBindings[overtoneIndex].overtoneCreatorFrequency.setText(String.valueOf(overtone.getFrequency()) + "Hz");
        overtoneBindings[overtoneIndex].overtoneCreatorVolumeInput.setText(String.valueOf(overtone.getAmplitude()));
        overtoneBindings[overtoneIndex].overtoneCreatorVolumeBar.setProgress(overtone.getAmplitude());
    }

    private void initializeDefaultLayout() {
        //  Spinners
        ArrayAdapter<CharSequence> sampleRatesAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.sample_rates_array, android.R.layout.simple_spinner_item);
        sampleRatesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.toneCreatorSampleRatesSpinner.setAdapter(sampleRatesAdapter);

        ArrayAdapter<CharSequence> envelopePresetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.envelope_presets_array, android.R.layout.simple_spinner_item);
        envelopePresetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.toneCreatorEnvelopePresetSpinner.setAdapter(envelopePresetAdapter);

        ArrayAdapter<CharSequence> overtonesPresetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.overtones_presets_array, android.R.layout.simple_spinner_item);
        overtonesPresetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.toneCreatorOvertonesPreset.setAdapter(overtonesPresetAdapter);

        //  Bars
        binding.toneCreatorFundamentalFrequencyBar.setMax(Config.FREQUENCY_PROGRESS_BAR_MAX.value);

        // Overtone indexes
        for (int i = 0; i < overtoneBindings.length; i++) {
            overtoneBindings[i].overtoneCreatorIndex.setText(viewModel.getIndexWithSuffix(i + 1));
        }

        // Layout visibility
        binding.toneCreatorOvertonesLayout.setVisibility(View.GONE);
        binding.toneCreatorOvertonesPreset.setVisibility(View.GONE);
    }

    private void initializeUIListeners() {
        binding.toneCreatorEnvelopePresetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.updateEnvelopePreset(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.toneCreatorEnvelopeAttack.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateEnvelopeParameter(EnvelopeComponent.EnvelopeParameters.ATTACK_DURATION, s.toString());
                binding.toneCreatorEnvelopePresetSpinner.setSelection(viewModel.getEnvelopePresetPosition());
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
                viewModel.updateEnvelopeParameter(EnvelopeComponent.EnvelopeParameters.DECAY_DURATION, s.toString());
                binding.toneCreatorEnvelopePresetSpinner.setSelection(viewModel.getEnvelopePresetPosition());
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
                viewModel.updateEnvelopeParameter(EnvelopeComponent.EnvelopeParameters.SUSTAIN_LEVEL, s.toString());
                binding.toneCreatorEnvelopePresetSpinner.setSelection(viewModel.getEnvelopePresetPosition());
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
                viewModel.updateEnvelopeParameter(EnvelopeComponent.EnvelopeParameters.SUSTAIN_DURATION, s.toString());
                binding.toneCreatorEnvelopePresetSpinner.setSelection(viewModel.getEnvelopePresetPosition());
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
                viewModel.updateEnvelopeParameter(EnvelopeComponent.EnvelopeParameters.RELEASE_DURATION, s.toString());
                binding.toneCreatorEnvelopePresetSpinner.setSelection(viewModel.getEnvelopePresetPosition());
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
                viewModel.updateOvertonesState(isChecked);
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
                viewModel.updateOvertonesPreset(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        overtoneBindings[0].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.updateOvertoneState(0, isChecked);
            }
        });

        overtoneBindings[1].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.updateOvertoneState(1, isChecked);
            }
        });

        overtoneBindings[2].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.updateOvertoneState(2, isChecked);
            }
        });

        overtoneBindings[3].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.updateOvertoneState(3, isChecked);
            }
        });

        overtoneBindings[4].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.updateOvertoneState(4, isChecked);
            }
        });

        overtoneBindings[5].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.updateOvertoneState(5, isChecked);
            }
        });

        overtoneBindings[6].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.updateOvertoneState(6, isChecked);
            }
        });

        overtoneBindings[7].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.updateOvertoneState(7, isChecked);
            }
        });

        overtoneBindings[8].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.updateOvertoneState(8, isChecked);
            }
        });

        overtoneBindings[9].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.updateOvertoneState(9, isChecked);
            }
        });

        overtoneBindings[10].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.updateOvertoneState(10, isChecked);
            }
        });

        overtoneBindings[11].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.updateOvertoneState(11, isChecked);
            }
        });

        overtoneBindings[12].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.updateOvertoneState(12, isChecked);
            }
        });

        overtoneBindings[13].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.updateOvertoneState(13, isChecked);
            }
        });

        overtoneBindings[14].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.updateOvertoneState(14, isChecked);
            }
        });

        overtoneBindings[0].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateOvertoneAmplitude(0, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        overtoneBindings[1].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateOvertoneAmplitude(1, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        overtoneBindings[2].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateOvertoneAmplitude(2, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        overtoneBindings[3].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateOvertoneAmplitude(3, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        overtoneBindings[4].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateOvertoneAmplitude(4, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        overtoneBindings[5].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateOvertoneAmplitude(5, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        overtoneBindings[6].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateOvertoneAmplitude(6, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        overtoneBindings[7].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateOvertoneAmplitude(7, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        overtoneBindings[8].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateOvertoneAmplitude(8, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        overtoneBindings[9].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateOvertoneAmplitude(9, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        overtoneBindings[10].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateOvertoneAmplitude(10, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        overtoneBindings[11].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateOvertoneAmplitude(11, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        overtoneBindings[12].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateOvertoneAmplitude(12, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        overtoneBindings[13].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateOvertoneAmplitude(13, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        overtoneBindings[14].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    viewModel.updateOvertoneAmplitude(14, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.toneCreatorGenerateToneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.generateTone();
            }
        });

        binding.toneCreatorPlayToneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.playTone();
            }
        });

        binding.toneCreatorSaveToneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.saveTone();
            }
        });

        binding.toneCreatorResetToneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.resetTone();
            }
        });
    }
}