package edu.umass.cs.MHLClient.client;

/**
 * Defines methods that handle the server connection state events,
 * e.g. when the server is connected or the connection attempt fails.
 *
 * @author Sean Noran
 */
public interface ConnectionStateHandler {
    void onConnected();
    void onConnectionFailed();
}