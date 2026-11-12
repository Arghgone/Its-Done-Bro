package com.dnslock.admin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Re-runs the policy after boot. Policy.apply() does nothing unless this app is device owner. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
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
