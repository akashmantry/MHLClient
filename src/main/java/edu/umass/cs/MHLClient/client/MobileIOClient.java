package edu.umass.cs.MHLClient.client;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import org.apache.http.conn.ssl.SSLSocketFactory;

import javax.net.ssl.SSLSocket;

import cs.umass.edu.MHLClient.R;
import edu.umass.cs.MHLClient.structures.BlockingSensorReadingQueue;
import edu.umass.cs.MHLClient.sensors.SensorReading;

/**
 * The Mobile IO client is responsible for handling the server connection
 * and sending data to the server. To connect to the server, instantiate
 * a Mobile IO client instance and call {@link #connect()}. If you register
 * a {@link ConnectionStateHandler} using the
 * {@link #setConnectionStateHandler(ConnectionStateHandler)} method, then
 * you can handle the {@link ConnectionStateHandler#onConnected()} and
 * {@link ConnectionStateHandler#onConnectionFailed(Exception)} events.
 *
 * @author Erik Risinger
 */
public class MobileIOClient {

    @SuppressWarnings("unused")
    /** Used for debugging purposes */
    private static final String TAG = MobileIOClient.class.getName();

    /** The blocking queue containing the sensor data. **/
    private volatile BlockingSensorReadingQueue sensorReadingQueue;

    /** The 10-byte hex ID associated with the user establishing the connection. **/
    private final String userID;

    /**
     * The connection state handler is notified when the connection to the server is successfully
     * established or when the connection attempt has failed.
     */
    private ConnectionStateHandler connectionStateHandler;

    /**
     * The list of message receivers notified when data is received from the server.
     */
    private ArrayList<MessageReceiver> messageReceivers;

    /** The socket to the server. **/
    private SSLSocket socket;

    /**
     * The IP address of the server.
     */
    private static final String ip = "none.cs.umass.edu"; //"192.168.24.58"; //"none.cs.umass.edu";

    /**
     * The port on the server listening for incoming data.
     */
    private static final int port = 9997;

    /**
     * Singleton instance.
     */
    private static MobileIOClient instance;

    /**
     * Thread responsible for transmitting data to the server.
     */
    private Thread transmissionThread;

    /**
     * Thread responsible for receiving data from the server.
     */
    private Thread consumptionThread;

    /**
     * Context required to access resources, i.e. keystore.
     */
    private Context context;

    /**
     * Receives data from the server.
     */
    private BufferedReader input;

    /**
     * Writes data to the server.
     */
    private BufferedWriter output;

    /**
     * Creates a singleton mobile IO client instance with a pre-existing (external)
     * blocking queue and a user-defined connection timeout.
     * @param context the context to access application resources.
     * @param q a blocking queue containing sensor reading objects
     * @param id the user ID required to validate the connection
     */
    private MobileIOClient(final Context context, final BlockingSensorReadingQueue q, final String id){
        this.sensorReadingQueue = q;
        this.userID = id;
        messageReceivers = new ArrayList<>();
        instance = this;
        this.context = context;
    }

    /**
     * Creates a mobile IO client with a pre-existing (external) blocking queue.
     * @param q a blocking queue containing sensor reading objects
     * @param id the user ID required to validate the connection
     */
    public static MobileIOClient getInstance(final Context context, final BlockingSensorReadingQueue q, final String id){
        if (instance == null){
            return new MobileIOClient(context, q, id);
        }
        return instance;
    }

    /**
     * Creates a mobile IO client with a self-contained blocking queue.
     * @param id the user ID required to validate the connection
     */
    public static MobileIOClient getInstance(final Context context, final String id){
        if (instance == null){
            return new MobileIOClient(context, new BlockingSensorReadingQueue(), id);
        }
        return instance;
    }

    /**
     * Sets the server connection state handler for handling connection and connection failed events.
     * @param connectionStateHandler defines how events are handled.
     * @see ConnectionStateHandler#onConnected()
     * @see ConnectionStateHandler#onConnectionFailed(Exception)
     */
    public void setConnectionStateHandler(ConnectionStateHandler connectionStateHandler){
        this.connectionStateHandler = connectionStateHandler;
    }

    /**
     * Registers a message handler for handling messages received from the server.
     * @param messageReceiver defines how incoming messages are handled.
     * @see MessageReceiver#onMessageReceived(JSONObject)
     */
    public void registerMessageReceiver(MessageReceiver messageReceiver){
        this.messageReceivers.add(messageReceiver);
    }

    /**
     * Unregisters the given message handler.
     * @param messageReceiver reference to the message handler
     */
    public void unregisterMessageReceiver(MessageReceiver messageReceiver){
        this.messageReceivers.remove(messageReceiver);
    }

    /**
     * Unregisters all message handlers.
     */
    public void unregisterMessageReceivers(){
        this.messageReceivers.clear();
    }

    /**
     * Sends a sensor reading to the server.
     * @param reading Any subclass of {@link SensorReading}, e.g. {@link edu.umass.cs.MHLClient.sensors.AccelerometerReading} or a custom reading
     * @return true if the reading was successfully queued for transmission to the server, false otherwise
     */
    public boolean sendSensorReading(SensorReading reading){
        return sensorReadingQueue.offer(reading);
    }

