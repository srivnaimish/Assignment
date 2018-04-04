package com.example.naimish.assignment.executor;

import com.example.naimish.assignment.Model.Track;

import java.util.List;

public interface APIListener {
    public void onApiResultsFetch(List<Track> tracks);
}
