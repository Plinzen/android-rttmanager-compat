package de.plinzen.rttmanager;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RttManagerCompat {
    public interface ResponderCallbackWrapper {
        ResponderCallback getResponderCallback();
    }


    public interface RttListener {
        void onAborted();

        void onFailure(int reason, String description);

        void onSuccess(RttResult[] results);
    }

    public interface RttListenerWrapper {
        RttListener getRttListenerCompat();
    }

    /**
     * @deprecated Use the new {@link RttManagerCompat.RttCapabilities} API
     */
    @Deprecated
    public static class Capabilities {
        public int supportedPeerType;
        public int supportedType;
    }

    /**
     * Callbacks for responder operations.
     * <p>
     * A {@link ResponderCallback} is the handle to the calling client. {@link RttManagerCompat} will keep
     * a reference to the callback for the entire period when responder is enabled. The same
     * callback as used in enabling responder needs to be passed for disabling responder.
     * The client can freely destroy or reuse the callback after {@link RttManagerCompat#disableResponder}
     * is called.
     */
    public abstract static class ResponderCallback {
        /**
         * Callback when enabling responder failed.
         */
        public abstract void onResponderEnableFailure(int reason);

        /**
         * Callback when responder is enabled.
         */
        public abstract void onResponderEnabled(ResponderConfig config);
        // TODO: consider adding onResponderAborted once it's supported.
    }

    /**
     * Configuration used for RTT responder mode. The configuration information can be used by a
     * peer device to range the responder.
     *
     * @see ScanResult
     */
    public static class ResponderConfig implements Parcelable {
        // TODO: make all fields final once we can get mac address from responder HAL APIs.
        /**
         * Implement {@link Parcelable} interface
         */
        public static final Parcelable.Creator<ResponderConfig> CREATOR =
                new Parcelable.Creator<ResponderConfig>() {
                    @Override
                    public ResponderConfig createFromParcel(Parcel in) {
                        ResponderConfig config = new ResponderConfig();
                        config.macAddress = in.readString();
                        config.frequency = in.readInt();
                        config.centerFreq0 = in.readInt();
                        config.centerFreq1 = in.readInt();
                        config.channelWidth = in.readInt();
                        config.preamble = in.readInt();
                        return config;
                    }

                    @Override
                    public ResponderConfig[] newArray(int size) {
                        return new ResponderConfig[size];
                    }
                };
        /**
         * Center frequency of the channel where responder is enabled on. Only in use when channel
         * width is at least 40MHz.
         *
         * @see ScanResult#centerFreq0
         */
        public int centerFreq0;
        /**
         * Center frequency of the second segment when channel width is 80 + 80 MHz.
         *
         * @see ScanResult#centerFreq1
         */
        public int centerFreq1;
        /**
         * Width of the channel where responder is enabled on.
         *
         * @see ScanResult#channelWidth
         */
        public int channelWidth;
        /**
         * The primary 20 MHz frequency (in MHz) of the channel where responder is enabled.
         *
         * @see ScanResult#frequency
         */
        public int frequency;
        /**
         * Wi-Fi mac address used for responder mode.
         */
        public String macAddress = "";
        /**
         * Preamble supported by responder.
         */
        public int preamble;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("macAddress = ").append(macAddress)
                    .append(" frequency = ").append(frequency)
                    .append(" centerFreq0 = ").append(centerFreq0)
                    .append(" centerFreq1 = ").append(centerFreq1)
                    .append(" channelWidth = ").append(channelWidth)
                    .append(" preamble = ").append(preamble);
            return builder.toString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(macAddress);
            dest.writeInt(frequency);
            dest.writeInt(centerFreq0);
            dest.writeInt(centerFreq1);
            dest.writeInt(channelWidth);
            dest.writeInt(preamble);
        }
    }

    /**
     * This class describe the RTT capability of the Hardware
     */
    public static class RttCapabilities implements Parcelable {
        /**
         * Implement the Parcelable interface {@hide}
         */
        public static final Creator<RttCapabilities> CREATOR =
                new Creator<RttCapabilities>() {
                    @Override
                    public RttCapabilities createFromParcel(Parcel in) {
                        RttCapabilities capabilities = new RttCapabilities();
                        capabilities.oneSidedRttSupported = (in.readInt() == 1);
                        capabilities.twoSided11McRttSupported = (in.readInt() == 1);
                        capabilities.lciSupported = (in.readInt() == 1);
                        capabilities.lcrSupported = (in.readInt() == 1);
                        capabilities.preambleSupported = in.readInt();
                        capabilities.bwSupported = in.readInt();
                        capabilities.responderSupported = (in.readInt() == 1);
                        capabilities.secureRttSupported = (in.readInt() == 1);
                        capabilities.mcVersion = in.readInt();
                        return capabilities;
                    }

                    /** Implement the Parcelable interface {@hide} */
                    @Override
                    public RttCapabilities[] newArray(int size) {
                        return new RttCapabilities[size];
                    }
                };
        //RTT bandwidth supported
        public int bwSupported;
        //location configuration information supported
        public boolean lciSupported;
        //location civic records supported
        public boolean lcrSupported;
        /**
         * Draft 11mc version supported, including major and minor version. e.g, draft 4.3 is 43
         */
        public int mcVersion;
        //1-sided rtt measurement is supported
        public boolean oneSidedRttSupported;
        //preamble supported, see bit mask definition above
        public int preambleSupported;
        // Whether STA responder role is supported.
        public boolean responderSupported;
        /**
         * Whether the secure RTT protocol is supported.
         */
        public boolean secureRttSupported;
        /**
         * @deprecated It is not supported
         */
        @Deprecated
        public boolean supportedPeerType;
        /**
         * @deprecated It is not supported
         */
        @Deprecated
        public boolean supportedType;
        //11mc 2-sided rtt measurement is supported
        public boolean twoSided11McRttSupported;

        /**
         * Implement the Parcelable interface {@hide}
         */
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("oneSidedRtt ").
                    append(oneSidedRttSupported ? "is Supported. " : "is not supported. ").
                    append("twoSided11McRtt ").
                    append(twoSided11McRttSupported ? "is Supported. " : "is not supported. ").
                    append("lci ").
                    append(lciSupported ? "is Supported. " : "is not supported. ").
                    append("lcr ").
                    append(lcrSupported ? "is Supported. " : "is not supported. ");
            if ((preambleSupported & PREAMBLE_LEGACY) != 0) {
                sb.append("Legacy ");
            }
            if ((preambleSupported & PREAMBLE_HT) != 0) {
                sb.append("HT ");
            }
            if ((preambleSupported & PREAMBLE_VHT) != 0) {
                sb.append("VHT ");
            }
            sb.append("is supported. ");
            if ((bwSupported & RTT_BW_5_SUPPORT) != 0) {
                sb.append("5 MHz ");
            }
            if ((bwSupported & RTT_BW_10_SUPPORT) != 0) {
                sb.append("10 MHz ");
            }
            if ((bwSupported & RTT_BW_20_SUPPORT) != 0) {
                sb.append("20 MHz ");
            }
            if ((bwSupported & RTT_BW_40_SUPPORT) != 0) {
                sb.append("40 MHz ");
            }
            if ((bwSupported & RTT_BW_80_SUPPORT) != 0) {
                sb.append("80 MHz ");
            }
            if ((bwSupported & RTT_BW_160_SUPPORT) != 0) {
                sb.append("160 MHz ");
            }
            sb.append("is supported.");
            sb.append(" STA responder role is ")
                    .append(responderSupported ? "supported" : "not supported");
            sb.append(" Secure RTT protocol is ")
                    .append(secureRttSupported ? "supported" : "not supported");
            sb.append(" 11mc version is " + mcVersion);
            return sb.toString();
        }

        /**
         * Implement the Parcelable interface {@hide}
         */
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(oneSidedRttSupported ? 1 : 0);
            dest.writeInt(twoSided11McRttSupported ? 1 : 0);
            dest.writeInt(lciSupported ? 1 : 0);
            dest.writeInt(lcrSupported ? 1 : 0);
            dest.writeInt(preambleSupported);
            dest.writeInt(bwSupported);
            dest.writeInt(responderSupported ? 1 : 0);
            dest.writeInt(secureRttSupported ? 1 : 0);
            dest.writeInt(mcVersion);
        }
    }

    /**
     * specifies parameters for RTT request
     */
    public static class RttParams {
        /**
         * Request LCI information, only available when choose double side RTT measurement
         * need check RttCapabilties first.
         * Default value: false
         */
        public boolean LCIRequest;
        /**
         * Request LCR information, only available when choose double side RTT measurement
         * need check RttCapabilties first.
         * Default value: false
         */
        public boolean LCRRequest;
        /**
         * bandWidth used for RTT measurement.User need verify the highest BW the destination
         * support (from scan result etc) before set this value. Wider channels result usually give
         * better accuracy. However, the frame loss can increase too.
         * should be one of RTT_BW_5_SUPPORT to RTT_BW_160_SUPPORT. However, need check
         * RttCapabilities firstto verify HW support this bandwidth.
         * Default value:RTT_BW_20_SUPPORT
         */
        public int bandwidth;
        /**
         * mac address of the device being ranged
         * Default value: null
         */
        public String bssid;
        /**
         * Timeout for each burst, (250 * 2^x) us,
         * Range 1-11 and 15. 15 means no control Default value: 15
         */
        public int burstTimeout;
        /**
         * Not used if the AP bandwidth is 20 MHz
         * If the AP use 40, 80 or 160 MHz, this is the center frequency
         * if the AP use 80 + 80 MHz, this is the center frequency of the first segment
         * same as ScanResult.centerFreq0
         * Default value: 0
         */
        public int centerFreq0;
        /**
         * Only used if the AP bandwidth is 80 + 80 MHz
         * if the AP use 80 + 80 MHz, this is the center frequency of the second segment
         * same as ScanResult.centerFreq1
         * Default value: 0
         */
        public int centerFreq1;
        /**
         * channel width of the destination AP. Same as ScanResult.channelWidth
         * Default value: 0
         */
        public int channelWidth;
        /**
         * type of destination device being ranged
         * currently only support RTT_PEER_TYPE_AP
         * Range:RTT_PEER_TYPE_xxxx Default value:RTT_PEER_TYPE_AP
         */
        public int deviceType;
        /**
         * The primary control channel over which the client is
         * communicating with the AP.Same as ScanResult.frequency
         * Default value: 0
         */
        public int frequency;
        /**
         * valid only if numberBurst > 1, interval between burst(100ms).
         * Range : 0-31, 0--means no specific
         * Default value: 0
         */
        public int interval;
        /**
         * number of retries for FTMR frame (control frame) if it fails.
         * Only used by 80211MC double side RTT
         * Range: 0-3  Default Value : 0
         */
        public int numRetriesPerFTMR;
        /**
         * number of retries for each measurement frame if a sample fails
         * Only used by single side RTT,
         * Range 0 - 3 Default value: 0
         */
        public int numRetriesPerMeasurementFrame;
        /**
         * number of samples to be taken in one burst
         * Range: 1-31
         * Default value: 8
         */
        public int numSamplesPerBurst;
        /**
         * number of retries if a sample fails
         *
         * @deprecated Use
         * {@link RttManagerCompat.RttParams#numRetriesPerMeasurementFrame} API.
         */
        @Deprecated
        public int num_retries;
        /**
         * number of samples to be taken
         *
         * @deprecated Use the new
         * {@link RttManagerCompat.RttParams#numSamplesPerBurst}
         */
        @Deprecated
        public int num_samples;
        /**
         * Number of burst in exp , 2^x. 0 means single shot measurement, range 0-15
         * Currently only single shot is supported
         * Default value: 0
         */
        public int numberBurst;
        /**
         * preamble used for RTT measurement
         * Range: PREAMBLE_LEGACY, PREAMBLE_HT, PREAMBLE_VHT
         * Default value: PREAMBLE_HT
         */
        public int preamble;
        /**
         * type of RTT measurement method. Need check scan result and RttCapabilities first
         * Range: RTT_TYPE_ONE_SIDED or RTT_TYPE_TWO_SIDED
         * Default value: RTT_TYPE_ONE_SIDED
         */
        public int requestType;
        /**
         * Whether the secure RTT protocol needs to be used for ranging this peer device.
         */
        public boolean secure;

        public RttParams() {
            //provide initial value for RttParams
            deviceType = RTT_PEER_TYPE_AP;
            requestType = RTT_TYPE_ONE_SIDED;
            numberBurst = 0;
            numSamplesPerBurst = 8;
            numRetriesPerMeasurementFrame = 0;
            numRetriesPerFTMR = 0;
            burstTimeout = 15;
            preamble = PREAMBLE_HT;
            bandwidth = RTT_BW_20_SUPPORT;
        }

        /**
         * {@hide}
         */
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("deviceType=" + deviceType);
            sb.append(", requestType=" + requestType);
            sb.append(", secure=" + secure);
            sb.append(", bssid=" + bssid);
            sb.append(", frequency=" + frequency);
            sb.append(", channelWidth=" + channelWidth);
            sb.append(", centerFreq0=" + centerFreq0);
            sb.append(", centerFreq1=" + centerFreq1);
            sb.append(", num_samples=" + num_samples);
            sb.append(", num_retries=" + num_retries);
            sb.append(", numberBurst=" + numberBurst);
            sb.append(", interval=" + interval);
            sb.append(", numSamplesPerBurst=" + numSamplesPerBurst);
            sb.append(", numRetriesPerMeasurementFrame=" + numRetriesPerMeasurementFrame);
            sb.append(", numRetriesPerFTMR=" + numRetriesPerFTMR);
            sb.append(", LCIRequest=" + LCIRequest);
            sb.append(", LCRRequest=" + LCRRequest);
            sb.append(", burstTimeout=" + burstTimeout);
            sb.append(", preamble=" + preamble);
            sb.append(", bandwidth=" + bandwidth);
            return sb.toString();
        }
    }

    /**
     * specifies RTT results
     */
    public static class RttResult {
        /**
         * LCI information Element, only available for double side RTT.
         */
        public WifiInformationElement LCI;
        /**
         * LCR information Element, only available to double side RTT.
         */
        public WifiInformationElement LCR;
        /**
         * mac address of the device being ranged.
         */
        public String bssid;
        /**
         * the duration of this measurement burst, unit ms.
         */
        public int burstDuration;
        /**
         * # of burst for this measurement.
         */
        public int burstNumber;
        /**
         * average distance in cm, computed based on rtt.
         */
        public int distance;
        /**
         * spread (i.e. max - min) distance in cm.
         */
        public int distanceSpread;
        /**
         * standard deviation observed in distance in cm.
         */
        public int distanceStandardDeviation;
        /**
         * average distance in centimeter, computed based on rtt_ns
         *
         * @deprecated use {@link RttManagerCompat.RttResult#distance} API.
         */
        @Deprecated
        public int distance_cm;
        /**
         * standard deviation observed in distance
         *
         * @deprecated Use
         * {@link de.plinzen.rttmanager.RttManagerCompat.RttResult#distanceStandardDeviation} API.
         */
        @Deprecated
        public int distance_sd_cm;
        /**
         * spread (i.e. max - min) distance
         *
         * @deprecated Use {@link RttManagerCompat.RttResult#distanceSpread} API.
         */
        @Deprecated
        public int distance_spread_cm;
        /**
         * Maximum number of frames per burst supported by peer. Two side RTT only
         * Valid only if less than request
         */
        public int frameNumberPerBurstPeer;
        /**
         * total number of measurement frames attempted in this measurement.
         */
        public int measurementFrameNumber;
        /**
         * RTT measurement method type used, should be one of RTT_TYPE_ONE_SIDED or
         * RTT_TYPE_TWO_SIDED.
         */
        public int measurementType;
        /**
         * Burst number supported by peer after negotiation, 2side RTT only
         */
        public int negotiatedBurstNum;
        /**
         * type of the request used
         *
         * @deprecated Use {@link RttManagerCompat.RttResult#measurementType}
         */
        @Deprecated
        public int requestType;
        /**
         * only valid when status ==  RTT_STATUS_FAIL_BUSY_TRY_LATER
         * please retry RTT measurement after this duration since peer indicate busy at ths moment
         * Unit S  Range:1-31
         */
        public int retryAfterDuration;
        /**
         * average RSSI observed, unit of 0.5 dB.
         */
        public int rssi;
        /**
         * RSSI spread (i.e. max - min), unit of 0.5 dB.
         */
        public int rssiSpread;
        /**
         * RSSI spread (i.e. max - min)
         *
         * @deprecated Use {@link RttManagerCompat.RttResult#rssiSpread} API.
         */
        @Deprecated
        public int rssi_spread;
        /**
         * average round trip time in 0.1 nano second.
         */
        public long rtt;
        /**
         * spread (i.e. max - min) RTT in 0.1 ns.
         */
        public long rttSpread;
        /**
         * standard deviation of RTT in 0.1 ns.
         */
        public long rttStandardDeviation;
        /**
         * average round trip time in nano second
         *
         * @deprecated Use {@link RttManagerCompat.RttResult#rtt} API.
         */
        @Deprecated
        public long rtt_ns;
        /**
         * standard deviation observed in round trip time
         *
         * @deprecated Use {@link RttManagerCompat.RttResult#rttStandardDeviation}
         * API.
         */
        @Deprecated
        public long rtt_sd_ns;
        /**
         * spread (i.e. max - min) round trip time
         *
         * @deprecated Use {@link RttManagerCompat.RttResult#rttSpread} API.
         */
        @Deprecated
        public long rtt_spread_ns;
        /**
         * average receiving rate Unit (100kbps).
         */
        public int rxRate;
        /**
         * Whether the secure RTT protocol was used for ranging.
         */
        public boolean secure;
        /**
         * status of the request
         */
        public int status;
        /**
         * total successful number of measurement frames in this measurement.
         */
        public int successMeasurementFrameNumber;
        /**
         * timestamp of completion, in microsecond since boot.
         */
        public long ts;
        /**
         * average transmit rate. Unit (100kbps).
         */
        public int txRate;
        /**
         * average transmit rate
         *
         * @deprecated Use {@link RttManagerCompat.RttResult#txRate} API.
         */
        @Deprecated
        public int tx_rate;

        /**
         * {@hide}
         */
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("bssid=").append(bssid).append(", burstNumber=").append(burstNumber)
                    .append(", measurementFrameNumber=" + measurementFrameNumber)
                    .append(", successMeasurementFrameNumber="
                            + successMeasurementFrameNumber)
                    .append(", frameNumberPerBurstPeer=" + frameNumberPerBurstPeer)
                    .append(", status=" + status)
                    .append(", requestType=" + requestType)
                    .append(", measurementType=" + measurementType)
                    .append(", retryAfterDuration=" + retryAfterDuration)
                    .append(", ts=" + ts)
                    .append(", rssi=" + rssi)
                    .append(", rssi_spread=" + rssi_spread)
                    .append(", rssiSpread=" + rssiSpread)
                    .append(", tx_rate=" + tx_rate)
                    .append(", txRate=" + txRate)
                    .append(", rxRate=" + rxRate)
                    .append(", rtt_ns=" + rtt_ns)
                    .append(", rtt=" + rtt)
                    .append(", rtt_sd_ns=" + rtt_sd_ns)
                    .append(", rttStandardDeviation=" + rttStandardDeviation)
                    .append(", rtt_spread_ns=" + rtt_spread_ns)
                    .append(", rttSpread=" + rttSpread)
                    .append(", distance_cm=" + distance_cm)
                    .append(", distance=" + distance)
                    .append(", distance_sd_cm=" + distance_sd_cm)
                    .append(", distanceStandardDeviation=" + distanceStandardDeviation)
                    .append(", distance_spread_cm=" + distance_spread_cm)
                    .append(", distanceSpread=" + distanceSpread)
                    .append(", burstDuration=" + burstDuration)
                    .append(", negotiatedBurstNum=" + negotiatedBurstNum)
                    .append(", LCI=" + LCI)
                    .append(", LCR=" + LCR)
                    .append(", secure=" + secure);
            return sb.toString();
        }
    }

    public static class WifiInformationElement {
        public byte[] data;
        /**
         * Information Element ID 0xFF means element is invalid.
         */
        public byte id;
    }

    /* private methods @see https://android.googlesource.com/platform/frameworks/base
    .git/+/master/core/java/com/android/internal/util/Protocol.java */
    public static final int BASE = 0x00027200;
    public static final int CMD_OP_ABORTED = BASE + 4;
    public static final int CMD_OP_DISABLE_RESPONDER = BASE + 6;
    public static final int CMD_OP_ENABLE_RESPONDER = BASE + 5;
    public static final int
            CMD_OP_ENALBE_RESPONDER_FAILED = BASE + 8;
    public static final int
            CMD_OP_ENALBE_RESPONDER_SUCCEEDED = BASE + 7;
    public static final int CMD_OP_FAILED = BASE + 2;
    public static final int CMD_OP_START_RANGING = BASE + 0;
    public static final int CMD_OP_STOP_RANGING = BASE + 1;
    public static final int CMD_OP_SUCCEEDED = BASE + 3;
    public static final String DESCRIPTION_KEY = "de.plinzen.rttmanager.RttManagerCompat.Description";
    public static final int PREAMBLE_HT = 0x02;
    /**
     * RTT Preamble Support bit mask
     */
    public static final int PREAMBLE_LEGACY = 0x01;
    public static final int PREAMBLE_VHT = 0x04;
    /**
     * Ranging failed because responder role is enabled in STA mode.
     */
    public static final int
            REASON_INITIATOR_NOT_ALLOWED_WHEN_RESPONDER_ON = -6;
    public static final int REASON_INVALID_LISTENER = -3;
    public static final int REASON_INVALID_REQUEST = -4;
    public static final int REASON_NOT_AVAILABLE = -2;
    /**
     * Do not have required permission
     */
    public static final int REASON_PERMISSION_DENIED = -5;
    public static final int REASON_UNSPECIFIED = -1;
    public static final int RTT_BW_10_SUPPORT = 0x02;
    public static final int RTT_BW_160_SUPPORT = 0x20;
    public static final int RTT_BW_20_SUPPORT = 0x04;
    public static final int RTT_BW_40_SUPPORT = 0x08;
    /**
     * RTT BW supported bit mask, used as RTT param bandWidth too
     */
    public static final int RTT_BW_5_SUPPORT = 0x01;
    public static final int RTT_BW_80_SUPPORT = 0x10;
    /**
     * @deprecated It is not supported anymore.
     * Use {@link RttManagerCompat#RTT_BW_10_SUPPORT} API.
     */
    @Deprecated
    public static final int RTT_CHANNEL_WIDTH_10 = 6;
    /**
     * @deprecated It is not supported anymore.
     * Use {@link RttManagerCompat#RTT_BW_160_SUPPORT} API.
     */
    @Deprecated
    public static final int RTT_CHANNEL_WIDTH_160 = 3;
    /**
     * @deprecated It is not supported anymore.
     * Use {@link RttManagerCompat#RTT_BW_20_SUPPORT} API.
     */
    @Deprecated
    public static final int RTT_CHANNEL_WIDTH_20 = 0;
    /**
     * @deprecated It is not supported anymore.
     * Use {@link RttManagerCompat#RTT_BW_40_SUPPORT} API.
     */
    @Deprecated
    public static final int RTT_CHANNEL_WIDTH_40 = 1;
    /**
     * @deprecated It is not supported anymore.
     * Use {@link RttManagerCompat#RTT_BW_5_SUPPORT} API.
     */
    @Deprecated
    public static final int RTT_CHANNEL_WIDTH_5 = 5;
    /**
     * @deprecated It is not supported anymore.
     * Use {@link RttManagerCompat#RTT_BW_80_SUPPORT} API.
     */
    @Deprecated
    public static final int RTT_CHANNEL_WIDTH_80 = 2;
    /**
     * @deprecated not supported anymore
     */
    @Deprecated
    public static final int RTT_CHANNEL_WIDTH_80P80 = 4;
    /**
     * @deprecated channel info must be specified.
     */
    @Deprecated
    public static final int RTT_CHANNEL_WIDTH_UNSPECIFIED = -1;
    public static final int RTT_PEER_NAN = 5;
    public static final int RTT_PEER_P2P_CLIENT = 4;
    public static final int RTT_PEER_P2P_GO = 3;
    public static final int RTT_PEER_TYPE_AP = 1;
    public static final int RTT_PEER_TYPE_STA = 2;       /* requires NAN */
    /**
     * @deprecated It is not supported anymore.
     */
    @Deprecated
    public static final int RTT_PEER_TYPE_UNSPECIFIED = 0;
    /**
     * Request abort fro uncertain reason
     */
    public static final int RTT_STATUS_ABORTED = 8;
    /**
     * General failure
     */
    public static final int RTT_STATUS_FAILURE = 1;
    /**
     * Destination is on a different channel from the RTT Request
     */
    public static final int RTT_STATUS_FAIL_AP_ON_DIFF_CHANNEL = 6;
    /**
     * destination is busy now, you can try after a specified time from destination
     */
    public static final int RTT_STATUS_FAIL_BUSY_TRY_LATER = 12;
    /**
     * Responder overrides param info, cannot range with new params 2-side RTT only
     */
    public static final int RTT_STATUS_FAIL_FTM_PARAM_OVERRIDE = 15;
    /**
     * The T1-T4 or TOD/TOA Timestamp is illegal
     */
    public static final int RTT_STATUS_FAIL_INVALID_TS = 9;
    /** */
    public static final int RTT_STATUS_FAIL_NOT_SCHEDULED_YET = 4;
    /**
     * This type of Ranging is not support by Hardware
     */
    public static final int RTT_STATUS_FAIL_NO_CAPABILITY = 7;
    /**
     * Destination does not respond to RTT request
     */
    public static final int RTT_STATUS_FAIL_NO_RSP = 2;
    /**
     * 11mc protocol level failed, eg, unrecognized FTMR/FTM frame
     */
    public static final int RTT_STATUS_FAIL_PROTOCOL = 10;
    /**
     * RTT request is rejected by the destination. Double side RTT only
     */
    public static final int RTT_STATUS_FAIL_REJECTED = 3;
    /**
     * Request can not be scheduled by hardware
     */
    public static final int RTT_STATUS_FAIL_SCHEDULE = 11;
    /**
     * Timing measurement timeout
     */
    public static final int RTT_STATUS_FAIL_TM_TIMEOUT = 5;
    /**
     * Bad Request argument
     */
    public static final int RTT_STATUS_INVALID_REQ = 13;
    /**
     * Wifi is not enabled
     */
    public static final int RTT_STATUS_NO_WIFI = 14;
    public static final int RTT_STATUS_SUCCESS = 0;
    /**
     * @deprecated It is not supported anymore.
     */
    @Deprecated
    public static final int RTT_TYPE_11_MC = 4;
    /**
     * @deprecated It is not supported anymore.
     */
    @Deprecated
    public static final int RTT_TYPE_11_V = 2;
    public static final int RTT_TYPE_ONE_SIDED = 1;
    public static final int RTT_TYPE_TWO_SIDED = 2;
    /**
     * @deprecated It is Not supported anymore.
     */
    @Deprecated
    public static final int RTT_TYPE_UNSPECIFIED = 0;
    private static final String METHOD_GET_CAPABILITIES = "getCapabilities";
    private static final String METHOD_GET_RTT_CAPABILITIES = "getRttCapabilities";
    private static final String TAG = RttManagerCompat.class.getSimpleName();
    private static final String WIFI_RTT_SERVICE = "rttmanager";
    private final Context context;
    private final List<ResponderCallbackWrapper> nativeResponderCallbacks;
    private final List<RttListenerWrapper> nativeRttListeners;
    private final Object rttManager;
    private final Class<?> rttManagerClass;

    @SuppressWarnings("WrongConstant")
    public RttManagerCompat(Context context) {
        this.context = context;
        this.rttManager = context.getSystemService(WIFI_RTT_SERVICE);
        rttManagerClass = rttManager.getClass();
        nativeRttListeners = new LinkedList<>();
        nativeResponderCallbacks = new LinkedList<>();
    }

    /**
     * * <b>CURRENTLY THIS FEATURE IS NOT WORKING</b>
     * <p>
     * Disable Wi-Fi RTT responder mode on the device. The {@code callback} needs to be the
     * same one used in {@link #enableResponder(ResponderCallback)}.
     * <p>
     * Calling this method when responder isn't enabled won't have any effect. The callback can be
     * reused for enabling responder after this method is called.
     *
     * @param callback The same callback used for enabling responder.
     * @throws IllegalArgumentException If {@code callback} is null.
     */
    @TargetApi(Build.VERSION_CODES.N)
    public void disableResponder(ResponderCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback is not allowed to be null.");
        }
        ResponderCallbackWrapper selectedNativeCallback = null;
        final Iterator<ResponderCallbackWrapper> it = nativeResponderCallbacks.iterator();
        while (selectedNativeCallback == null && it.hasNext()) {
            final ResponderCallbackWrapper wrapper = it.next();
            if (callback == wrapper.getResponderCallback()) {
                selectedNativeCallback = wrapper;
                it.remove();
            }
        }
        if (selectedNativeCallback == null) {
            return;
        }

        try {
            final Method disableResponder = rttManagerClass.getMethod("disableResponder", Class.forName
                    (RttManagerCompatUtil.CLASS_RESPONDER_CALLBACK));
            disableResponder.invoke(rttManager, selectedNativeCallback);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException
                e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * <b>CURRENTLY THIS FEATURE IS NOT WORKING</b>
     * <p>
     * Enable Wi-Fi RTT responder mode on the device. The enabling result will be delivered via
     * {@code callback}.
     * <p>
     * Note calling this method with the same callback when the responder is already enabled won't
     * change the responder state, a cached {@link ResponderConfig} from the last enabling will be
     * returned through the callback.
     *
     * @param callback Callback for responder enabling/disabling result.
     * @throws IllegalArgumentException If {@code callback} is null.
     */
    @TargetApi(Build.VERSION_CODES.N)
    public void enableResponder(ResponderCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback is not allowed to be null.");
        }
        try {
            final ResponderCallbackWrapper nativeCallback = RttManagerCompatUtil.wrapResponderCallback(callback);
            final Method startRangingMethod = rttManagerClass.getMethod("enableResponder", Class.forName
                    (RttManagerCompatUtil.CLASS_RESPONDER_CALLBACK));
            startRangingMethod.invoke(rttManager, nativeCallback);
            nativeResponderCallbacks.add(nativeCallback);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InstantiationException |
                InvocationTargetException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * @deprecated Use the new {@link RttManagerCompat#getRttCapabilities()} API.
     */
    @Deprecated
    public Capabilities getCapabilities() {
        try {
            final Object nativeCapabilities = rttManagerClass.getMethod(METHOD_GET_CAPABILITIES).invoke(rttManager);
            return RttManagerCompatUtil.buildCapabilitiesFromNativeObject(nativeCapabilities);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public RttCapabilities getRttCapabilities() {
        try {
            final Object nativeCapabilities = rttManagerClass.getMethod(METHOD_GET_RTT_CAPABILITIES).invoke(rttManager);
            return RttManagerCompatUtil.buildRttCapabilitiesFromNativeObject(nativeCapabilities);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Request to start an RTT ranging
     *
     * @param params   -- RTT request Parameters
     * @param listener -- Call back to inform RTT result
     * @throws throw IllegalArgumentException when params are illegal
     *               throw IllegalStateException when RttCapabilities do not exist
     */
    public void startRanging(final RttParams[] params, final RttListener listener) throws Throwable {
        try {
            final Object nativeParams = RttManagerCompatUtil.buildNativeRttParams(params);
            final RttListenerWrapper nativeListener = RttManagerCompatUtil.wrapRttListener(listener);

            final Class<?> listenerClass = Class.forName(RttManagerCompatUtil.CLASS_RTT_LISTENER);
            final Class<?> nativeParamsArrayClass = Array.newInstance(
                    Class.forName(RttManagerCompatUtil.CLASS_RTT_PARAMS), 0).getClass();

            final Method startRangingMethod = rttManagerClass.getMethod("startRanging", nativeParamsArrayClass,
                    listenerClass);
            startRangingMethod.invoke(rttManager, nativeParams, nativeListener);
            nativeRttListeners.add(nativeListener);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException |
                InstantiationException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public void stopRanging(final RttListener listener) {
        if (listener == null) {
            return;
        }
        RttListenerWrapper selectedNativeListener = null;
        final Iterator<RttListenerWrapper> it = nativeRttListeners.iterator();
        while (selectedNativeListener == null && it.hasNext()) {
            final RttListenerWrapper wrapper = it.next();
            if (listener == wrapper.getRttListenerCompat()) {
                selectedNativeListener = wrapper;
                it.remove();
            }
        }
        if (selectedNativeListener == null) {
            return;
        }
        try {
            final Class<?> listenerClass = Class.forName(RttManagerCompatUtil.CLASS_RTT_LISTENER);
            final Method stopRangingMethod = rttManagerClass.getMethod("stopRanging", listenerClass);
            stopRangingMethod.invoke(rttManager, selectedNativeListener);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException
                e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }


}