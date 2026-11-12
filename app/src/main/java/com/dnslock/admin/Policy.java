package com.dnslock.admin;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.UserManager;
import android.util.Log;

/** The whole policy. Change HOST if you want a different DNS provider. */
final class Policy {
    static final String TAG = "DnsLock";
    static final String HOST = "no-ads-porn-malware-typo.freedns.controld.com";

    private Policy() {}

    static void apply(Context context) {
        DevicePolicyManager dpm =
                (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(context, AdminReceiver.class);

        if (!dpm.isDeviceOwnerApp(context.getPackageName())) {
            Log.w(TAG, "Not device owner, nothing applied");
            return;
        }

        // Each step is isolated so a failure in one is logged on its own
        // and the later steps still run (hardening).
        liftRestriction(dpm, admin);
        setDns(dpm, admin);
        applyRestrictions(dpm, admin);
    }

    private static void liftRestriction(DevicePolicyManager dpm, ComponentName admin) {
        try {
            dpm.clearUserRestriction(admin, UserManager.DISALLOW_CONFIG_PRIVATE_DNS);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException in step: restriction lift", e);
        } catch (Exception e) {
            Log.e(TAG, "Failure in step: restriction lift", e);
        }
    }

    private static void setDns(DevicePolicyManager dpm, ComponentName admin) {
        try {
            int result = dpm.setGlobalPrivateDnsModeSpecifiedHost(admin, HOST);
            if (result == DevicePolicyManager.PRIVATE_DNS_SET_NO_ERROR) {
                Log.i(TAG, "DNS set OK: " + HOST);
            } else {
                Log.e(TAG, "Failure in step: DNS set, result code " + result
                        + " (check internet, then re-run the APPLY broadcast)");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException in step: DNS set", e);
        } catch (Exception e) {
            Log.e(TAG, "Failure in step: DNS set", e);
        }
    }

    private static void applyRestrictions(DevicePolicyManager dpm, ComponentName admin) {
        try {
            dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_PRIVATE_DNS);
            dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_VPN);
            dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_CREDENTIALS);
            Log.i(TAG, "Restrictions re-apply OK");
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException in step: restriction re-apply", e);
        } catch (Exception e) {
            Log.e(TAG, "Failure in step: restriction re-apply", e);
        }
    }
}
