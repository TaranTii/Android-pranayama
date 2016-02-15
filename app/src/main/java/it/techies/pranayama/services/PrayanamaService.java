package it.techies.pranayama.services;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import com.squareup.otto.Bus;

import java.io.IOException;

import it.techies.pranayama.MyApplication;
import it.techies.pranayama.infrastructure.Auth;
import timber.log.Timber;


public class PrayanamaService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener {

    private static final String TAG = PrayanamaService.class.getSimpleName();

    protected Auth mAuth;
    protected Bus mBus;
    protected MyApplication mApplication;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private MediaPlayer mPlayer;

    private boolean isPlayingYogaMusic = false;

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
        Timber.tag(TAG);
        Timber.d("onCreate()");

        mApplication = (MyApplication) getApplication();
        mBus = mApplication.getBus();
        mAuth = mApplication.getAuth();
        mBus.register(this);

        playYogaMusic();
    }

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
            Timber.d("prepareAsync()");
            mPlayer.prepareAsync();

            isPlayingYogaMusic = true;

        } catch (IOException e)
        {
            Timber.e(e, "Media Player");
        }
    }

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
            Timber.d("prepareAsync()");
            mPlayer.prepareAsync();

            isPlayingYogaMusic = false;

        } catch (IOException e)
        {
            Timber.e(e, "Media Player");
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        mBus.unregister(this);

        Timber.d("onDestroy()");

        if (mPlayer != null)
        {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        Timber.d("onCompletion()");

        if (isPlayingYogaMusic)
        {
            mp.seekTo(0);
            mp.start();
        }
        else
        {
            playYogaMusic();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        Timber.d("onPrepared()");
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
