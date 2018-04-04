package com.example.naimish.assignment.executor;

import android.content.Context;
import android.widget.Toast;

import com.example.naimish.assignment.view.MainActivity;

public class PlayMediaExec {
    private Context ctx;

    public PlayMediaExec(Context context) {
        this.ctx = context;
    }

    public void startPlaying(int position,boolean setNewTrack) {
        MediaService mediaService = ((MainActivity) ctx).getMediaService();
        if(setNewTrack)
            mediaService.setTrack(((MainActivity) ctx).getTracks().get(position));
        mediaService.togglePlay();
    }


}
