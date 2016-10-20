package edu.umass.cs.MHLClient.client;

import org.json.JSONObject;

/**
 * The message receiver interface defines how to handle messages that
 * are received from the server. Only data that match the specified filter
 * will be received. When instantiating a message receiver object, you may
 * supply any number of filters, separated by a comma or in a {@link String}
 * array.
 *
 * @author Sean Noran
 */
public abstract class MessageReceiver {

    /**
     * Specifies a filter for the types of messages of interest. All message objects
     * whose path starts with pathFilter will be sent to {@link #onMessageReceived(JSONObject)}.
     **/
    private String[] sensorFilters;

    /**
     * Creates a message receiver for handling data from the server, allowing only
     * data that matches the specified filters. If no data filters are specified,
     * then all data will be permitted. For performance and modular design purposes,
     * it is recommended that at least one non-empty string filter is provided.
     * @param sensorFilters any number of strings separated by comma or a {@link String} array.
     */
    public MessageReceiver(String... sensorFilters){
        this.sensorFilters = sensorFilters;
        //in the case no filter is specified, allow everything through by including the empty string filter
        if (this.sensorFilters.length == 0)
            this.sensorFilters = new String[]{""};
    }

    /**
     * Checks whether the specified path is allowed by the sensor filter array.
     * @return true if path starts with any of the sensor filters, false otherwise.
     */
    boolean checkPath(String path){
        for (String filter : sensorFilters){
            if (path.startsWith(filter)){
                return true;
            }
        }
        return false;
    }

    /**
     * Defines how messages received from the server should be handled.
     * @param json the JSON object containing the message data.
     */
    protected abstract void onMessageReceived(JSONObject json);
}