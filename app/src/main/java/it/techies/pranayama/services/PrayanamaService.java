package it.techies.pranayama.services;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class PrayanamaService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener {

    private static final String TAG = "PrayanamaService";

    /**
     * Binder given to clients.
     */
    private final IBinder mBinder = new LocalBinder();

    /**
     * MediaPlayer instance.
     */
    private MediaPlayer mPlayer;

    /**
     * To keep track of which music is being played in media player.
     * Yoga music or meditation bell.
     */
    private boolean isPlayingYogaMusic = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG, "onStartCommand() called with: " + "intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {

        public PrayanamaService getService()
        {
            // Return this instance of LocalService so clients can call public methods
            return PrayanamaService.this;
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        playYogaMusic();
    }

    /**
     * Initialize media player and setup listeners.
     */
    private void initPlayer()
    {
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setScreenOnWhilePlaying(true);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnBufferingUpdateListener(this);
        mPlayer.setScreenOnWhilePlaying(true);
    }

    /**
     * Start playing yoga music file in media player.
     */
    public void playYogaMusic()
    {
        if (mPlayer == null)
        {
            initPlayer();
        }

        if (mPlayer.isPlaying())
        {
            mPlayer.stop();
        }

        mPlayer.reset();

        try
        {
            AssetFileDescriptor afd = getAssets().openFd("yoga-music.mp3");
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

            Log.i(TAG, "playYogaMusic: prepareAsync");
            mPlayer.prepareAsync();

            isPlayingYogaMusic = true;
        } catch (IOException e)
        {
            Log.e(TAG, "playYogaMusic: unable to open music file", e);
        }
    }

    /**
     * Start playing meditation bell music in media player.
     */
    public void playMeditationBellMusic()
    {
        if (mPlayer == null)
        {
            initPlayer();
        }

        if (mPlayer.isPlaying())
        {
            mPlayer.stop();
        }

        mPlayer.reset();

        try
        {
            AssetFileDescriptor afd = getAssets().openFd("meditation-bell.mp3");
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

            Log.i(TAG, "playMeditationBellMusic: prepareAsync");
            mPlayer.prepareAsync();

            isPlayingYogaMusic = false;
        } catch (IOException e)
        {
            Log.e(TAG, "playMeditationBellMusic: unable to open music file", e);
        }
    }

    @Override
    public void onDestroy()
    {
        if (mPlayer != null)
        {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        super.onDestroy();
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        Log.d(TAG, "onCompletion() called with: " + "mp = [" + mp + "]");

        if (isPlayingYogaMusic)
        {
            mp.seekTo(0);
            mp.start();
        }
        else
        {
            stopSelf();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        Log.d(TAG, "onPrepared() called with: " + "mp = [" + mp + "]");

        mp.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        Log.d(TAG, "onError() called with: " + "mp = [" + mp + "], what = [" + what + "], extra = [" + extra + "]");

        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent)
    {
        Log.d(TAG, "onBufferingUpdate() called with: " + "mp = [" + mp + "], percent = [" + percent + "]");
    }
}
