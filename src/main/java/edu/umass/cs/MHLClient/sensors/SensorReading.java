package edu.umass.cs.MHLClient.sensors;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Base class for a sensor reading. You may use existing implementations such
 * as {@link AccelerometerReading} or, if your sensing modality is not available,
 * subclass {@link SensorReading} in your own custom reading. To do so, you must
 * only define the {@link #toJSONObject()} method.
 *
 * @author Erik Risinger
 *
 * @see JSONObject
 * @see AccelerometerReading
 * @see GyroscopeReading
 * @see RSSIReading
 */
@SuppressWarnings("unused")
public abstract class SensorReading {

    /** A 10-byte hex string identifying the current user. **/
    protected final String userID;

    /** Describes the device **/
    protected final String deviceType;

    /** Unique string identifying the device. **/
    protected final String deviceID;

    /** Identifies the sensor type. **/
    protected final String sensorType;

    /** Indicates when the sensor reading occurred. **/
    protected final long timestamp;

    /** Indicates the label, -1 indicates that no label is available **/
    protected final int label;

    /**
     * Instantiates a sensor reading object. Because {@link SensorReading} is
     * abstract, this should only be called by subclasses for initializing
     * metadata common across sensing modalities.
     *
     * @param userID a 10-byte hex string identifying the current user.
     * @param deviceType describes the device.
     * @param deviceID TODO
     * @param sensorType identifies the sensor type.
     * @param timestamp indicates when the sensor reading occurred, in Unix time by convention.
     */
    protected SensorReading(String userID, String deviceType, String deviceID, String sensorType, long timestamp){
        this.userID = userID;
        this.deviceType = deviceType;
        this.deviceID = deviceID;
        this.sensorType = sensorType;
        this.timestamp = timestamp;
        this.label = -1;
    }

    /**
     * Instantiates a sensor reading object. Because {@link SensorReading} is
     * abstract, this should only be called by subclasses for initializing
     * metadata common across sensing modalities.
     *
     * @param userID a 10-byte hex string identifying the current user.
     * @param deviceType describes the device.
     * @param deviceID TODO
     * @param sensorType identifies the sensor type.
     * @param timestamp indicates when the sensor reading occurred, in Unix time by convention.
     */
    protected SensorReading(String userID, String deviceType, String deviceID, String sensorType, long timestamp, int label){
        this.userID = userID;
        this.deviceType = deviceType;
        this.deviceID = deviceID;
        this.sensorType = sensorType;
        this.timestamp = timestamp;
        this.label = label;
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
     * @return a String representing the device type.
     */
    public String getDeviceType(){
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
    protected abstract JSONObject toJSONObject();

    /**
     * Returns a base JSON object with the sensor reading metadata already encoded.
     * The 'data' object is left undefined and should be specified by subclasses
     * that use this method.
     * @return a JSON object encoding the metadata.
     */
    protected JSONObject getBaseJSONObject(){
        JSONObject obj = new JSONObject();
        JSONObject device = new JSONObject();

        try {
            device.put("device_type", deviceType);
            device.put("device_id", deviceID);
            obj.put("user_id", userID);
            obj.put("device_type", deviceType);
            obj.put("device", device);
            obj.put("sensor_type", sensorType);
            if (label != -1){
                obj.put("label", label);
            }
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
