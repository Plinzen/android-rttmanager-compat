package de.plinzen.rttmanager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Proxy;

class RttManagerCompatUtil {



    static final String CLASS_RESPONDER_CALLBACK = "android.net.wifi.RttManager$ResponderCallback";
    static final String CLASS_RTT_LISTENER = "android.net.wifi.RttManager$RttListener";
    static final String CLASS_RTT_PARAMS = "android.net.wifi.RttManager$RttParams";

    static RttManagerCompat.Capabilities buildCapabilitiesFromNativeObject(
            @NonNull final Object nativeCapabilities) {
        final Class<?> nativeCapabilitiesClass = nativeCapabilities.getClass();
        final RttManagerCompat.Capabilities capabilities = new RttManagerCompat.Capabilities();
        capabilities.supportedType = readInt(nativeCapabilitiesClass, nativeCapabilities, "supportedType");
        capabilities.supportedPeerType = readInt(nativeCapabilitiesClass, nativeCapabilities, "supportedPeerType");
        return capabilities;
    }

    static RttManagerCompat.RttCapabilities buildRttCapabilitiesFromNativeObject(final Object nativeCapabilities) {
        if (nativeCapabilities == null) {
            return null;
        }
        final Class<?> nativeCapabilitiesClass = nativeCapabilities.getClass();
        final RttManagerCompat.RttCapabilities capabilities = new RttManagerCompat.RttCapabilities();

        capabilities.supportedType = readBoolean(nativeCapabilitiesClass, nativeCapabilities, "supportedType");
        capabilities.supportedPeerType = readBoolean(nativeCapabilitiesClass, nativeCapabilities, "supportedPeerType");
        capabilities.oneSidedRttSupported = readBoolean(nativeCapabilitiesClass, nativeCapabilities,
                "oneSidedRttSupported");
        capabilities.twoSided11McRttSupported = readBoolean(nativeCapabilitiesClass, nativeCapabilities,
                "twoSided11McRttSupported");
        capabilities.lciSupported = readBoolean(nativeCapabilitiesClass, nativeCapabilities, "lciSupported");
        capabilities.lcrSupported = readBoolean(nativeCapabilitiesClass, nativeCapabilities, "lcrSupported");
        capabilities.preambleSupported = readInt(nativeCapabilitiesClass, nativeCapabilities, "preambleSupported");
        capabilities.bwSupported = readInt(nativeCapabilitiesClass, nativeCapabilities, "bwSupported");
        capabilities.responderSupported = readBoolean(nativeCapabilitiesClass, nativeCapabilities,
                "responderSupported");
        return capabilities;
    }

    static Object buildNativeRttParams(
            @Nullable final RttManagerCompat.RttParams[] params) throws ClassNotFoundException,
            IllegalAccessException, InstantiationException {
        if (params == null) {
            return null;
        }
        final Class<?> nativeParamClass = Class.forName(CLASS_RTT_PARAMS);
        final Object nativeParams = Array.newInstance(nativeParamClass, params.length);
        for (int i = 0; i < params.length; i++) {
            final RttManagerCompat.RttParams rttParam = params[i];
            final Object nativeParam = nativeParamClass.newInstance();
            setInt(nativeParamClass, nativeParam, "deviceType", rttParam.deviceType);
            setInt(nativeParamClass, nativeParam, "requestType", rttParam.requestType);

            setBoolean(nativeParamClass, nativeParam, "secure", rttParam.secure);
            setObject(nativeParamClass, nativeParam, "bssid", rttParam.bssid);
            setInt(nativeParamClass, nativeParam, "frequency", rttParam.frequency);
            setInt(nativeParamClass, nativeParam, "channelWidth", rttParam.channelWidth);
            setInt(nativeParamClass, nativeParam, "centerFreq0", rttParam.centerFreq0);
            setInt(nativeParamClass, nativeParam, "centerFreq1", rttParam.centerFreq1);
            setInt(nativeParamClass, nativeParam, "num_samples", rttParam.num_samples);
            setInt(nativeParamClass, nativeParam, "num_retries", rttParam.num_retries);
            setInt(nativeParamClass, nativeParam, "numberBurst", rttParam.numberBurst);
            setInt(nativeParamClass, nativeParam, "interval", rttParam.interval);
            setInt(nativeParamClass, nativeParam, "numSamplesPerBurst", rttParam.numSamplesPerBurst);
            setInt(nativeParamClass, nativeParam, "numRetriesPerMeasurementFrame", rttParam
                    .numRetriesPerMeasurementFrame);
            setInt(nativeParamClass, nativeParam, "numRetriesPerFTMR", rttParam.numRetriesPerFTMR);
            setBoolean(nativeParamClass, nativeParam, "LCIRequest", rttParam.LCIRequest);
            setBoolean(nativeParamClass, nativeParam, "LCRRequest", rttParam.LCRRequest);
            setInt(nativeParamClass, nativeParam, "burstTimeout", rttParam.burstTimeout);
            setInt(nativeParamClass, nativeParam, "preamble", rttParam.preamble);
            setInt(nativeParamClass, nativeParam, "bandwidth", rttParam.bandwidth);
            Array.set(nativeParams, i, nativeParam);
        }
        return nativeParams;
    }

