# RttManager Compat
This library wraps the currently only as `@SystemAPI` available `android.net.wifi.RttManager` class.

__WARNING: This library makes use of Java Reflection to communicate with the Android RttManager. If there are some changes in the RttManager class this could break the library!__

The RttManager allows to measure the Round Trip Time (RTT) of a data packet + ACK to Access Point (AP) and back. The standard describing the measurement is IEEE 802.11mc (http://ieeexplore.ieee.org/document/6805649/).

## Requirements

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
