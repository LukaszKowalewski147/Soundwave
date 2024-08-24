package com.example.soundwave.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.soundwave.R;
import com.example.soundwave.additionalviews.OnToneClickListener;
import com.example.soundwave.additionalviews.ToneViewAdapter;
import com.example.soundwave.components.sound.Tone;
import com.example.soundwave.databinding.FragmentHomepageTonesBinding;
import com.example.soundwave.utils.Options;
import com.example.soundwave.viewmodel.HomepageTonesViewModel;

import java.util.ArrayList;

public class HomepageTonesFragment extends Fragment implements OnToneClickListener {
    private final String TAG = "HomepageTonesFragment";

    private HomepageTonesViewModel viewModel;
    private FragmentHomepageTonesBinding binding;
    private ToneViewAdapter toneViewAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomepageTonesBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(HomepageTonesViewModel.class);
        initializeLayout();
        initializeObservers();
        initializeListeners();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        viewModel.stopTonePlaying();
        binding = null;
        super.onDestroyView();
    }

    private void initializeLayout() {
        binding.tonesRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        toneViewAdapter = new ToneViewAdapter(getContext(), new ArrayList<>(), this);
        binding.tonesRecyclerview.setAdapter(toneViewAdapter);
    }

    private void initializeObservers() {
        viewModel.getAllTones().observe(getViewLifecycleOwner(), tones -> {
            if (tones != null) {
                toneViewAdapter.setToneItems(tones);
                if (tones.isEmpty())
                    showEmptyLayout();
            }
        });

        viewModel.getIsTonePlaying().observe(getViewLifecycleOwner(), aBoolean -> {
            if (!aBoolean)
                toneViewAdapter.notifyItemChanged(viewModel.getLastPlayedTonePosition());
            // Notify only on natural end of playback
        });
    }

    private void initializeListeners() {
        binding.tonesToneCreatorBtn.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.openToneCreator();
        });
    }

    private void showEmptyLayout() {
        binding.tonesEmptyLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRenameClick(Tone tone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.alert_dialog_homepage_rename_tone_message);

        final EditText toneNewName = new EditText(getContext());
        toneNewName.setInputType(InputType.TYPE_CLASS_TEXT);
        toneNewName.setText(tone.getName());

        builder.setView(toneNewName);

        builder.setPositiveButton(R.string.alert_dialog_homepage_rename_tone_positive, null);
        builder.setNegativeButton(R.string.alert_dialog_homepage_rename_tone_negative, null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String newName = toneNewName.getText().toString().trim();
            if (!newName.isEmpty()) {
                viewModel.renameTone(tone, newName).observe(getViewLifecycleOwner(), success -> {
                    if (success)
                        Toast.makeText(getContext(), R.string.alert_dialog_homepage_rename_tone_success, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getContext(), R.string.alert_dialog_homepage_rename_tone_fail, Toast.LENGTH_SHORT).show();
                });
                dialog.dismiss();
            } else {
                toneNewName.setError(getString(R.string.error_msg_empty_name));
            }
        }));

        dialog.show();
    }

    @Override
    public void onDeleteClick(Tone tone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.alert_dialog_homepage_delete_tone_message);
        builder.setPositiveButton(R.string.alert_dialog_homepage_delete_tone_positive, (dialog, id) -> viewModel.deleteTone(tone).observe(getViewLifecycleOwner(), success -> {
            if (success)
                Toast.makeText(getContext(), R.string.alert_dialog_homepage_delete_tone_success, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getContext(), R.string.alert_dialog_homepage_delete_tone_fail, Toast.LENGTH_SHORT).show();
        }));
        builder.setNegativeButton(R.string.alert_dialog_homepage_delete_tone_negative, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onDownloadClick(Tone tone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.alert_dialog_homepage_download_tone_message);

        builder.setPositiveButton(R.string.alert_dialog_homepage_download_tone_positive, (dialog, id) -> {
            if (viewModel.downloadTone(tone))
                Toast.makeText(getContext(), getString(R.string.alert_dialog_homepage_download_tone_success) + " " + Options.filepathToDownloadTones, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getContext(), R.string.alert_dialog_homepage_download_tone_fail, Toast.LENGTH_LONG).show();
        });

        builder.setNegativeButton(R.string.alert_dialog_homepage_download_tone_negative, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void playTone(Tone tone, int position) {
        boolean playingSuccessful = viewModel.playTone(tone, position);

        if (!playingSuccessful) {
            Log.e(TAG, "Play tone: playing unsuccessful");
            Toast.makeText(requireContext(), R.string.error_playing_tone, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void stopTonePlaying(boolean anyTone) {
        int lastPlayedTonePosition = viewModel.stopTonePlaying();
        if (anyTone)
            toneViewAdapter.notifyItemChanged(lastPlayedTonePosition);
    }

    @Override
    public boolean isTonePlaying(int position) {
        return viewModel.isTonePlaying(position);
    }

    @Override
    public boolean isAnyTonePlaying() {
        return viewModel.isAnyTonePlaying();
    }
}