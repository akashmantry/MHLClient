package edu.umass.cs.MHLClient.sensors;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Wraps an accelerometer reading and defines a JSON structure that allows
 * the reading to be sent to the server.
 *
 * @author Erik Risinger
 *
 * @see SensorReading
 */
public class AccelerometerReading extends SensorReading {

    /** The acceleration along the x-axis **/
    private final double x;

    /** The acceleration along the y-axis **/
    private final double y;

    /** The acceleration along the z-axis **/
    private final double z;

    /**
     * Instantiates an accelerometer reading.
     * @param userID a 10-byte hex string identifying the current user.
     * @param deviceType describes the device
     * @param deviceID unique device identifier
     * @param t the timestamp at which the event occurred, in Unix time by convention.
     * @param values the x, y, z readings
     */
    public AccelerometerReading(String userID, String deviceType, String deviceID, long t, float... values){
        super(userID, deviceType, deviceID, "SENSOR_ACCEL", t);

        this.x = values[0];
        this.y = values[1];
        this.z = values[2];
    }

    /**
     * Instantiates an accelerometer reading.
     * @param userID a 10-byte hex string identifying the current user.
     * @param deviceType describes the device.
     * @param deviceID unique device identifier.
     * @param t the timestamp at which the event occurred, in Unix time by convention.
     * @param label the class label associated with the reading.
     * @param values the x, y, z readings.
     */
    public AccelerometerReading(String userID, String deviceType, String deviceID, long t, int label, float... values){
        super(userID, deviceType, deviceID, "SENSOR_ACCEL", t, label);

        this.x = values[0];
        this.y = values[1];
        this.z = values[2];
    }

    @Override
    protected JSONObject toJSONObject(){
        JSONObject obj = getBaseJSONObject();
        JSONObject data = new JSONObject();

        try {
            data.put("t", timestamp);
            data.put("x", x);
            data.put("y", y);
            data.put("z", z);

            obj.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
