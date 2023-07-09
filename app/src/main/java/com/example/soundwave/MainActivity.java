package com.example.soundwave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ConstraintLayout sampleRateLayout;
    private ConstraintLayout durationLayout;
    private ConstraintLayout tone2ActivatorLayout;
    private ConstraintLayout tone2Layout;
    private ConstraintLayout tone3ActivatorLayout;
    private ConstraintLayout tone3Layout;
    private ConstraintLayout tone4ActivatorLayout;
    private ConstraintLayout tone4Layout;
    private ConstraintLayout loadBtnLayout;
    private ConstraintLayout playbackBarLayout;
    private ImageButton durationDecrementBtn;
    private ImageButton durationIncrementBtn;
    private AppCompatButton loadBtn;
    private ImageButton saveBtn;
    private ImageButton playPauseBtn;
    private ImageButton replayBtn;
    private ImageButton loopBtn;
    private ImageView loopIndicator;
    private TextView frequencyDetails;
    private TextView durationDetailsHeader;
    private TextView durationDetails;
    private TextView sampleRateDetails;
    private TextView playbackElapsedTime;
    private TextView playbackTotalTime;
    private EditText durationTxt;
    private CheckBox tone2Activator;
    private CheckBox tone3Activator;
    private CheckBox tone4Activator;
    private SeekBar durationBar;
    private SeekBar playbackBar;
    private Spinner playbackModesSpinner;
    private Spinner sampleRatesSpinner;
    //private AppCompatButton extraBtn;

    private Sound sound;
    private ToneManager[] toneManagers;
    private PlaybackManager playbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sound = null;
        toneManagers = null;
        playbackManager = null;

        initializeUIElements();
        initializeDefaultLayout();
        initializeToneManagers();
        initializePlaybackManager();
        initializeUIListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        resetPlayback();
    }

    private void setPlaybackMode(Options.PlaybackMode selectedPlaybackMode) {
        if (Options.playbackMode.equals(selectedPlaybackMode))
            return;
        resetPlayback();
        Options.playbackMode = selectedPlaybackMode;
        setLayoutForPlaybackMode();

        if (Options.playbackMode.equals(Options.PlaybackMode.STATIC)) {
            initializePlaybackManager();
        } else {
            //TODO: tone streaming
        }
    }

    private void setLayoutForPlaybackMode() {
        int visibility = View.VISIBLE;
        if (Options.playbackMode.equals(Options.PlaybackMode.STREAM)) {
            visibility = View.GONE;
            loopIndicator.setVisibility(visibility);
        } else
            loopIndicator.setVisibility(View.INVISIBLE);

        sampleRateLayout.setVisibility(visibility);
        durationLayout.setVisibility(visibility);
        loadBtnLayout.setVisibility(visibility);
        playbackBarLayout.setVisibility(visibility);
        durationDetailsHeader.setVisibility(visibility);
        durationDetails.setVisibility(visibility);
        saveBtn.setVisibility(visibility);
        replayBtn.setVisibility(visibility);
        loopBtn.setVisibility(visibility);
    }

    private void loadSound() {
        sound = new SoundGenerator(getDuration(), getSampleRate()).generateSound(getTones());
        playbackManager.setSound(sound);
        displaySoundParameters(sound);
    }

    private void managePlayPauseActivity() {
        playbackManager.managePlayPauseActivity();
        if (Options.playbackState == Options.PlaybackState.ON) {
            Thread playbackManagerThread = new Thread(playbackManager);
            playbackManagerThread.start();
        }
    }

    private void resetPlayback() {
        playbackManager.resetPlayback();
    }

    private void saveSound() {
        WavCreator wavCreator = new WavCreator(this, sound);
        wavCreator.saveSound();
    }

    private void manageLoopButton() {
        if (loopIndicator.getVisibility() == View.INVISIBLE) {
            loopIndicator.setVisibility(View.VISIBLE);
            Options.looperState = Options.LooperState.ON;
        } else {
            loopIndicator.setVisibility(View.INVISIBLE);
            Options.looperState = Options.LooperState.OFF;
        }
    }

    private void validateToneDetails() {
        short duration = 0;
        try {
            duration = Short.parseShort(durationTxt.getText().toString());
            if (duration < Config.DURATION_MIN.value)
                durationTxt.setText(String.valueOf(Config.DURATION_MIN.value));
        } catch (Exception e) {
            Toast.makeText(this, "Duration error", Toast.LENGTH_SHORT).show();
            durationTxt.setText(String.valueOf(Config.DURATION_DEFAULT.value));
        }
    }

    private void extra() {

        //Toast.makeText(this, "threads: " + Thread.activeCount(), Toast.LENGTH_SHORT).show();

        playbackManager.extra();
    }

    private short getDuration() {
        return Short.parseShort(durationTxt.getText().toString());
    }

    private Tone[] getTones() {
        Tone[] tones = new Tone[getNumberOfTones()];
        for (int i = 0; i < tones.length; i++) {
            tones[i] = SoundGenerator.generateTone(toneManagers[i].getSineWaves());
        }
        return tones;
    }

    private int getNumberOfTones() {
        int numberOfTones = 1;
        if (tone2Activator.isChecked())
            ++numberOfTones;
        return  numberOfTones;
    }

    private SampleRate getSampleRate() {
        SampleRate sampleRate = SampleRate.RATE_44_1_KHZ;
        String selectedSampleRate = sampleRatesSpinner.getSelectedItem().toString();
        switch (selectedSampleRate) {
            case "48kHz":
                sampleRate = SampleRate.RATE_48_KHZ;
                break;
            case "96kHz":
                sampleRate = SampleRate.RATE_96_KHZ;
                break;
            case "192kHz":
                sampleRate = SampleRate.RATE_192_KHZ;
                break;
        }
        return sampleRate;
    }

    private void displaySoundParameters(@NonNull Sound sound) {
        int frequency = sound.getNumberOfTones();
        short duration = sound.getDuration();
        SampleRate sampleRate = sound.getSampleRate();

        String displayFrequency = frequency + getResources().getString(R.string.frequency_unit);
        String displayDuration = duration + getResources().getString(R.string.duration_unit);
        String displaySampleRate = sampleRatesSpinner.getSelectedItem().toString();
        String displayTotalPlaybackTime = String.valueOf(duration);
        int playbackBarMaxValue = sampleRate.sampleRate / 100;      // one step every 100 samples

        frequencyDetails.setText(displayFrequency);
        durationDetails.setText(displayDuration);
        sampleRateDetails.setText(displaySampleRate);
        playbackTotalTime.setText(displayTotalPlaybackTime);
        playbackBar.setMax(playbackBarMaxValue);
    }

    private void initializePlaybackManager() {
        sound = new SoundGenerator(getDuration(), getSampleRate()).generateSound(getTones());
        playbackManager = new PlaybackManager(this, new Handler(), playPauseBtn, playbackBar, playbackElapsedTime);
        playbackManager.setSound(sound);
        displaySoundParameters(sound);
    }

    private void initializeToneManagers() {
        toneManagers = new ToneManager[Config.TONES_NUMBER.value];
        for (int i = 0; i < toneManagers.length; ++i) {
            int toneIndex = i + 1;
            String ID = "tone_" + toneIndex;
            int resID = getResources().getIdentifier(ID, "id", getPackageName());
            View toneView = findViewById(resID);
            toneManagers[i] = new ToneManager(this, toneView, toneIndex);
        }
    }

    private void initializeDefaultLayout() {
        // Spinners
        ArrayAdapter<CharSequence> playbackModesAdapter = ArrayAdapter.createFromResource(this, R.array.playback_modes_array, android.R.layout.simple_spinner_item);
        playbackModesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playbackModesSpinner.setAdapter(playbackModesAdapter);

        ArrayAdapter<CharSequence> sampleRatesAdapter = ArrayAdapter.createFromResource(this, R.array.sample_rates_array, android.R.layout.simple_spinner_item);
        sampleRatesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sampleRatesSpinner.setAdapter(sampleRatesAdapter);

        // ProgressBars
        durationBar.setMax(UnitsConverter.convertDurationToSeekBarPosition(Config.DURATION_MAX.value));
        durationBar.setProgress(UnitsConverter.convertDurationToSeekBarPosition(Config.DURATION_DEFAULT.value));

        // TextViews
        durationTxt.setText(String.valueOf(Config.DURATION_DEFAULT.value));

        // Content visibility
        tone2Layout.setVisibility(View.GONE);
        //tone3Layout.setVisibility(View.GONE);
        //tone4Layout.setVisibility(View.GONE);
        loopIndicator.setVisibility(View.INVISIBLE);
    }

    private void initializeUIElements() {
        sampleRateLayout = findViewById(R.id.sample_rate_layout);
        durationLayout = findViewById(R.id.duration_layout);
        tone2ActivatorLayout = findViewById(R.id.tone_2_activator_layout);
        tone2Layout = findViewById(R.id.tone_2);
        tone2Activator = findViewById(R.id.tone_2_activator);
    /*    tone3ActivatorLayout = findViewById(R.id.tone_3_activator_layout);
        tone3Layout = findViewById(R.id.tone_3);
        tone3Activator = findViewById(R.id.tone_3_activator);
        tone4ActivatorLayout = findViewById(R.id.tone_4_activator_layout);
        tone4Layout = findViewById(R.id.tone_4);
        tone4Activator = findViewById(R.id.tone_4_activator);   */
        loadBtnLayout = findViewById(R.id.load_btn_layout);
        playbackBarLayout = findViewById(R.id.playback_bar_layout);
        durationBar = findViewById(R.id.duration_bar);
        durationTxt = findViewById(R.id.duration_input);
        durationDecrementBtn = findViewById(R.id.duration_decrement_btn);
        durationIncrementBtn = findViewById(R.id.duration_increment_btn);
        loadBtn = findViewById(R.id.load_btn);
        saveBtn = findViewById(R.id.save_btn);
        frequencyDetails = findViewById(R.id.tone_details_frequency);
        durationDetailsHeader = findViewById(R.id.tone_details_duration_txt);
        durationDetails = findViewById(R.id.tone_details_duration);
        sampleRateDetails = findViewById(R.id.tone_details_sample_rate);
        playbackBar = findViewById(R.id.playback_bar);
        playbackElapsedTime = findViewById(R.id.playback_elapsed_time);
        playbackTotalTime = findViewById(R.id.playback_total_time);
        playPauseBtn = findViewById(R.id.play_pause_btn);
        replayBtn = findViewById(R.id.replay_btn);
        loopBtn = findViewById(R.id.loop_btn);
        loopIndicator = findViewById(R.id.loop_indicator);
        playbackModesSpinner = findViewById(R.id.playback_modes_spinner);
        sampleRatesSpinner = findViewById(R.id.sample_rates_spinner);
        //extraBtn = findViewById(R.id.extra_btn);
    }

    private void initializeUIListeners() {
        durationTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int duration = Integer.parseInt(s.toString());
                    if (duration >= Config.DURATION_MIN.value && duration <= Config.DURATION_MAX.value)
                        durationBar.setProgress(UnitsConverter.convertDurationToSeekBarPosition(duration));
                    else if (duration > Config.DURATION_MAX.value)
                        durationTxt.setText(String.valueOf(Config.DURATION_MAX.value));
                    else
                        durationBar.setProgress(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        durationTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    validateToneDetails();
            }
        });

        durationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    durationTxt.setText(String.valueOf(UnitsConverter.convertSeekBarPositionToDuration(progress)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        durationDecrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int duration = Integer.parseInt(durationTxt.getText().toString());
                if (--duration >= Config.DURATION_MIN.value) {
                    durationBar.setProgress(UnitsConverter.convertDurationToSeekBarPosition(duration));
                    durationTxt.setText(String.valueOf(duration));
                }
            }
        });

        durationDecrementBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Options.buttonDecrementDurationState = Options.ButtonLongPressState.PRESSED;
                Handler handler = new Handler();
                SeekBarUpdater seekBarUpdater = new SeekBarUpdater(handler, durationTxt, Options.Operation.DURATION_DECREMENT);
                Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
                seekBarUpdaterThread.start();
                return true;
            }
        });

        durationDecrementBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    validateToneDetails();
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    Options.buttonDecrementDurationState = Options.ButtonLongPressState.RELEASED;
                }
                return false;
            }
        });

        durationIncrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int duration = Integer.parseInt(durationTxt.getText().toString());
                if (++duration <= Config.DURATION_MAX.value) {
                    durationBar.setProgress(UnitsConverter.convertDurationToSeekBarPosition(duration));
                    durationTxt.setText(String.valueOf(duration));
                }
            }
        });

        durationIncrementBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Options.buttonIncrementDurationState = Options.ButtonLongPressState.PRESSED;
                Handler handler = new Handler();
                SeekBarUpdater seekBarUpdater = new SeekBarUpdater(handler, durationTxt, Options.Operation.DURATION_INCREMENT);
                Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
                seekBarUpdaterThread.start();
                return true;
            }
        });

        durationIncrementBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    validateToneDetails();
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    Options.buttonIncrementDurationState = Options.ButtonLongPressState.RELEASED;
                }
                return false;
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

        tone2Activator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tone2ActivatorLayout.setBackgroundResource(0);
                    tone2Layout.setVisibility(View.VISIBLE);
                    return;
                }
                tone2ActivatorLayout.setBackgroundResource(R.color.tone_not_active);
                tone2Layout.setVisibility(View.GONE);
            }
        });
/*
        tone3Activator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tone3ActivatorLayout.setBackgroundResource(0);
                    tone3Layout.setVisibility(View.VISIBLE);
                    return;
                }
                tone3ActivatorLayout.setBackgroundResource(R.color.frequency_not_active);
                tone3Layout.setVisibility(View.GONE);
            }
        });

        tone4Activator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tone4ActivatorLayout.setBackgroundResource(0);
                    tone4Layout.setVisibility(View.VISIBLE);
                    return;
                }
                tone4ActivatorLayout.setBackgroundResource(R.color.frequency_not_active);
                tone4Layout.setVisibility(View.GONE);
            }
        });*/
/*
        extraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extra();
            }
        });
 */
    }
}