    /**
     * Establishes a connection to the server. Call
     * {@link #setConnectionStateHandler(ConnectionStateHandler)} first to register
     * a {@link ConnectionStateHandler} if you wish to handle the
     * connection and connection failed events.
     */
    public void connect(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                if (socket == null || !socket.isConnected() || socket.isClosed()) {
                    try {
                        KeyStore ks = KeyStore.getInstance("BKS");
                        InputStream keyin = context.getResources().openRawResource(R.raw.serverkeys);
                        ks.load(keyin, "password".toCharArray());
                        SSLSocketFactory socketFactory = new SSLSocketFactory(ks);
                        socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                        socket = (SSLSocket) socketFactory.createSocket(new Socket(ip, port), ip, port, false);
                        socket.startHandshake();
                    }catch( IOException | KeyStoreException |
                            UnrecoverableKeyException | CertificateException |
                            KeyManagementException | NoSuchAlgorithmException e){

                        e.printStackTrace();
                    }

                    Log.i(TAG, "STARTING SENSOR THREAD");

                    if (connectToServer()) {
                        //connection successful -- launch transmission thread
                        final TransmissionRunnable transmissionRunnable = new TransmissionRunnable();
                        transmissionThread = new Thread(transmissionRunnable);
                        transmissionThread.start();

                        //launch notification consumption thread
                        final ConsumptionRunnable consumptionRunnable = new ConsumptionRunnable();
                        consumptionThread = new Thread(consumptionRunnable);
                        consumptionThread.start();

                        Log.i(TAG, "Connected to server.");
                    }else {
                        Log.i(TAG, "Connection failed.");
                        return;
                    }
                } else {
                    Log.i(TAG, "Already connected to server.");
                }
                if (connectionStateHandler != null)
                    connectionStateHandler.onConnected();
            }
        }).start();
    }

    /**
     * Authenticates the user with a handshake.
     */
    private boolean connectToServer(){
        Log.i(TAG, "connectToServer()");

        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String handshake = input.readLine();
            if (handshake == null || !handshake.equals("ID")){
                Log.w(TAG, "Handshake failed.");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String outString = "ID," + userID + "\n";
        Log.i(TAG, outString);

        //send user ID
        try {
            output.write(outString);
            output.flush();
        }catch (IOException e){
            e.printStackTrace();
            if (connectionStateHandler != null) {
                connectionStateHandler.onConnectionFailed(e);
            }
            return false;
        }

        //read in ACK
        String ackString;
        try {
            ackString = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            if (connectionStateHandler != null) {
                connectionStateHandler.onConnectionFailed(e);
            }
            return false;
        }
        if (ackString == null){
            Log.e(TAG, "ACK is null.");
            if (connectionStateHandler != null) {
                connectionStateHandler.onConnectionFailed(new AuthenticationException());
            }
            return false;
        }
        String[] ack = ackString.split(",");

        Log.i(TAG, "Ack string: " + ackString);
        Log.i(TAG, "User ID: " + userID);

        //expecting "ACK" with user ID echoed back as CSV string, e.g.: "ACK,0"
        if (!("ACK".equals(ack[0]) && ack[1].equals(userID))){
            if (connectionStateHandler != null) {
                connectionStateHandler.onConnectionFailed(new AuthenticationException());
            }
            return false;
        }
        return true;
    }

    /**
     * Closes the socket connection.
     */
    public void disconnect(){
        Log.i(TAG, "disconnect()");
        transmissionThread.interrupt();
        consumptionThread.interrupt();
        sensorReadingQueue.clear();
    }

    /**
     * A transmission thread is responsible for sending data to the server.
     * It must be initialized with a valid open socket.
     */
    private class TransmissionRunnable implements Runnable {
        public TransmissionRunnable(){}
        public void run(){
            ArrayList<SensorReading> latestReadings;

            //transmit data continuously until stopped
            //auto reconnect in case of interruption
//          if (!running) this.connectToServer();
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    latestReadings = new ArrayList<>();
                    sensorReadingQueue.drainTo(latestReadings);

                    for (int i = 0; i < latestReadings.size(); i++) {
                        SensorReading reading = latestReadings.get(i);
                        Log.d(TAG, "Sending data to server: " + reading.toJSONString());
                        output.write(reading.toJSONString() + "\n");
                        output.flush();
                    }
                    Thread.sleep(10);
                }
            } catch (IOException | InterruptedException e){
                e.printStackTrace();
            } finally {
//                try {
//                    Log.i(TAG, "Sending null character to request termination...");
//                    output.write("\0\n");
//                    output.flush();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    socket = null;
                }
            }
        }
    }

    /**
     * A consumption thread is responsible for receiving data, e.g. messages, from the server.
     * It must be initialized with a valid open socket.
     */
    private class ConsumptionRunnable implements Runnable {
        public void run(){
            try {
                String json=null;

                while (!Thread.currentThread().isInterrupted() && (json = input.readLine()) != null){
                    Log.i("received notification: ", json);
                    try {
                        JSONObject obj = new JSONObject(json);
                        String sensorType = obj.getString("sensor_type");
                        Log.i(TAG, "Notification has sensor type " + sensorType);
                        if (sensorType.equals("SENSOR_SERVER_MESSAGE")) {
                            String message = obj.getString("message");
                            for (MessageReceiver receiver : messageReceivers) {
                                if (receiver.checkPath(message))
                                    receiver.onMessageReceived(obj);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (json == null) {
                    Log.i(TAG, "Received null character. Terminating consumption thread...");
                } else {
                    Log.i(TAG, "Thread interrupted. Terminating consumption thread...");
                }

            } catch (IOException e) {
                Log.e(TAG, "IO Error receiving data from server.");
                e.printStackTrace();
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "Consumption thread terminated.");
            }
        }
    }
}
