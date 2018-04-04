package com.example.naimish.assignment.executor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.naimish.assignment.Constants.AppConstants;
import com.example.naimish.assignment.Model.Track;
import com.example.naimish.assignment.R;
import com.example.naimish.assignment.view.MainActivity;

import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;

import static com.example.naimish.assignment.Constants.AppConstants.ACTION_NEXT;
import static com.example.naimish.assignment.Constants.AppConstants.ACTION_PAUSE;
import static com.example.naimish.assignment.Constants.AppConstants.ACTION_PLAY;
import static com.example.naimish.assignment.Constants.AppConstants.ACTION_PREVIOUS;
import static com.example.naimish.assignment.Constants.AppConstants.ACTION_STOP;
import static com.example.naimish.assignment.Constants.AppConstants.CHANNEL_ID;
import static com.example.naimish.assignment.Constants.AppConstants.NOTIFICATION_ID;
import static com.example.naimish.assignment.Constants.AppConstants.PAUSED;
import static com.example.naimish.assignment.Constants.AppConstants.PLAYING;
import static com.example.naimish.assignment.Constants.AppConstants.STOPPED;

public class MediaService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    private IBinder mBind = new MusicBinder();
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    public MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private MediaPlayerListener mediaPlayerListener;
    private Track currentTrack;
    private int playerState;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        try {
            initMediaSession();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        playerState = STOPPED;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBind;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //notify Activity
        mp.stop();
        mp.reset();
        mediaPlayerListener.onNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);

        mediaPlayer.release();
        mediaSession.release();
        audioManager.abandonAudioFocus(this);

        super.onDestroy();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            if (mediaPlayer.isPlaying()) {
                togglePlay();
            }
        }
    }

    public void setTrack(Track track) {
        this.currentTrack=track;
        playerState = STOPPED;
    }

    public void togglePlay() {
        switch (playerState) {
            case STOPPED:
                if (requestAudioFocus()) {
                    playSong();
                    mediaPlayerListener.onStateChanged(playerState = PLAYING);
                    buildNotification(playerState);
                    Toast.makeText(this, "NOW PLAYING FRESH", Toast.LENGTH_SHORT).show();
                }
                break;
            case PAUSED:
                if (requestAudioFocus()) {
                    mediaPlayer.start();
                    mediaPlayerListener.onStateChanged(playerState = PLAYING);
                    buildNotification(playerState);
                    Toast.makeText(this, "RESUMED", Toast.LENGTH_SHORT).show();
                }
                break;
            case PLAYING:
                mediaPlayer.pause();
                mediaPlayerListener.onStateChanged(playerState = PAUSED);
                buildNotification(playerState);
                Toast.makeText(this, "PAUSE", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    private void playSong() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        try {
            mediaPlayer.setDataSource(currentTrack.getPreviewURL());
            updateMetaData();
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.d("Error","Error setting data source");
        }

    }

    public boolean requestAudioFocus() {
        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        }
        return false;
    }

    private void initMediaSession() throws RemoteException {
        //if (mediaSessionManager != null) return; //mediaSessionManager exists

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaSessionManager mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        }
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "Assignment");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                togglePlay();
                buildNotification(playerState);

                //Toast.makeText(MusicService.this, ""+playerState, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPause() {
                super.onPause();
                togglePlay();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                onCompletion(mediaPlayer);

            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayerListener.onPrev();
            }

            @Override
            public void onStop() {
                super.onStop();
                // removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    public void setMediaPlayerListener(MediaPlayerListener mediaPlayerListener) {
        this.mediaPlayerListener = mediaPlayerListener;
    }


    public class MusicBinder extends Binder {
        public MediaService getService() {
            return MediaService.this;
        }
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

    private void updateMetaData() {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                R.drawable.dummy);
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTrack.getName());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentTrack.getArtistName());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentTrack.getAlbumName());
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt);

        mediaSession.setMetadata(metadataBuilder.build());

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE);
        stateBuilder.setState(playerState, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f);
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private void buildNotification(int playerState) {
        int notificationAction = R.drawable.ic_notification_pause; //needs to be initialized

        PendingIntent play_pauseAction = null;
        boolean ongoing=false;

        if (playerState==PLAYING) {
            notificationAction = R.drawable.ic_notification_pause;
            ongoing=true;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playerState==PAUSED||playerState==STOPPED) {
            notificationAction = R.drawable.ic_notification_play;
            ongoing=false;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap albumArt = BitmapFactory.decodeResource(getResources(),R.drawable.dummy);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 2, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID);

        notificationBuilder
                .setStyle(new MediaStyle()
                        .setShowActionsInCompactView(0,1,2)
                        .setMediaSession(mediaSession.getSessionToken()))
                //.setDeleteIntent(mStopIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(albumArt)
                .setOngoing(ongoing)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(resultPendingIntent)
                .setContentText(currentTrack.getArtistName())
                .setContentTitle(currentTrack.getName())
                .setContentInfo(currentTrack.getAlbumName())
                .addAction(R.drawable.ic_notification_prev, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(R.drawable.ic_notification_next, "next", playbackAction(2));

        NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>26) {
            notificationBuilder.setChannelId(CHANNEL_ID);
            NotificationChannel androidChannel = new NotificationChannel(CHANNEL_ID,
                    "MediaStreaming", NotificationManager.IMPORTANCE_LOW);
            androidChannel.enableLights(true);
            androidChannel.setLightColor(Color.RED);
            androidChannel.setSound(null,null);
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mManager.createNotificationChannel(androidChannel);
        }
        mManager.notify(NOTIFICATION_ID, notificationBuilder.build());

    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }
}
