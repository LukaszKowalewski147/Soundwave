package com.example.soundwave;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Build;
import android.os.Bundle;
import android.provider.Contacts;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {

    private SeekBar frequencyBar;
    private SeekBar durationBar;
    private EditText frequencyTxt;
    private EditText durationTxt;
    private AppCompatButton frequencyApplyBtn;
    private AppCompatButton durationApplyBtn;
    private AppCompatButton startBtn;
    private AppCompatButton stopBtn;
    private AppCompatButton saveBtn;

    private SoundGenerator soundGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soundGenerator = null;

        initializeUIElements();
        initializeUIListeners();
    }

    private void startPlaying() {
        stopPlaying();
        int frequency = Integer.parseInt(frequencyTxt.getText().toString());
        int duration = durationBar.getProgress();
        soundGenerator = new SoundGenerator(frequency, duration);
        soundGenerator.play();
    }

    private void stopPlaying() {
        if (soundGenerator != null)
            soundGenerator.stop();
        soundGenerator = null;
    }

    private void saveTone() {
        //TODO: save tone
    }

    private int convertFromSlider(int sliderValue) {
        int output = 0;
        double fractionOutput = 0.0d;
        double toEightPower = 0.0d;

        sliderValue += 500;
        toEightPower = sliderValue/500.0d;
        fractionOutput = Math.pow(toEightPower, 10);
        output = (int) Math.round(fractionOutput); // 1 is the lowest possible
        output += 19; // thats why + 19 to match 20Hz minimum

        if (output > Constants.FREQ_MAX.value)
            output = Constants.FREQ_MAX.value;

        return output;
    }

    private int convertToSlider(int input) {
        int output = 0;
        double fractionOutput = 0.0d;

        double toEightPower = input/20.0d;
        fractionOutput = Math.pow(toEightPower, 8);
        output = (int) Math.round(fractionOutput);





        return output;
    }

    private void initializeUIElements() {
        frequencyBar = findViewById(R.id.frequency_bar);
        durationBar = findViewById(R.id.duration_bar);
        frequencyTxt = findViewById(R.id.frequency_txt);
        durationTxt = findViewById(R.id.duration_txt);
        frequencyApplyBtn = findViewById(R.id.frequency_apply_btn);
        durationApplyBtn = findViewById(R.id.duration_apply_btn);
        startBtn = findViewById(R.id.on_btn);
        stopBtn = findViewById(R.id.off_btn);
        saveBtn = findViewById(R.id.save_btn);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            frequencyBar.setMin(0);
            durationBar.setMin(Constants.DURATION_MIN.value);
        }
        frequencyBar.setMax(850);
        durationBar.setMax(Constants.DURATION_MAX.value);

        frequencyBar.setProgress(425);
        durationBar.setProgress(Constants.DURATION_START.value);

        //frequencyTxt.setText(String.valueOf(Constants.FREQUENCY_START.value));
        frequencyTxt.setText(String.valueOf(convertFromSlider(425)));
        durationTxt.setText(String.valueOf(Constants.DURATION_START.value));
    }

    private void initializeUIListeners() {
        frequencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                frequencyTxt.setText(String.valueOf(convertFromSlider(progress)));
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
                durationTxt.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        frequencyApplyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int frequency = Integer.parseInt(frequencyTxt.getText().toString());

                if (frequency >= Constants.FREQ_MIN.value && frequency <= Constants.FREQ_MAX.value)
                    frequencyBar.setProgress(frequency);
                else if (frequency > Constants.FREQ_MAX.value)
                    frequencyBar.setProgress(Constants.FREQ_MAX.value);
                else
                    frequencyBar.setProgress(Constants.FREQ_MIN.value);
            }
        });

        durationApplyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int duration = Integer.parseInt(durationTxt.getText().toString());

                if (duration >= Constants.DURATION_MIN.value && duration <= Constants.DURATION_MAX.value)
                    durationBar.setProgress(duration);
                else if (duration > Constants.DURATION_MAX.value)
                    durationBar.setProgress(Constants.DURATION_MAX.value);
                else
                    durationBar.setProgress(Constants.DURATION_MIN.value);
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlaying();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlaying();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTone();
            }
        });
    }
}