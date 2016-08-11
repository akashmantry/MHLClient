package edu.umass.cs.MHLClient.client;

/**
 * The message receiver interface defines how to handle messages that
 * are received from the server.
 *
 * @author Sean Noran
 */
public interface MessageReceiver {
    /**
     * Defines how messages received from the server should be handled.
     * @param json the message, conventionally a JSON string object
     */
    void onMessageReceived(String json);
}
