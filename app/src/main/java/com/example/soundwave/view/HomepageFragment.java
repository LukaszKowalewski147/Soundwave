package com.example.soundwave.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.soundwave.R;
import com.example.soundwave.databinding.FragmentHomepageBinding;
import com.example.soundwave.Tone;
import com.example.soundwave.recyclerviews.OnToneClickListener;
import com.example.soundwave.recyclerviews.ToneViewAdapter;
import com.example.soundwave.utils.Options;
import com.example.soundwave.viewmodel.HomepageViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomepageFragment extends Fragment implements OnToneClickListener {

    private HomepageViewModel viewModel;
    private FragmentHomepageBinding binding;
    private ToneViewAdapter toneViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomepageBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(HomepageViewModel.class);
        initializeLayout();
        initializeObservers();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        viewModel.stopTonePlaying();
        super.onDestroyView();
        binding = null;
    }

    private void initializeLayout() {
        binding.tonesRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        toneViewAdapter = new ToneViewAdapter(getContext(), new ArrayList<>(), this);
        binding.tonesRecyclerview.setAdapter(toneViewAdapter);
    }

    private void initializeObservers() {
        viewModel.getAllTones().observe(getViewLifecycleOwner(), new Observer<List<Tone>>() {
            @Override
            public void onChanged(List<Tone> tones) {
                if (tones != null) {
                    toneViewAdapter.setToneItems(tones);
                }
            }
        });

        viewModel.getIsTonePlaying().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (!aBoolean)
                    toneViewAdapter.notifyItemChanged(viewModel.getLastPlayedTonePosition());
                // Notify only on natural end of playback
            }
        });
    }

    @Override
    public void onRenameClick(Tone tone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.alert_dialog_homepage_rename_message);

        final EditText toneNewName = new EditText(getContext());
        toneNewName.setInputType(InputType.TYPE_CLASS_TEXT);
        toneNewName.setText(tone.getName());

        builder.setView(toneNewName);

        builder.setPositiveButton(R.string.alert_dialog_homepage_rename_positive, null);
        builder.setNegativeButton(R.string.alert_dialog_homepage_rename_negative, new DialogInterface.OnClickListener() {
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
                        String newName = toneNewName.getText().toString().trim();
                        if (!newName.isEmpty()) {
                            viewModel.renameTone(tone, newName).observe(getViewLifecycleOwner(), success -> {
                                if (success)
                                    Toast.makeText(getContext(), R.string.alert_dialog_homepage_rename_success, Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getContext(), R.string.alert_dialog_homepage_rename_fail, Toast.LENGTH_SHORT).show();
                            });
                            dialog.dismiss();
                        } else {
                            toneNewName.setError(getString(R.string.error_msg_empty_name));
                        }
                    }
                });
            }
        });

        dialog.show();
    }

    @Override
    public void onDeleteClick(Tone tone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.alert_dialog_homepage_delete_message);
        builder.setPositiveButton(R.string.alert_dialog_homepage_delete_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                viewModel.deleteTone(tone).observe(getViewLifecycleOwner(), success -> {
                    if (success)
                        Toast.makeText(getContext(), R.string.alert_dialog_homepage_delete_success, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getContext(), R.string.alert_dialog_homepage_delete_fail, Toast.LENGTH_SHORT).show();
                });
            }
        });
        builder.setNegativeButton(R.string.alert_dialog_homepage_delete_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onDownloadClick(Tone tone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.alert_dialog_homepage_download_message);

        builder.setPositiveButton(R.string.alert_dialog_homepage_download_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (viewModel.downloadTone(tone))
                    Toast.makeText(getContext(), getString(R.string.alert_dialog_homepage_download_success) + " " + Options.filepathToDownload, Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getContext(), R.string.alert_dialog_homepage_download_fail, Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton(R.string.alert_dialog_homepage_download_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void playTone(Tone tone, int position) {
        viewModel.playTone(tone, position);
    }

    @Override
    public void stopTonePlaying(boolean anyTone) {
        int lastPlayedTonePosition = viewModel.stopTonePlaying();
        if (anyTone)
            toneViewAdapter.notifyItemChanged(lastPlayedTonePosition);
    }

    @Override
    public boolean isAnyTonePlaying() {
        return viewModel.isAnyTonePlaying();
    }

    @Override
    public boolean isTonePlaying(int position) {
        return viewModel.isTonePlaying(position);
    }
}