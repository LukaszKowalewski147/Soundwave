package com.example.soundwave.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.soundwave.databinding.FragmentHomepageBinding;
import com.example.soundwave.model.entity.Tone;
import com.example.soundwave.recyclerviews.ToneViewAdapter;
import com.example.soundwave.viewmodel.HomepageViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomepageFragment extends Fragment {

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
        toneViewAdapter = new ToneViewAdapter(getContext(), new ArrayList<>());
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
    }
}