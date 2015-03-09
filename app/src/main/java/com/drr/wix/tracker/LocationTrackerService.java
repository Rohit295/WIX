package com.drr.wix.tracker;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.util.Log;

/**
 *
 */
public class LocationTrackerService extends Service {

    private LocationTrackerServiceHandler mServiceHandler;
    private Messenger mServiceMessenger;

    @Override
    public void onCreate() {
        super.onCreate();

        // Start up the thread running the service.  Note that we create a separate thread because
        // the service normally runs in the process's main thread, which we don't want to block.
        // We also make it background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceHandler = new LocationTrackerServiceHandler(this);
        mServiceMessenger = new Messenger(mServiceHandler);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(this.getClass().getName(), "OnBind invocation");
        return mServiceMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(this.getClass().getName(), "Start Command: startid="+startId);

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        return START_STICKY;
    }
}


