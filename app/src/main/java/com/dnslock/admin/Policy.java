package com.dnslock.admin;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;

public final class Policy {
    public static final String TAG = "DnsLock";
    public static final String DNS_HOST = "no-ads-porn-malware-typo.freedns.controld.com";

    private Policy() {}

    public static void applyAll(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(context, AdminReceiver.class);

        if (dpm == null || !dpm.isDeviceOwnerApp(context.getPackageName())) {
            Log.e(TAG, "Not device owner; cannot apply policy");
            return;
        }

        try {
            // 1. Temporarily lift Private DNS restriction so Device Owner can write Settings.Global directly
            dpm.clearUserRestriction(admin, UserManager.DISALLOW_CONFIG_PRIVATE_DNS);

            // 2. Write Private DNS mode and Control D hostname directly (bypasses broken port 853 probe)
            Settings.Global.putString(context.getContentResolver(), "private_dns_specifier", DNS_HOST);
            Settings.Global.putString(context.getContentResolver(), "private_dns_mode", "hostname");

            // Also attempt official DPM API silently in case ROM supports it
            try {
                dpm.setGlobalPrivateDnsModeSpecifiedHost(admin, DNS_HOST);
            } catch (Throwable ignored) {}

            Log.i(TAG, "Private DNS applied: " + DNS_HOST);
        } catch (Exception e) {
            Log.e(TAG, "Failed writing Private DNS settings", e);
        } finally {
            // 3. Re-lock Private DNS and VPN settings immediately
            dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_PRIVATE_DNS);
            dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_VPN);
            Log.i(TAG, "Restrictions locked OK");
        }
    }
}
