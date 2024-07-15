package com.example.soundwave.utils;

import java.util.List;

public class TrackData {
    private final List<TrackToneData> children;

    public TrackData(List<TrackToneData> children) {
        this.children = children;
    }

    public int getChildCount() {
        return children.size();
    }

    public List<TrackToneData> getChildren() {
        return children;
    }

    public TrackToneData getChildAt(int index) {
        return children.get(index);
    }
}
