package com.example.soundwave.utils;

import com.example.soundwave.Overtone;

import java.util.ArrayList;
import java.util.List;

public class OvertonesAdapter {
    public static List<Overtone> convertDbStringToOvertones(String overtones) {
        List<Overtone> overtonesList = new ArrayList<>();
        StringBuilder search = new StringBuilder().append(overtones);

        while(overtones.contains("o{")) {

        }

        return overtonesList;
    }
}
