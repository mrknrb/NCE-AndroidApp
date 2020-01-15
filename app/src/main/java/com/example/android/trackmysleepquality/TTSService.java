package com.example.android.trackmysleepquality;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import java.io.IOException;
import java.util.List;


public class TTSService extends Service {
    public static final String TAG = "MPS";
    private MediaSessionCompat mediaSession;

  //  private MediaPlayer mMediaPlayer;
   // private MediaSessionCompat mMediaSessionCompat;


    private final MediaSessionCompat.Callback mMediaSessionCallback
            = new MediaSessionCompat.Callback() {

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            final String intentAction = mediaButtonEvent.getAction();
            // Toast.makeText(getApplicationContext(), mediaButtonEvent.getAction()., Toast.LENGTH_SHORT).show();

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
                           // MainActivity.showText("KEYCODE_MEDIA_PLAY");

                            Toast.makeText(getApplicationContext(), "play", Toast.LENGTH_SHORT).show();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                           // MainActivity.showText("KEYCODE_MEDIA_Pause");

                            Toast.makeText(getApplicationContext(), "pause", Toast.LENGTH_SHORT).show();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                           // MainActivity.showText("KEYCODE_MEDIA_NEXT");
                            Toast.makeText(getApplicationContext(), "next", Toast.LENGTH_SHORT).show();

                            break;
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            //MainActivity.showText("KEYCODE_MEDIA_PREVIOUS");
                            Toast.makeText(getApplicationContext(), "previous", Toast.LENGTH_SHORT).show();

                            break;
                    }
                    startService(new Intent(getApplicationContext(), TTSService.class));
                    return true;
                }
            }
            return false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        ComponentName receiver = new ComponentName(getPackageName(), RemoteReceiver.class.getName());
        mediaSession = new MediaSessionCompat(this, "PlayerService", receiver, null);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //val toSpeak = "This is a sample text that should play with Android default TTS engine."
       // Toast.makeText(applicationContext, toSpeak, Toast.LENGTH_SHORT).show()

        // Stupid Android 8 "Oreo" hack to make media buttons work
        final    MediaPlayer mMediaPlayer;
               mMediaPlayer = MediaPlayer.create(this, R.raw.silent_sound);

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mMediaPlayer.release();
            }
        });

    //{  };
        mMediaPlayer.start();

       // myTts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null)


        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                .build());
//        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
//                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Test Artist")
//                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Test Album")
//                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Test Track Name")
//                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 10000)
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
//                        BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
//                .build());
        mediaSession.setCallback(mMediaSessionCallback);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                // Ignore
                //MainActivity.showText("focusChange=" + focusChange);
            }
        }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        mediaSession.setActive(true);





        /*
        super.onCreate();
        initMediaSession();
        mMediaSessionCompat.setActive(true);
        // Toast.makeText(getApplicationContext(),"stop tracking", Toast.LENGTH_SHORT).show();
        System.out.println("mrknrb" + mMediaSessionCompat.isActive());
*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
            //MainActivity.showText("mediaSession set PAUSED state");
            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0.0f)
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).build());
        } else {
            //MainActivity.showText("mediaSession set PLAYING state");
            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f)
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).build());
        }


        mediaSession.setActive(true);
/*
        String input = intent.getStringExtra("EXTRA_MESSAGE");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ecalogo);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Text to Speech")
                .setSmallIcon(R.drawable.ic_launcher_sleep_tracker_foreground)
                .setContentText(input)
                .setColor(Color.RED)
                .setLargeIcon(largeIcon)

                .addAction(R.drawable.ic_sleep_0, "Dislike", null)
                .addAction(R.drawable.ic_sleep_0, "Previous", null)
                .addAction(R.drawable.ic_sleep_0, "Pause", null)
                .addAction(R.drawable.ic_sleep_0, "Next", null)
                .addAction(R.drawable.ic_sleep_1, "Like", null)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(1, 2, 3)
                        //.setMediaSession(mMediaSessionCompat.getSessionToken())   ha pirosra akarod alakítani
                )
                .setSubText("Sub Text")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        mediaSession.setActive(true);*/
        //do heavy work on a background thread
        //stopSelf();
        return START_NOT_STICKY;


    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.release();
    }

}