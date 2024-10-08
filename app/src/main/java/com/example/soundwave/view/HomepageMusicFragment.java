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
import com.example.soundwave.viewadapter.MusicViewAdapter;
import com.example.soundwave.viewadapter.OnMusicClickListener;
import com.example.soundwave.components.sound.Music;
import com.example.soundwave.databinding.FragmentHomepageMusicBinding;
import com.example.soundwave.utils.Options;
import com.example.soundwave.viewmodel.HomepageMusicViewModel;

import java.util.ArrayList;

public class HomepageMusicFragment extends Fragment implements OnMusicClickListener {
    private final String TAG = "HomepageMusicFragment";

    private HomepageMusicViewModel viewModel;
    private FragmentHomepageMusicBinding binding;
    private MusicViewAdapter musicViewAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomepageMusicBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(HomepageMusicViewModel.class);
        initializeLayout();
        initializeObservers();
        initializeListeners();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        viewModel.stopMusicPlaying();
        binding = null;
        super.onDestroyView();
    }

    private void initializeLayout() {
        binding.musicRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        musicViewAdapter = new MusicViewAdapter(getContext(), new ArrayList<>(), this);
        binding.musicRecyclerview.setAdapter(musicViewAdapter);
    }

    private void initializeObservers() {
        viewModel.getAllMusic().observe(getViewLifecycleOwner(), musicList -> {
            if (musicList != null) {
                musicViewAdapter.setMusicItems(musicList);
                if (musicList.isEmpty())
                    showEmptyLayout();
            }
        });

        viewModel.getIsMusicPlaying().observe(getViewLifecycleOwner(), aBoolean -> {
            if (!aBoolean)
                musicViewAdapter.notifyItemChanged(viewModel.getLastPlayedMusicPosition());
            // Notify only on natural end of playback
        });
    }

    private void initializeListeners() {
        binding.musicTonesMixerBtn.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.openToneMixer();
        });
    }

    private void showEmptyLayout() {
        binding.musicEmptyLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRenameClick(Music music) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.alert_dialog_homepage_rename_music_message);

        final EditText musicNewName = new EditText(getContext());
        musicNewName.setInputType(InputType.TYPE_CLASS_TEXT);
        musicNewName.setText(music.getName());

        builder.setView(musicNewName);

        builder.setPositiveButton(R.string.alert_dialog_homepage_rename_music_positive, null);
        builder.setNegativeButton(R.string.alert_dialog_homepage_rename_music_negative, null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String newName = musicNewName.getText().toString().trim();
            if (!newName.isEmpty()) {
                viewModel.renameMusic(music, newName).observe(getViewLifecycleOwner(), success -> {
                    if (success)
                        Toast.makeText(getContext(), R.string.alert_dialog_homepage_rename_music_success, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getContext(), R.string.alert_dialog_homepage_rename_music_fail, Toast.LENGTH_SHORT).show();
                });
                dialog.dismiss();
            } else {
                musicNewName.setError(getString(R.string.error_msg_empty_name));
            }
        }));

        dialog.show();
    }

    @Override
    public void onDeleteClick(Music music) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.alert_dialog_homepage_delete_music_message);
        builder.setPositiveButton(R.string.alert_dialog_homepage_delete_music_positive, (dialog, id) -> viewModel.deleteMusic(music).observe(getViewLifecycleOwner(), success -> {
            if (success)
                Toast.makeText(getContext(), R.string.alert_dialog_homepage_delete_music_success, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getContext(), R.string.alert_dialog_homepage_delete_music_fail, Toast.LENGTH_SHORT).show();
        }));
        builder.setNegativeButton(R.string.alert_dialog_homepage_delete_music_negative, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onDownloadClick(Music music) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.alert_dialog_homepage_download_music_message);

        builder.setPositiveButton(R.string.alert_dialog_homepage_download_music_positive, (dialog, id) -> {
            if (viewModel.downloadMusic(music))
                Toast.makeText(getContext(), getString(R.string.alert_dialog_homepage_download_music_success) + " " + Options.filepathToDownloadMusic, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getContext(), R.string.alert_dialog_homepage_download_music_fail, Toast.LENGTH_LONG).show();
        });

        builder.setNegativeButton(R.string.alert_dialog_homepage_download_music_negative, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void playMusic(Music music, int position) {
        boolean playingSuccessful = viewModel.playMusic(music, position);

        if (!playingSuccessful) {
            Log.e(TAG, "Play music: playing unsuccessful");
            Toast.makeText(requireContext(), R.string.error_playing_music, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void stopMusicPlaying(boolean anyMusic) {
        int lastPlayedMusicPosition = viewModel.stopMusicPlaying();
        if (anyMusic)
            musicViewAdapter.notifyItemChanged(lastPlayedMusicPosition);
    }

    @Override
    public boolean isMusicPlaying(int position) {
        return viewModel.isMusicPlaying(position);
    }

    @Override
    public boolean isAnyMusicPlaying() {
        return viewModel.isAnyMusicPlaying();
    }
}
