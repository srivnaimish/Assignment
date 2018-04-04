package com.example.naimish.assignment.executor;

import com.example.naimish.assignment.Model.Track;

public interface MediaPlayerListener {

    public void onStateChanged(int status);
    public void onNext();
    public void onPrev();
}
