package com.dnslock.admin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Lets the laptop re-apply the policy via adb. Nothing on the phone can call it usefully. */
public class ApplyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pending = goAsync();
        new Thread(() -> {
            try {
                Policy.apply(context);
            } finally {
                pending.finish();
            }
        }).start();
    }
}
