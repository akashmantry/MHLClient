package edu.umass.cs.MHLClient.sensors;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Wraps a GPS reading and defines a JSON structure that allows
 * the reading to be sent to the server.
 *
 * @author Erik Risinger
 *
 * @see SensorReading
 */
public class GPSReading extends SensorReading {

    /** The latitude of the GPS reading **/
    private final double latitude;

    /** The longitude of the GPS reading **/
    private final double longitude;

    /**
     * Instantiates a GPS reading.
     * @param userID a 10-byte hex string identifying the current user.
     * @param deviceType describes the device
     * @param deviceID unique device identifier
     * @param t the timestamp at which the event occurred, in Unix time by convention.
     * @param latitude the latitude of the GPS reading
     * @param longitude the longitude of the GPS reading
     */
    public GPSReading(String userID, String deviceType, String deviceID, long t, double latitude, double longitude){
        super(userID, deviceType, deviceID, "SENSOR_GPS", t);

        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    protected JSONObject toJSONObject(){
        JSONObject obj = getBaseJSONObject();
        JSONObject data = new JSONObject();

        try {
            data.put("t", timestamp);
            data.put("latitude", latitude);
            data.put("longitude", longitude);

            obj.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
