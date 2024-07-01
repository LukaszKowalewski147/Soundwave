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
import com.example.soundwave.model.entity.Tone;
import com.example.soundwave.recyclerviews.OnToneClickListener;
import com.example.soundwave.recyclerviews.ToneViewAdapter;
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
                toneViewAdapter.notifyDataSetChanged();
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

        builder.setPositiveButton(R.string.alert_dialog_homepage_rename_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                viewModel.renameTone(tone, toneNewName.getText().toString()).observe(getViewLifecycleOwner(), success -> {
                    if (success)
                        Toast.makeText(getContext(), R.string.alert_dialog_homepage_rename_success, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getContext(), R.string.alert_dialog_homepage_rename_fail, Toast.LENGTH_SHORT).show();
                });
            }
        });
        builder.setNegativeButton(R.string.alert_dialog_homepage_rename_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog dialog = builder.create();
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
    public void onEditClick(Tone tone) {

    }

    @Override
    public void onPlayStopClick(Tone tone) {
        viewModel.playStopTone(tone);
    }

    @Override
    public boolean isPlaying(Tone tone) {
        return viewModel.isTonePlaying(tone);
    }
}