    static RttManagerCompat.RttListenerWrapper wrapRttListener(
            @Nullable final RttManagerCompat.RttListener rttListener) throws
            ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (rttListener == null) {
            return null;
        }
        final Class<?> nativeParamClass = Class.forName(CLASS_RTT_LISTENER);
        return (RttManagerCompat.RttListenerWrapper) Proxy
                .newProxyInstance(RttManagerCompat.RttListenerWrapper.class.getClassLoader(), new
                        Class[]{RttManagerCompat
                        .RttListenerWrapper.class, nativeParamClass}, (proxy,
                        method, args) -> {
                    if ("onSuccess".equals(method.getName())) {
                        rttListener.onSuccess(buildRttResultFromNativeObjects((Object[]) args[0]));
                    } else if ("onFailure".equals(method.getName())) {
                        rttListener.onFailure((int) args[0], (String) args[1]);
                    } else if ("onAborted".equals(method.getName())) {
                        rttListener.onAborted();
                    } else if ("getRttListenerCompat".equals(method.getName())) {
                        return rttListener;
                    }
                    return null;
                });
    }

    static RttManagerCompat.ResponderCallbackWrapper wrapResponderCallback(
            @Nullable final RttManagerCompat.ResponderCallback responderCallback) throws
            ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (responderCallback == null) {
            return null;
        }
        final Class<?> nativeParamClass = Class.forName(CLASS_RESPONDER_CALLBACK);
        return (RttManagerCompat.ResponderCallbackWrapper) Proxy
                .newProxyInstance(RttManagerCompat.ResponderCallbackWrapper.class.getClassLoader(), new
                        Class[]{RttManagerCompat.ResponderCallbackWrapper.class, nativeParamClass}, (proxy,
                        method, args) -> {
                    if ("onResponderEnabled".equals(method.getName())) {
                        responderCallback.onResponderEnabled(buildResponderConfigFromNativeObjects(args[0]));
                    } else if ("onResponderEnableFailure".equals(method.getName())) {
                        responderCallback.onResponderEnableFailure((int) args[0]);
                    } else if ("getResponderCallback".equals(method.getName())) {
                        return responderCallback;
                    }
                    return null;
                });
    }

    private static void setInt(@NonNull final Class<?> nativeParamClass, @NonNull final Object nativeObject,
            @NonNull final String fieldName, @NonNull final int value) {
        try {
            nativeParamClass.getDeclaredField(fieldName).setInt(nativeObject, value);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
    
    private static final String TAG = RttManagerCompatUtil.class.getSimpleName();

    private static void setObject(@NonNull final Class<?> nativeParamClass, @NonNull final Object nativeObject,
            @NonNull final String fieldName, @NonNull final Object value) {
        try {
            nativeParamClass.getDeclaredField(fieldName).set(nativeObject, value);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }


    private static void setBoolean(@NonNull final Class<?> nativeParamClass, @NonNull final Object nativeObject,
            @NonNull final String fieldName, @NonNull final boolean value) {
        try {
            nativeParamClass.getDeclaredField(fieldName).setBoolean(nativeObject, value);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }


    private static boolean readBoolean(@NonNull final Class<?> nativeClass, @NonNull final Object nativeObject,
            @NonNull final String fieldName) {
        try {
            return nativeClass.getDeclaredField(fieldName).getBoolean(nativeObject);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return false;
    }

    private static int readInt(@NonNull final Class<?> nativeClass, @NonNull final Object nativeObject,
            @NonNull final String fieldName) {
        try {
            return nativeClass.getDeclaredField(fieldName).getInt(nativeObject);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return 0;
    }

    private static long readLong(@NonNull final Class<?> nativeClass, @NonNull final Object nativeObject,
            @NonNull final String fieldName) {
        try {
            return nativeClass.getDeclaredField(fieldName).getLong(nativeObject);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return 0;
    }

    private static byte readByte(@NonNull final Class<?> nativeClass, @NonNull final Object nativeObject,
            @NonNull final String fieldName) {
        try {
            return nativeClass.getDeclaredField(fieldName).getByte(nativeObject);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return 0;
    }


    private static <T> T readObject(@NonNull final Class<?> nativeClass, @NonNull final Object nativeObject,
            @NonNull final String fieldName) {
        try {
            return (T) nativeClass.getDeclaredField(fieldName).get(nativeObject);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    private static RttManagerCompat.ResponderConfig buildResponderConfigFromNativeObjects(
            @Nullable final Object nativeResult) {
        if (nativeResult == null) {
            return null;
        }
        final Class<?> nativeResultClass = nativeResult.getClass();
        final RttManagerCompat.ResponderConfig config = new RttManagerCompat.ResponderConfig();
        config.centerFreq0 = readInt(nativeResultClass, nativeResult, "centerFreq0");
        config.centerFreq1 = readInt(nativeResultClass, nativeResult, "centerFreq1");
        config.macAddress = readObject(nativeResultClass, nativeResult, "macAddress");
        config.frequency = readInt(nativeResultClass, nativeResult, "frequency");
        config.channelWidth = readInt(nativeResultClass, nativeResult, "channelWidth");
        config.preamble = readInt(nativeResultClass, nativeResult, "preamble");
        return config;
    }


    private static RttManagerCompat.WifiInformationElement buildWifiInformationElementFromNativeObjects(
            @Nullable final Object nativeResult) {
        if (nativeResult == null) {
            return null;
        }
        final Class<?> wifiInformationClass = nativeResult.getClass();
        final RttManagerCompat.WifiInformationElement wifiInformation = new RttManagerCompat.WifiInformationElement();
        wifiInformation.id = readByte(wifiInformationClass, nativeResult, "id");
        wifiInformation.data = readObject(wifiInformationClass, nativeResult, "data");
        return wifiInformation;
    }

    private static RttManagerCompat.RttResult[] buildRttResultFromNativeObjects(
            @Nullable final Object nativeResults) throws NoSuchFieldException, IllegalAccessException {
        if (nativeResults == null) {
            return null;
        }
        final RttManagerCompat.RttResult[] results = new RttManagerCompat.RttResult[Array.getLength(nativeResults)];
        for (int i = 0; i < results.length; i++) {
            final Object nativeResult = Array.get(nativeResults, i);
            final Class<?> nativeResultClass = nativeResult.getClass();
            final RttManagerCompat.RttResult result = new RttManagerCompat.RttResult();
            result.bssid = readObject(nativeResultClass, nativeResult, "bssid");
            result.burstNumber = readInt(nativeResultClass, nativeResult, "burstNumber");
            result.measurementFrameNumber = readInt(nativeResultClass, nativeResult, "measurementFrameNumber");
            result.successMeasurementFrameNumber = readInt(nativeResultClass, nativeResult,
                    "successMeasurementFrameNumber");
            result.frameNumberPerBurstPeer = readInt(nativeResultClass, nativeResult, "frameNumberPerBurstPeer");
            result.status = readInt(nativeResultClass, nativeResult, "status");
            result.requestType = readInt(nativeResultClass, nativeResult, "requestType");
            result.measurementType = readInt(nativeResultClass, nativeResult, "measurementType");
            result.retryAfterDuration = readInt(nativeResultClass, nativeResult, "retryAfterDuration");
            result.ts = readLong(nativeResultClass, nativeResult, "ts");
            result.rssi = readInt(nativeResultClass, nativeResult, "rssi");
            result.rssiSpread = readInt(nativeResultClass, nativeResult, "rssiSpread");
            result.txRate = readInt(nativeResultClass, nativeResult, "txRate");
            result.rxRate = readInt(nativeResultClass, nativeResult, "rxRate");
            result.rtt = readLong(nativeResultClass, nativeResult, "rtt");
            result.rttStandardDeviation = readLong(nativeResultClass, nativeResult, "rttStandardDeviation");
            result.rttSpread = readLong(nativeResultClass, nativeResult, "rttSpread");
            result.distance = readInt(nativeResultClass, nativeResult, "distance");
            result.distanceStandardDeviation = readInt(nativeResultClass, nativeResult, "distanceStandardDeviation");
            result.distanceSpread = readInt(nativeResultClass, nativeResult, "distanceSpread");
            result.burstDuration = readInt(nativeResultClass, nativeResult, "burstDuration");
            result.negotiatedBurstNum = readInt(nativeResultClass, nativeResult, "negotiatedBurstNum");
            result.secure = readBoolean(nativeResultClass, nativeResult, "secure");
            result.LCI = buildWifiInformationElementFromNativeObjects(nativeResultClass.getField("LCI").get
                    (nativeResult));
            result.LCR = buildWifiInformationElementFromNativeObjects(nativeResultClass.getField("LCR").get
                    (nativeResult));
            results[i] = result;
        }
        return results;
    }
}
