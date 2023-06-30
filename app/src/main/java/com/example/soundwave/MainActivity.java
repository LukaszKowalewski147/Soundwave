package com.example.soundwave;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ConstraintLayout sampleRateLayout;
    private ConstraintLayout durationLayout;
    private ConstraintLayout loadBtnLayout;
    private ConstraintLayout playbackBarLayout;
    private AppCompatButton frequencyDecrementBtn;
    private AppCompatButton frequencyIncrementBtn;
    private AppCompatButton durationDecrementBtn;
    private AppCompatButton durationIncrementBtn;
    private AppCompatButton loadBtn;
    private AppCompatButton saveBtn;
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
    private EditText frequencyTxt;
    private EditText durationTxt;
    private SeekBar frequencyBar;
    private SeekBar durationBar;
    private SeekBar playbackBar;
    private Spinner playbackModesSpinner;
    private Spinner sampleRatesSpinner;
    //private AppCompatButton extraBtn;

    private Tone tone;
    private PlaybackManager playbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tone = null;
        playbackManager = null;

        initializeUIElements();
        initializeUIListeners();
        initializePlaybackManager();
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

    private void loadTone() {
        int frequency = getFrequency();
        short duration = getDuration();
        SampleRate sampleRate = getSampleRate();
        tone = new SoundGenerator(frequency, duration, sampleRate).generateTone();
        playbackManager.setTone(tone);

        frequencyDetails.setText(frequency + "Hz");
        durationDetails.setText(duration + "s");
        sampleRateDetails.setText(sampleRatesSpinner.getSelectedItem().toString());
        playbackTotalTime.setText(String.valueOf(duration));
        playbackBar.setMax(sampleRate.sampleRate /100); // one step every 100 samples
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

    private void saveTone() {
        WavCreator wavCreator = new WavCreator(this, tone);
        wavCreator.saveTone();
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
        int frequency = 0;
        try {
            frequency = Integer.parseInt(frequencyTxt.getText().toString());
            if (frequency < Constants.FREQUENCY_MIN.value)
                frequencyTxt.setText(String.valueOf(Constants.FREQUENCY_MIN.value));
        } catch (Exception e) {
            frequencyTxt.setText(String.valueOf(UnitsConverter.convertSeekBarPositionToFrequency(Constants.FREQUENCY_PROGRESS_BAR_DEFAULT.value)));
        }
        short duration = 0;
        try {
            duration = Short.parseShort(durationTxt.getText().toString());
            if (duration < Constants.DURATION_MIN.value)
                durationTxt.setText(String.valueOf(Constants.DURATION_MIN.value));
        } catch (Exception e) {
            durationTxt.setText(String.valueOf(Constants.DURATION_DEFAULT.value));
        }
    }

    private void extra() {

        //Toast.makeText(this, "threads: " + Thread.activeCount(), Toast.LENGTH_SHORT).show();

        playbackManager.extra();
    }

    private int getFrequency() {
        return Integer.parseInt(frequencyTxt.getText().toString());
    }

    private short getDuration() {
        return Short.parseShort(durationTxt.getText().toString());
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

    private void initializePlaybackManager() {
        int frequency = getFrequency();
        short duration = getDuration();
        SampleRate sampleRate = getSampleRate();
        tone = new SoundGenerator(frequency, duration, sampleRate).generateTone();

        Handler handler = new Handler();
        playbackManager = new PlaybackManager(this, handler, playPauseBtn, playbackBar, playbackElapsedTime);
        playbackManager.setTone(tone);

        frequencyDetails.setText(frequency + "Hz");
        durationDetails.setText(duration + "s");
        playbackTotalTime.setText(String.valueOf(duration));
        playbackBar.setMax(sampleRate.sampleRate /100); // one step every 100 samples
    }

    private void initializeUIElements() {
        sampleRateLayout = findViewById(R.id.sample_rate_layout);
        durationLayout = findViewById(R.id.duration_layout);
        loadBtnLayout = findViewById(R.id.load_btn_layout);
        playbackBarLayout = findViewById(R.id.playback_bar_layout);
        frequencyBar = findViewById(R.id.frequency_bar);
        durationBar = findViewById(R.id.duration_bar);
        frequencyTxt = findViewById(R.id.frequency_txt);
        durationTxt = findViewById(R.id.duration_txt);
        frequencyDecrementBtn = findViewById(R.id.frequency_decrement_btn);
        frequencyIncrementBtn = findViewById(R.id.frequency_increment_btn);
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

        ArrayAdapter<CharSequence> playbackModesAdapter = ArrayAdapter.createFromResource(this, R.array.playback_modes_array, android.R.layout.simple_spinner_item);
        playbackModesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playbackModesSpinner.setAdapter(playbackModesAdapter);

        ArrayAdapter<CharSequence> sampleRatesAdapter = ArrayAdapter.createFromResource(this, R.array.sample_rates_array, android.R.layout.simple_spinner_item);
        sampleRatesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sampleRatesSpinner.setAdapter(sampleRatesAdapter);

        //extraBtn = findViewById(R.id.extra_btn);

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //    durationBar.setMin(Constants.DURATION_MIN.value);
        //}
        frequencyBar.setMax(Constants.FREQUENCY_PROGRESS_BAR_MAX.value);
        durationBar.setMax(UnitsConverter.convertDurationToSeekBarPosition(Constants.DURATION_MAX.value));

        frequencyBar.setProgress(Constants.FREQUENCY_PROGRESS_BAR_DEFAULT.value);
        durationBar.setProgress(UnitsConverter.convertDurationToSeekBarPosition(Constants.DURATION_DEFAULT.value));

        frequencyTxt.setText(String.valueOf(UnitsConverter.convertSeekBarPositionToFrequency(Constants.FREQUENCY_PROGRESS_BAR_DEFAULT.value)));
        durationTxt.setText(String.valueOf(Constants.DURATION_DEFAULT.value));

        loopIndicator.setVisibility(View.INVISIBLE);
    }

    private void initializeUIListeners() {
        frequencyTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int frequency = Integer.parseInt(s.toString());
                    if (frequency >= Constants.FREQUENCY_MIN.value && frequency <= Constants.FREQUENCY_MAX.value)
                        frequencyBar.setProgress(UnitsConverter.convertFrequencyToSeekBarPosition(frequency));
                    else if (frequency > Constants.FREQUENCY_MAX.value)
                        frequencyTxt.setText(String.valueOf(Constants.FREQUENCY_MAX.value));
                    else
                        frequencyBar.setProgress(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        frequencyTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    validateToneDetails();
            }
        });

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
                    if (duration >= Constants.DURATION_MIN.value && duration <= Constants.DURATION_MAX.value)
                        durationBar.setProgress(UnitsConverter.convertDurationToSeekBarPosition(duration));
                    else if (duration > Constants.DURATION_MAX.value)
                        durationTxt.setText(String.valueOf(Constants.DURATION_MAX.value));
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

        frequencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    frequencyTxt.setText(String.valueOf(UnitsConverter.convertSeekBarPositionToFrequency(progress)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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

        frequencyDecrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int frequency = Integer.parseInt(frequencyTxt.getText().toString());
                if (--frequency >= Constants.FREQUENCY_MIN.value) {
                    frequencyBar.setProgress(UnitsConverter.convertFrequencyToSeekBarPosition(frequency));
                    frequencyTxt.setText(String.valueOf(frequency));
                }
            }
        });

        frequencyDecrementBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Options.buttonDecrementFrequencyState = Options.ButtonLongPressState.PRESSED;
                Handler handler = new Handler();
                SeekBarUpdater seekBarUpdater = new SeekBarUpdater(handler, frequencyTxt, Options.Operation.FREQUENCY_DECREMENT);
                Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
                seekBarUpdaterThread.start();
                return true;
            }
        });

        frequencyDecrementBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    validateToneDetails();
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    Options.buttonDecrementFrequencyState = Options.ButtonLongPressState.RELEASED;
                }
                return false;
            }
        });

        frequencyIncrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int frequency = Integer.parseInt(frequencyTxt.getText().toString());
                if (++frequency <= Constants.FREQUENCY_MAX.value) {
                    frequencyBar.setProgress(UnitsConverter.convertFrequencyToSeekBarPosition(frequency));
                    frequencyTxt.setText(String.valueOf(frequency));
                }
            }
        });

        frequencyIncrementBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Options.buttonIncrementFrequencyState = Options.ButtonLongPressState.PRESSED;
                Handler handler = new Handler();
                SeekBarUpdater seekBarUpdater = new SeekBarUpdater(handler, frequencyTxt, Options.Operation.FREQUENCY_INCREMENT);
                Thread seekBarUpdaterThread = new Thread(seekBarUpdater);
                seekBarUpdaterThread.start();
                return true;
            }
        });

        frequencyIncrementBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    validateToneDetails();
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    Options.buttonIncrementFrequencyState = Options.ButtonLongPressState.RELEASED;
                }
                return false;
            }
        });

        durationDecrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int duration = Integer.parseInt(durationTxt.getText().toString());
                if (--duration >= Constants.DURATION_MIN.value) {
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
                if (++duration <= Constants.DURATION_MAX.value) {
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
                loadTone();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTone();
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