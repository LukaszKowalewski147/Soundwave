package com.example.soundwave.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;

import com.example.soundwave.MainActivity;
import com.example.soundwave.utils.PresetOvertones;
import com.example.soundwave.utils.SeekBarUpdater;
import com.example.soundwave.Tone;
import com.example.soundwave.utils.WavCreator;
import com.example.soundwave.components.ControlPanelComponent;
import com.example.soundwave.components.EnvelopeComponent;
import com.example.soundwave.components.FundamentalFrequencyComponent;
import com.example.soundwave.databinding.OvertoneCreatorBinding;
import com.example.soundwave.Overtone;
import com.example.soundwave.utils.Config;
import com.example.soundwave.R;
import com.example.soundwave.utils.Options;
import com.example.soundwave.databinding.FragmentToneCreatorBinding;
import com.example.soundwave.utils.SampleRate;
import com.example.soundwave.utils.Scale;
import com.example.soundwave.utils.UnitsConverter;
import com.example.soundwave.viewmodel.ToneCreatorViewModel;

import java.io.File;

public class ToneCreatorFragment extends Fragment {

    private ToneCreatorViewModel viewModel;
    private FragmentToneCreatorBinding binding;
    private OvertoneCreatorBinding[] overtoneBindings;

    private Tone editedTone;
    private boolean editorMode = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentToneCreatorBinding.inflate(inflater, container, false);
        initializeOvertoneBindings();
        viewModel = new ViewModelProvider(this).get(ToneCreatorViewModel.class);
        initializeDefaultLayout();
        initializeObservers();
        initializeUIListeners();
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (getActivity() != null) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.selectToneCreatorOnBottomNav();
            }
            editorMode = true;
            editedTone = (Tone) bundle.getSerializable("tone");
            viewModel.loadEditedTone(editedTone);
            initializeToneEditorLayout();
        }
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        overtoneBindings = null;
        Options.lastOvertonePreset = PresetOvertones.FLAT;
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

        //  Note picker
        Scale[] notes = Scale.values();
        int notesCount = notes.length;
        String[] noteNames = new String[notesCount];

        for (int i = 0; i < notesCount; ++i) {
            noteNames[i] = notes[i].noteName;
        }

        binding.toneCreatorNoteInput.setMinValue(0);
        binding.toneCreatorNoteInput.setMaxValue(notesCount - 1);
        binding.toneCreatorNoteInput.setDisplayedValues(noteNames);

        //  Bars
        binding.toneCreatorFundamentalFrequencyBar.setMax(Config.FREQUENCY_PROGRESS_BAR_MAX.value);

        //  Overtone indexes
        for (int i = 0; i < overtoneBindings.length; i++) {
            overtoneBindings[i].overtoneCreatorIndex.setText(viewModel.getIndexWithSuffix(i + 1));
        }

        //  Layout visibility
        binding.toneCreatorOvertonesLayout.setVisibility(View.GONE);
        binding.toneCreatorOvertonesPreset.setVisibility(View.GONE);

        manageToneDetails(null);
    }

    private void initializeObservers() {
        viewModel.getSampleRate().observe(getViewLifecycleOwner(), new Observer<SampleRate>() {
            @Override
            public void onChanged(SampleRate sampleRate) {
                binding.toneCreatorSampleRatesSpinner.setSelection(UnitsConverter.convertSampleRateToPosition(sampleRate));
            }
        });

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

        viewModel.getFundamentalFrequencyComponent().observe(getViewLifecycleOwner(), new Observer<FundamentalFrequencyComponent>() {
            @Override
            public void onChanged(FundamentalFrequencyComponent fundamentalFrequencyComponent) {
                int fundamentalFrequency = fundamentalFrequencyComponent.getFundamentalFrequency();
                int fundamentalFrequencyBar = fundamentalFrequencyComponent.getFundamentalFrequencyBar();
                int masterVolume = fundamentalFrequencyComponent.getMasterVolume();
                int noteIndex = fundamentalFrequencyComponent.getNoteIndex();

                if (!binding.toneCreatorFundamentalFrequencyInput.getText().toString().equals(String.valueOf(fundamentalFrequency)))
                    binding.toneCreatorFundamentalFrequencyInput.setText(String.valueOf(fundamentalFrequency));

                binding.toneCreatorNoteInput.setValue(noteIndex);
                binding.toneCreatorFundamentalFrequencyBar.setProgress(fundamentalFrequencyBar);
                binding.toneCreatorMasterVolumeInput.setText(String.valueOf(masterVolume));
                binding.toneCreatorMasterVolumeBar.setProgress(masterVolume);

                manageMasterVolumeIcon(masterVolume);
            }
        });

        viewModel.getControlPanelComponent().observe(getViewLifecycleOwner(), new Observer<ControlPanelComponent>() {
            @Override
            public void onChanged(ControlPanelComponent controlPanelComponent) {
                manageControlPanel(controlPanelComponent);
            }
        });

        viewModel.getOvertones().observe(getViewLifecycleOwner(), new Observer<Overtone[]>() {
            @Override
            public void onChanged(Overtone[] overtones) {
                for (int i = 0; i < Config.OVERTONES_NUMBER.value; ++i) {
                    updateOvertoneView(i, overtones[i]);
                    manageOvertoneVolumeIcon(i, overtones[i].getAmplitude());
                }
                binding.toneCreatorOvertonesPreset.setSelection(viewModel.getOvertonesPresetPosition());
            }
        });

        viewModel.getTone().observe(getViewLifecycleOwner(), new Observer<Tone>() {
            @Override
            public void onChanged(Tone tone) {
                manageToneDetails(tone);
            }
        });
    }

    private void updateOvertoneView(int overtoneIndex, Overtone overtone) {
        overtoneBindings[overtoneIndex].overtoneCreatorActivator.setChecked(overtone.isActive());
        overtoneBindings[overtoneIndex].overtoneCreatorFrequency.setText(String.valueOf(overtone.getFrequency()) + "Hz");
        double amplitude = overtone.getAmplitude();
        String volumeInputSuffix = "";
        if (amplitude >= 0.0d)
            volumeInputSuffix = "+";
        overtoneBindings[overtoneIndex].overtoneCreatorVolumeInput.setText(volumeInputSuffix + amplitude + "dB");
        overtoneBindings[overtoneIndex].overtoneCreatorVolumeBar.setProgress(UnitsConverter.convertOvertoneDbHumanValueToSliderProgress(amplitude));
    }

    private void initializeUIListeners() {
        binding.toneCreatorSampleRatesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.updateSampleRate(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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

        binding.toneCreatorNoteInput.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                viewModel.updateNoteName(newVal);
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
                Options.buttonDecrementFrequencyState = Options.ButtonLongPressState.PRESSED;
                SeekBarUpdater seekBarUpdater = new SeekBarUpdater(new Handler(), binding.toneCreatorFundamentalFrequencyInput, Options.Operation.FREQUENCY_DECREMENT);
                Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
                seekBarUpdaterThread.start();
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
                Options.buttonIncrementFrequencyState = Options.ButtonLongPressState.PRESSED;
                SeekBarUpdater seekBarUpdater = new SeekBarUpdater(new Handler(), binding.toneCreatorFundamentalFrequencyInput, Options.Operation.FREQUENCY_INCREMENT);
                Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
                seekBarUpdaterThread.start();
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
                if (fromUser)
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
                    if (editorMode)
                        viewModel.setNoChange();
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

        for (int i = 0; i < overtoneBindings.length; ++i) {
            int index = i;
            overtoneBindings[index].overtoneCreatorActivator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    viewModel.updateOvertoneState(index, isChecked);
                    if (editorMode)
                        viewModel.setNoChange();
                }
            });

            overtoneBindings[index].overtoneCreatorVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        viewModel.updateOvertoneAmplitude(index, progress);
                        if (progress == 0)
                            overtoneBindings[index].overtoneCreatorActivator.setChecked(false);
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

        binding.toneCreatorGenerateToneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.alert_dialog_tone_creator_generate_message);
                builder.setPositiveButton(R.string.alert_dialog_tone_creator_generate_positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        takeGenerateToneAction();
                    }
                });
                builder.setNegativeButton(R.string.alert_dialog_tone_creator_generate_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        binding.toneCreatorPlayStopToneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.playStopTone();
            }
        });

        binding.toneCreatorSaveToneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.alert_dialog_tone_creator_save_message);

                final EditText toneName = new EditText(getContext());
                toneName.setInputType(InputType.TYPE_CLASS_TEXT);

                if (editorMode)
                    toneName.setText(editedTone.getName());

                builder.setView(toneName);

                builder.setPositiveButton(R.string.alert_dialog_tone_creator_save_positive, null);
                builder.setNegativeButton(R.string.alert_dialog_tone_creator_save_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

                AlertDialog dialog = builder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String nameToSave = toneName.getText().toString().trim();
                                if (!nameToSave.isEmpty()) {
                                    takeSaveToneAction(nameToSave);
                                    dialog.dismiss();
                                } else {
                                    toneName.setError(getString(R.string.error_msg_empty_name));
                                }
                            }
                        });
                    }
                });

                dialog.show();
            }
        });

        binding.toneCreatorResetToneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.alert_dialog_tone_creator_reset_message);
                builder.setPositiveButton(R.string.alert_dialog_tone_creator_reset_positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        takeResetAction();
                    }
                });
                builder.setNegativeButton(R.string.alert_dialog_tone_creator_reset_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void initializeToneEditorLayout() {
        binding.toneCreatorToneName.setText(editedTone.getName());
        binding.toneCreatorToneName.setVisibility(View.VISIBLE);
        binding.toneCreatorToneName.setSelected(true);

        if (viewModel.getTone().getValue().getOvertonesPreset() != PresetOvertones.NONE) {
            binding.toneCreatorOvertonesActivator.setChecked(true);
        }
    }

    private void takeGenerateToneAction() {
        viewModel.generateTone(editorMode);
    }

    private void takeSaveToneAction(String toneName) {
        File file = getActivity().getExternalFilesDir(WavCreator.getFileFolder());
        viewModel.saveTone(toneName, file, editorMode);
    }

    private void takeResetAction() {
        if (editorMode)
            viewModel.loadEditedTone(editedTone);
        else {
            binding.toneCreatorOvertonesActivator.setChecked(false);
            viewModel.resetTone();
        }
    }

    private void manageControlPanel(ControlPanelComponent controlPanelComponent) {
        Drawable icon;
        ControlPanelComponent.ButtonState generateBtnState = controlPanelComponent.getButtonsStates().get(ControlPanelComponent.Button.GENERATE);
        ControlPanelComponent.ButtonState playStopBtnState = controlPanelComponent.getButtonsStates().get(ControlPanelComponent.Button.PLAY_STOP);
        ControlPanelComponent.ButtonState saveBtnState = controlPanelComponent.getButtonsStates().get(ControlPanelComponent.Button.SAVE);
        ControlPanelComponent.ButtonState resetBtnState = controlPanelComponent.getButtonsStates().get(ControlPanelComponent.Button.RESET);

        switch (generateBtnState) {
            case STANDARD:
                binding.toneCreatorGenerateToneBtn.setEnabled(true);
                binding.toneCreatorGenerateToneBtn.setBackgroundResource(R.drawable.background_btn_standard);
                break;
            case INACTIVE:
                binding.toneCreatorGenerateToneBtn.setEnabled(false);
                binding.toneCreatorGenerateToneBtn.setBackgroundResource(R.drawable.background_btn_inactive);
        }

        switch (playStopBtnState) {
            case STANDARD:
                binding.toneCreatorPlayStopToneBtn.setEnabled(true);
                binding.toneCreatorPlayStopToneBtn.setBackgroundResource(R.drawable.background_btn_standard);
                binding.toneCreatorPlayStopToneBtn.setText(R.string.play_btn);
                icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_note, null);
                binding.toneCreatorPlayStopToneBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                break;
            case INACTIVE:
                binding.toneCreatorPlayStopToneBtn.setEnabled(false);
                binding.toneCreatorPlayStopToneBtn.setBackgroundResource(R.drawable.background_btn_inactive);
                binding.toneCreatorPlayStopToneBtn.setText(R.string.play_btn);
                icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_note, null);
                binding.toneCreatorPlayStopToneBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                break;
            case SECOND_FUNCTION:
                binding.toneCreatorPlayStopToneBtn.setEnabled(true);
                binding.toneCreatorPlayStopToneBtn.setBackgroundResource(R.drawable.background_btn_red);
                binding.toneCreatorPlayStopToneBtn.setText(R.string.stop_btn);
                icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_stop, null);
                binding.toneCreatorPlayStopToneBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        }

        switch (saveBtnState) {
            case STANDARD:
                binding.toneCreatorSaveToneBtn.setEnabled(true);
                binding.toneCreatorSaveToneBtn.setBackgroundResource(R.drawable.background_btn_standard);
                binding.toneCreatorSaveToneBtn.setText(R.string.save_btn);
                icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_save, null);
                binding.toneCreatorSaveToneBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                break;
            case INACTIVE:
                binding.toneCreatorSaveToneBtn.setEnabled(false);
                binding.toneCreatorSaveToneBtn.setBackgroundResource(R.drawable.background_btn_inactive);
                binding.toneCreatorSaveToneBtn.setText(R.string.save_btn);
                icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_save, null);
                binding.toneCreatorSaveToneBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                break;
            case DONE:
                binding.toneCreatorSaveToneBtn.setEnabled(false);
                binding.toneCreatorSaveToneBtn.setBackgroundResource(R.drawable.background_btn_green);
                binding.toneCreatorSaveToneBtn.setText(R.string.saved_btn);
                icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_done, null);
                binding.toneCreatorSaveToneBtn.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        }

        switch (resetBtnState) {
            case STANDARD:
                binding.toneCreatorResetToneBtn.setEnabled(true);
                binding.toneCreatorResetToneBtn.setBackgroundResource(R.drawable.background_btn_standard);
                break;
            case INACTIVE:
                binding.toneCreatorResetToneBtn.setEnabled(false);
                binding.toneCreatorResetToneBtn.setBackgroundResource(R.drawable.background_btn_inactive);
        }
    }

    private void manageToneDetails(Tone tone) {
        String sampleRateString = "-";
        String frequencyString = "-";
        String envelopeString = "-";
        String timbreString = "-";

        if (tone != null) {
            String note = UnitsConverter.convertFrequencyToNote(tone.getFundamentalFrequency());

            sampleRateString = UnitsConverter.convertSampleRateToStringVisible(tone.getSampleRate());
            frequencyString = tone.getFundamentalFrequency() + "Hz (" + note + ")";
            envelopeString = UnitsConverter.convertPresetEnvelopeToString(tone.getEnvelopePreset());
            timbreString = UnitsConverter.convertPresetOvertonesToString(tone.getOvertonesPreset());
        }
        binding.toneCreatorToneDetailsSampleRate.setText(sampleRateString);
        binding.toneCreatorToneDetailsFundamentalFrequency.setText(frequencyString);
        binding.toneCreatorToneDetailsEnvelopePreset.setText(envelopeString);
        binding.toneCreatorToneDetailsTimbrePreset.setText(timbreString);
    }

    private void manageMasterVolumeIcon(int masterVolume) {
        if (masterVolume == 0)
            binding.toneCreatorVolumeIcon.setImageResource(R.drawable.ic_volume_mute);
        else if (masterVolume < binding.toneCreatorMasterVolumeBar.getMax() / 3)
            binding.toneCreatorVolumeIcon.setImageResource(R.drawable.ic_volume_low);
        else if (masterVolume < 1 + (binding.toneCreatorMasterVolumeBar.getMax() / 3) * 2)
            binding.toneCreatorVolumeIcon.setImageResource(R.drawable.ic_volume_normal);
        else
            binding.toneCreatorVolumeIcon.setImageResource(R.drawable.ic_volume_high);
    }

    private void manageOvertoneVolumeIcon(int overtoneIndex, double volume) {
        double maxVolume = overtoneBindings[overtoneIndex].overtoneCreatorVolumeBar.getMax() / 10.0d;
        volume += 40;

        if (volume < maxVolume / 3)
            overtoneBindings[overtoneIndex].overtoneCreatorVolumeIcon.setImageResource(R.drawable.ic_volume_low);
        else if (volume < (maxVolume / 3) * 2)
            overtoneBindings[overtoneIndex].overtoneCreatorVolumeIcon.setImageResource(R.drawable.ic_volume_normal);
        else
            overtoneBindings[overtoneIndex].overtoneCreatorVolumeIcon.setImageResource(R.drawable.ic_volume_high);
    }
}