package com.example.naimish.assignment.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.naimish.assignment.Model.Track;
import com.example.naimish.assignment.R;
import com.example.naimish.assignment.databinding.ActivityMainBinding;
import com.example.naimish.assignment.executor.APIListener;
import com.example.naimish.assignment.executor.ApiManager;
import com.example.naimish.assignment.executor.MediaPlayerListener;
import com.example.naimish.assignment.executor.MediaService;
import com.example.naimish.assignment.executor.PlayMediaExec;
import com.example.naimish.assignment.executor.RecyclerViewAdapter;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.example.naimish.assignment.Constants.AppConstants.PAUSED;
import static com.example.naimish.assignment.Constants.AppConstants.PLAYING;

public class MainActivity extends AppCompatActivity implements APIListener, MediaPlayerListener {

    private ApiManager apiManager;
    private ActivityMainBinding mBinding;
    private RecyclerViewAdapter recyclerViewAdapter;
    private MediaService mediaService;
    private Intent playIntent;
    private int currentTrackPosition = 0;
    private List<Track> tracks;
    private PlayMediaExec playMediaExec;

    private ServiceConnection mediaConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MediaService.MusicBinder binder = (MediaService.MusicBinder) service;
            mediaService = binder.getService();
            mediaService.setMediaPlayerListener(MainActivity.this);
            mediaService.setTrack(tracks.get(currentTrackPosition));
            mBinding.trackName.setText(tracks.get(currentTrackPosition).getName());
            mBinding.artistName.setText(tracks.get(currentTrackPosition).getArtistName());
            mBinding.playCard.setAlpha(0.f);
            mBinding.playCard.animate()
                    .alpha(1.f)
                    .setDuration(100)
                    .start();
            mBinding.playCard.setVisibility(View.VISIBLE);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinding.playPause.setImageResource(R.drawable.ic_play);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerViewAdapter = new RecyclerViewAdapter(this);
        mBinding.recyclerView.setAdapter(recyclerViewAdapter);

        apiManager = new ApiManager(this);
        apiManager.fetchTrackList();    //startfetch
        playMediaExec = new PlayMediaExec(this);
    }

    @Override
    public void onApiResultsFetch(List<Track> tracks) {
        this.tracks = tracks;
        recyclerViewAdapter.addItems(tracks);
        mBinding.progressBar.setVisibility(View.GONE);
        mBinding.recyclerView.scheduleLayoutAnimation();
        startTheService();  //startBackgroundService
    }

    @Override
    public void onStateChanged(int status) {
        switch (status) {
            case PLAYING:
                mBinding.playPause.setImageResource(R.drawable.ic_pause);
                tracks.get(currentTrackPosition).setPlaying(true);
                recyclerViewAdapter.notifyItemChanged(currentTrackPosition);
                break;
            case PAUSED:
                mBinding.playPause.setImageResource(R.drawable.ic_play_arrow);
                tracks.get(currentTrackPosition).setPlaying(false);
                recyclerViewAdapter.notifyItemChanged(currentTrackPosition);
                break;
        }
    }

    @Override
    public void onNext() {
        updateCurrentPosition(currentTrackPosition+1);
    }

    @Override
    public void onPrev() {
        updateCurrentPosition(currentTrackPosition-1);
    }

    public void startTheService() {
        if (playIntent == null) {
            playIntent = new Intent(this, MediaService.class);
            startService(playIntent);
            Log.d("Bound", bindService(playIntent, mediaConnection, Context.BIND_AUTO_CREATE) + "");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mediaConnection);
        stopService(playIntent);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play_pause:
                mediaService.togglePlay();
                break;
        }
    }

    public MediaService getMediaService() {
        return mediaService;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void updateCurrentPosition(int position) {
        if (position >= 0 && position < tracks.size()) {
            Toast.makeText(this, "Playing "+position, Toast.LENGTH_SHORT).show();

            tracks.get(currentTrackPosition).setPlaying(false);
            tracks.get(position).setPlaying(true);

            recyclerViewAdapter.notifyItemChanged(currentTrackPosition);
            recyclerViewAdapter.notifyItemChanged(position);

            currentTrackPosition = position;
            playMediaExec.startPlaying(currentTrackPosition, true);
            mBinding.trackName.setText(tracks.get(currentTrackPosition).getName());
            mBinding.artistName.setText(tracks.get(currentTrackPosition).getArtistName());

        }
    }

}
