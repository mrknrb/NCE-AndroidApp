package com.example.android.trackmysleepquality;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.Locale;

import static com.example.android.trackmysleepquality.App.CHANNEL_ID;

public class PlayerService extends Service {
    public static final String TAG = "MPS";
    private MediaSessionCompat mediaSession;
    TextToSpeech myTts;

    private final MediaSessionCompat.Callback mMediaSessionCallback
            = new MediaSessionCompat.Callback() {
        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            final String intentAction = mediaButtonEvent.getAction();
            if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                final KeyEvent event = mediaButtonEvent.getParcelableExtra(
                        Intent.EXTRA_KEY_EVENT);
                if (event == null) {
                    return super.onMediaButtonEvent(mediaButtonEvent);
                }
                final int keycode = event.getKeyCode();
                final int action = event.getAction();
                if (event.getRepeatCount() == 0 && action == KeyEvent.ACTION_DOWN) {

                    switch (keycode) {
                        // Do what you want in here
                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                            Toast.makeText(getApplicationContext(), "play", Toast.LENGTH_SHORT).show();


                            String toSpeak = "This is a sample text that should play with Android default TTS engine.";
                            Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
/*
                                // Stupid Android 8 "Oreo" hack to make media buttons work
                                final MediaPlayer mMediaPlayer;
                                mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.silent_sound);
                                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mediaPlayer) {
                                        mMediaPlayer.release();
                                    }
                                });
                                mMediaPlayer.start();
*/
                            myTts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);


                            break;
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                            Toast.makeText(getApplicationContext(), "pause", Toast.LENGTH_SHORT).show();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                            Toast.makeText(getApplicationContext(), "next", Toast.LENGTH_SHORT).show();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            Toast.makeText(getApplicationContext(), "previous", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    startService(new Intent(getApplicationContext(), PlayerService.class));
                    return true;
                }
            }
            return false;
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();

        myTts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    myTts.setLanguage(Locale.US);
                }
            }
        });


        ComponentName receiver = new ComponentName(getPackageName(), RemoteReceiver.class.getName());
        mediaSession = new MediaSessionCompat(this, "PlayerService", receiver, null);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .build());
        mediaSession.setCallback(mMediaSessionCallback);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                // Ignore
               // MainActivity.showText("focusChange=" + focusChange);
            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        Toast.makeText(getApplicationContext(), "elindult", Toast.LENGTH_SHORT).show();

        mediaSession.setActive(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0.0f)
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).build());
        } else {
            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).build());
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ecalogo);
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("Text to Speech")
                .setSmallIcon(R.drawable.ic_launcher_sleep_tracker_foreground)
                //.setContentText(input)
                .setColor(Color.RED)
                .setLargeIcon(largeIcon)
                .addAction(R.drawable.ic_sleep_0, "Dislike", null)
                .addAction(R.drawable.ic_sleep_0, "Previous", null)
                .addAction(R.drawable.ic_sleep_0, "Pause", null)
                .addAction(R.drawable.ic_sleep_0, "Next", null)
                .addAction(R.drawable.ic_sleep_1, "Like", null)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(2, 3, 4)
                )
                .setSubText("Sub Text")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);

        // lehet, hogy nem kell, de ez a néma hangos trükk
       final MediaPlayer mMediaPlayer;
        mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.silent_sound);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override            public void onCompletion(MediaPlayer mediaPlayer) {
                mMediaPlayer.release();            }        });        mMediaPlayer.start();


        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "vege", Toast.LENGTH_SHORT).show();

        mediaSession.release();
    }
}