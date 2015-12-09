package it.techies.pranayama.services;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

import timber.log.Timber;


public class PrayanamaService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener
{
    private static final String TAG = PrayanamaService.class.getSimpleName();

    private MediaPlayer mPlayer;

    public PrayanamaService()
    {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Timber.d("onStartCommand(int flags, int startId)");
        Timber.d("onStartCommand(int %d, int %d)", flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setScreenOnWhilePlaying(true);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnBufferingUpdateListener(this);

        try
        {
            AssetFileDescriptor afd = getAssets().openFd("yoga-music.mp3");
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            Timber.d("prepareAsync()");
            mPlayer.prepareAsync();
        }
        catch (IOException e)
        {
            Timber.e(e, "Media Player");
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        if(mPlayer != null)
        {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        Timber.d("onCompletion(%s)", mp.toString());
        mp.seekTo(0);
        mp.start();
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        Timber.d("onPrepared(%s)", mp.toString());
        mp.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        Timber.d("onError(MediaPlayer mp, int what, int extra)");
        Timber.d("onError(what %d, extra %d)", what, extra);
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent)
    {
        Timber.d("onBufferingUpdate(MediaPlayer mp, int percent)");
        Timber.d("onBufferingUpdate(MediaPlayer %s, int %d)", mp.toString(), percent);
    }
}
