package edu.umass.cs.MHLClient.sensors;

import org.json.JSONException;
import org.json.JSONObject;

import edu.umass.cs.MHLClient.devices.DeviceType;

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
    private int rssi;

    /**
     * Instantiates an RSSI reading.
     * @param userID a 10-byte hex string identifying the current user.
     * @param deviceType identifies the device, as defined in {@link DeviceType}.
     * @param t the timestamp at which the event occurred, in Unix time by convention.
     * @param rssi the rssi reading
     */
    public RSSIReading(String userID, DeviceType deviceType, long t, int rssi){
        super(userID, deviceType, "SENSOR_RSSI", t);

        this.rssi = rssi;
    }

    @Override
    protected JSONObject toJSONObject() {
        JSONObject data = getBaseJSONObjet();
        JSONObject obj = new JSONObject();

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
