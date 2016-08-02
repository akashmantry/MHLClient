package edu.umass.cs.MHLClient.sensors;

import org.json.JSONException;
import org.json.JSONObject;

import edu.umass.cs.MHLClient.devices.DeviceType;

/**
 * Base class for a sensor reading. You may use existing implementations such
 * as {@link AccelerometerReading} or, if your sensing modality is not available,
 * subclass {@link SensorReading} in your own custom reading. To do so, you must
 * only define the {@link #toJSONObject()} method.
 *
 * @author Erik Risinger
 *
 * @see JSONObject
 * @see DeviceType
 * @see AccelerometerReading
 * @see GyroscopeReading
 * @see RSSIReading
 */
@SuppressWarnings("unused")
public abstract class SensorReading {

    /** A 10-byte hex string identifying the current user. **/
    protected String userID;

    /** Identifies the device, as defined in {@link DeviceType}. **/
    protected DeviceType deviceType;

    /** Identifies the sensor type. **/
    protected String sensorType;

    /** Indicates when the sensor reading occurred. **/
    protected long timestamp;

    /**
     * Instantiates a sensor reading object. Because {@link SensorReading} is
     * abstract, this should only be called by subclasses for initializing
     * metadata common across sensing modalities.
     *
     * @param userID a 10-byte hex string identifying the current user.
     * @param deviceType identifies the device, as defined in {@link DeviceType}.
     * @param sensorType identifies the sensor type.
     * @param timestamp indicates when the sensor reading occurred, in Unix time by convention.
     */
    protected SensorReading(String userID, DeviceType deviceType, String sensorType, long timestamp){
        this.userID = userID;
        this.deviceType = deviceType;
        this.sensorType = sensorType;
        this.timestamp = timestamp;
    }

    /**
     * Gets the user identifier.
     * @return a 10-byte hex string identifying the current user.
     */
    public String getUserID(){
        return userID;
    }

    /**
     * Gets the device type associated with the sensor reading.
     * @return a device type as defined in {@link DeviceType}.
     */
    public DeviceType getDeviceType(){
        return deviceType;
    }

    /**
     * Gets the sensor modality associated with the sensor reading.
     * @return a String representing the sensor type.
     */
    public String getSensorType(){
        return sensorType;
    }

    /**
     * Gets the timestamp at which the sensor reading occurred.
     * @return a timestamp, in Unix time by convention.
     */
    public long getTimestamp(){
        return timestamp;
    }

    /**
     * Defines how the data is converted to a JSON object.
     * @return a JSON object encoding the sensor reading.
     */
    public abstract JSONObject toJSONObject();

    /**
     * Returns a base JSON object with the sensor reading metadata already encoded.
     * The 'data' object is left undefined and should be specified by subclasses
     * that use this method.
     * @return a JSON object encoding the metadata.
     */
    protected JSONObject getBaseJSONObjet(){
        JSONObject obj = new JSONObject();

        try {
            obj.put("user_id", userID);
            obj.put("device_type", deviceType);
            obj.put("sensor_type", sensorType);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    /**
     * Converts the JSON object into a transmission-ready string.
     * @return a string form of the JSON object.
     */
    public String toJSONString(){
        return this.toJSONObject().toString();
    }
}
