package com.app.eisenflow.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import static com.app.eisenflow.utils.Constants.LOCK_NAME_LOCAL;
import static com.app.eisenflow.utils.Constants.LOCK_NAME_STATIC;

/**
 * Created on 1/28/18.
 */

public class WakefulIntentService extends IntentService {
    private static PowerManager.WakeLock sLock;
    private PowerManager.WakeLock mLock;

    public WakefulIntentService(String name) {
        super(name);
    }

    /**
     * Acquire a partial static WakeLock, needs to be called within the class
     * that calls startService()
     * @param context
     */
    public static void acquireStaticLock(Context context) {
        getLock(context).acquire();
    }

    synchronized private static PowerManager.WakeLock getLock(Context context) {
        if (sLock == null) {
            PowerManager
                    mgr=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
            sLock = mgr.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    LOCK_NAME_STATIC);
            sLock.setReferenceCounted(true);
        }
        return(sLock);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                LOCK_NAME_LOCAL);
        mLock.setReferenceCounted(true);
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        mLock.acquire();
        super.onStart(intent, startId);
        getLock(this).release();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mLock.release();
    }
}
