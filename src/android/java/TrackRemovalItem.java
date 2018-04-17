package com.rolamix.plugins.audioplayer;

public class TrackRemovalItem {
    public int trackIndex = -1;
    public String trackId = "";

    TrackRemovalItem(int index, String id) {
        trackIndex = index;
        trackId = id;
    }
}

