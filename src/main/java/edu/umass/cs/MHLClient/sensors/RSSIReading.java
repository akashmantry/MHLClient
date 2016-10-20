package edu.umass.cs.MHLClient.sensors;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Wraps a received signal strength indicator (RSSI) reading and defines a
 * JSON structure that allows the reading to be sent to the server.
 *
 * @author Erik Risinger
 *
 * @see SensorReading
 */
public class RSSIReading extends SensorReading {

    /** The RSSI reading. **/
    private final int rssi;

    /**
     * Instantiates an RSSI reading.
     * @param userID a 10-byte hex string identifying the current user.
     * @param deviceType describes the device.
     * @param deviceID unique device identifier.
     * @param t the timestamp at which the event occurred, in Unix time by convention.
     * @param rssi the rssi reading.
     */
    public RSSIReading(String userID, String deviceType, String deviceID, long t, int rssi){
        super(userID, deviceType, deviceID, "SENSOR_RSSI", t);
        this.rssi = rssi;
    }

    /**
     * Instantiates an RSSI reading.
     * @param userID a 10-byte hex string identifying the current user.
     * @param deviceType describes the device.
     * @param deviceID unique device identifier.
     * @param t the timestamp at which the event occurred, in Unix time by convention.
     * @param label the class label associated with the reading.
     * @param rssi the rssi reading.
     */
    public RSSIReading(String userID, String deviceType, String deviceID, long t, int label, int rssi){
        super(userID, deviceType, deviceID, "SENSOR_RSSI", t, label);
        this.rssi = rssi;
    }

    @Override
    protected JSONObject toJSONObject() {
        JSONObject obj = getBaseJSONObject();
        JSONObject data = new JSONObject();

        try {
            data.put("t", timestamp);
            data.put("rssi", rssi);

            obj.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
