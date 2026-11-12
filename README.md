# DNS Lock (headless device owner)

No icon, no screen. Locks Private DNS to no-ads-porn-malware-typo.freedns.controld.com and blocks VPN setup.
Controlled only from a laptop over ADB.

## 1. Build
1. Create a new GitHub repo. Upload everything in this folder (keep the .github folder).
   If the hidden .github folder won't upload by drag-and-drop, use
   Add file > Create new file, type `.github/workflows/build.yml` as the name, and paste the file contents.
2. Actions tab > "Build APK" > wait for green tick > open the run > download artifact `dnslock-apk`.
   Unzip it to get app-debug.apk.

## 2. Phone prep
- Back up. Remove ALL accounts (Settings > Accounts: Google, Infinix/Transsion, anything else).
- Remove any existing VPN configurations before provisioning.
- Developer options: USB debugging ON (and any extra "USB debugging (security settings)" toggle if present).

## 3. Install and enable (laptop)
    adb devices
    adb install -t app-debug.apk
    adb shell dpm set-device-owner com.dnslock.admin/.AdminReceiver

## 4. Verify
    adb shell settings get global private_dns_mode        # hostname
    adb shell settings get global private_dns_specifier   # no-ads-porn-malware-typo.freedns.controld.com
    adb logcat -d -s DnsLock
Settings > Network > Private DNS should be greyed out. Now re-add your accounts.

## Re-apply (if DNS ever shows as unset, e.g. no internet during first run)
    adb shell am broadcast -n com.dnslock.admin/.ApplyReceiver -a com.dnslock.admin.APPLY

## Remove (laptop only, no factory reset)
    adb shell dpm remove-active-admin com.dnslock.admin/.AdminReceiver
    adb uninstall com.dnslock.admin
Then set Private DNS back to automatic or off in Settings if you want.

## Upgrading to a new build
Debug signing keys are ephemeral (CI generates a new one each build), so a new APK cannot be installed over the old one. To upgrade, from the laptop:
1. `adb shell dpm remove-active-admin com.dnslock.admin/.AdminReceiver`
2. `adb uninstall com.dnslock.admin`
3. `adb install -t app-debug.apk`   (the new build)
4. `adb shell dpm set-device-owner com.dnslock.admin/.AdminReceiver`
Step 4 again needs the phone to have no accounts on it, so remove them first and re-add afterwards.
