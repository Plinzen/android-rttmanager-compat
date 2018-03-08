# RttManager Compat

## DEPRECATED: The purpose of this library was to make use of the internal RttManager API up to Android Oreo (requires ROOT). With Android P this API is public available in the SDK now and is NOT reflected in this library.

This library wraps the currently only as `@SystemAPI` available `android.net.wifi.RttManager` class.

__WARNING: This library makes use of Java Reflection to communicate with the Android RttManager. If there are some changes in the RttManager class this could break the library!__

The RttManager allows to measure the Round Trip Time (RTT) of a data packet + ACK to Access Point (AP) and back. The standard describing the measurement is IEEE 802.11mc (http://ieeexplore.ieee.org/document/6805649/).

## Requirements

* A rooted Android phone (app needs to be installed in sys/priv-app folder)
* Min SDK 23 (Marshmallow)
* Your smartphone supports the IEEE 802.11mc standard. (e.g. has Qualcomm Snapdragon 820 CPU)
* Your AP support the IEEE 802.11mc standard.

## Usage

* Clone the library and add it to your project.
* Use `RttManagerCompat`-class to retrieve a wrapper for the `android.net.wifi.RttManager`.
* Gather the features supported by your phone by call `RttManagerCompat.getRttCapabilities()`.
* To perform some ranging the following snippet might help:

```
public void startRanging(@NonNull final ScanResult wifiConfig,
            @NonNull final RttManagerCompat.RttListener rttListener) throws Throwable {        
    final RttManagerCompat.RttParams params = new RttManagerCompat.RttParams();
    params.bssid = wifiConfig.BSSID;
    params.requestType = RttManagerCompat.RTT_TYPE_TWO_SIDED;
    params.frequency = wifiConfig.frequency;
    params.centerFreq0 = wifiConfig.centerFreq0;
    params.centerFreq1 = wifiConfig.centerFreq1;
    params.channelWidth = wifiConfig.channelWidth;
    final RttManagerCompat.RttCapabilities capabilities = tManagerCompat.getRttCapabilities();
    if (capabilities != null) {
        params.LCIRequest = capabilities.lciSupported;
        params.LCRRequest = capabilities.lcrSupported;
    }
    rttManagerCompat.startRanging(new RttManagerCompat.RttParams[]{params}, rttListener);
}
```

## Installation

__Please notice that a rooted phone is required__

Perform the following steps to install your app containing this SDK:

* compile the app: `./gradlew clean assembleDebug`
* copy the APK to the phone, e.g. `adb push <<path_to_apk>> /sdcard` (`adb push app/build/outputs/app-debug.apk /sdcard`)
* Open a file manager app, which is capable to perform root operations, eg. `ES Filemanager`.
* Copy the APK from `/sdcard` to `/system/priv-app`
* (Optinal) Sometime it is necessary to check the permission of the copied APK. It should be executable by everyone.
* Reboot the phone. The app should be installed and be available in the app launcher.

### More details about the installation can be found here:

* http://stackoverflow.com/questions/24296286/get-android-system-permissions-by-putting-app-in-system-app
* http://stackoverflow.com/questions/19868138/aosp-privileged-vs-system-app
* http://android.stackexchange.com/questions/27/how-do-i-properly-install-a-system-app-given-its-apk
