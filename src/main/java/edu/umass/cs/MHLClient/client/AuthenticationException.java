package edu.umass.cs.MHLClient.client;

/**
 * Exception thrown when authentication fails.
 *
 * @author Sean Noran
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(){
        super("Could not authenticate user. Reason : failed to receive correct ACK from DCS.");
    }
}
