package com.dnslock.admin;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

/** Runs the policy once, as soon as this app becomes device owner. */
public class AdminReceiver extends DeviceAdminReceiver {
    @Override
    public void onEnabled(Context context, Intent intent) {
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
