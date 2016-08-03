package edu.umass.cs.MHLClient.client;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import edu.umass.cs.MHLClient.structures.BlockingSensorReadingQueue;
import edu.umass.cs.MHLClient.sensors.SensorReading;

/**
 * The Mobile IO client is responsible for handling the server connection
 * and sending data to the server. To connect to the server, instantiate
 * a Mobile IO client instance and call {@link #connect()}. If you register
 * a {@link ConnectionStateHandler} using the
 * {@link #setConnectionStateHandler(ConnectionStateHandler)} method, then
 * you can handle the {@link ConnectionStateHandler#onConnected()} and
 * {@link ConnectionStateHandler#onConnectionFailed()} events.
 *
 * @author Erik Risinger
 */
@SuppressWarnings("unused")
public class MobileIOClient {

    /** used for debugging purposes */
    private static final String TAG = MobileIOClient.class.getName();

    /** The blocking queue containing the sensor data. **/
    private volatile BlockingSensorReadingQueue sensorReadingQueue;

    /** The user ID associated with the user establishing the connection **/
    private final String userID;

    /**
     * The connection state handler is notified when the connection to the server is successfully
     * established or when the connection attempt has failed.
     */
    private ConnectionStateHandler connectionStateHandler;

    /**
     * The default connection timeout.
     * @see #connectionTimeoutMillis
     */
    private static final int DEFAULT_CONNECTION_TIMEOUT_MILLIS = 5000;

    /**
     * The number of milliseconds after which a server connection attempt ends and the
     * {@link ConnectionStateHandler#onConnectionFailed()} method is called.
     */
    private int connectionTimeoutMillis;

    /** The web socket to the server. **/
    private final Socket socket;

    /**
     * Indicates whether the server connection has been established
     */
    private boolean started = false; //TODO: This isn't used?

    /**
     * The IP address of the server
     */
    private final String ip;

    /**
     * The port of the server
     */
    private final int port;

    /**
     * Creates a mobile IO client with a pre-existing (external) blocking queue and
     * a user-defined connection timeout.
     * @param q a blocking queue containing sensor reading objects
     * @param ip the IP address of the server
     * @param port the port of the server
     * @param id the user ID required to validate the connection
     * @param connectionTimeoutMillis The number of milliseconds after which the connection fails.
     */
    public MobileIOClient(final BlockingSensorReadingQueue q, final String ip, final int port, final String id, final int connectionTimeoutMillis){
        this.sensorReadingQueue = q;
        this.userID = id;
        this.ip = ip;
        this.port = port;
        this.connectionTimeoutMillis = connectionTimeoutMillis;
        socket = new Socket();
    }

    /**
     * Creates a mobile IO client with a self-contained blocking queue and
     * a user-defined connection timeout.
     * @param ip the IP address of the server
     * @param port the port of the server
     * @param id the user ID required to validate the connection
     * @param connectionTimeoutMillis The number of milliseconds after which the connection fails.
     */
    public MobileIOClient(final String ip, final int port, final String id, final int connectionTimeoutMillis){
        this(new BlockingSensorReadingQueue(), ip, port, id, connectionTimeoutMillis);
    }

    /**
     * Creates a mobile IO client with a pre-existing (external) blocking queue.
     * @param q a blocking queue containing sensor reading objects
     * @param ip the IP address of the server
     * @param port the port of the server
     * @param id the user ID required to validate the connection
     */
    public MobileIOClient(final BlockingSensorReadingQueue q, final String ip, final int port, final String id){
        this(q, ip, port, id, DEFAULT_CONNECTION_TIMEOUT_MILLIS);
    }

    /**
     * Creates a mobile IO client with a self-contained blocking queue.
     * @param ip the IP address of the server
     * @param port the port of the server
     * @param id the user ID required to validate the connection
     */
    public MobileIOClient(final String ip, final int port, final String id){
        this(new BlockingSensorReadingQueue(), ip, port, id, DEFAULT_CONNECTION_TIMEOUT_MILLIS);
    }

    /**
     * Sets the server connection state handler for handling connection and connection failed events.
     * @param connectionStateHandler defines how events are handled.
     * @see ConnectionStateHandler#onConnected()
     * @see ConnectionStateHandler#onConnectionFailed()
     */
    public void setConnectionStateHandler(ConnectionStateHandler connectionStateHandler){
        this.connectionStateHandler = connectionStateHandler;
    }

    /**
     * Sends a sensor reading to the server.
     * @param reading Any subclass of {@link SensorReading}, e.g. {@link edu.umass.cs.MHLClient.sensors.AccelerometerReading} or a custom reading
     * @return true if the reading was successfully queued for transmission to the server, false otherwise
     */
    public boolean sendSensorReading(SensorReading reading){
        if (!socket.isConnected() || socket.isClosed())
            connect();

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
                started = true;
                TransmissionThread transmissionThread;
                ConsumptionThread consumptionThread;

                System.out.println("STARTING SENSOR THREAD");

                try {
                    System.out.println("connecting to server: " + ip + ":" + port);
                    socket.connect(new InetSocketAddress(ip, port), connectionTimeoutMillis);
                    System.out.println("connected");
                }
                catch (Exception e){
                    e.printStackTrace();
                    if (connectionStateHandler != null)
                        connectionStateHandler.onConnectionFailed();
                    return;
                }

                //connection successful -- launch transmission thread
                transmissionThread = new TransmissionThread(socket);
                new Thread(transmissionThread).start();

                //launch notification consumption thread
                consumptionThread = new ConsumptionThread(socket);
                new Thread(consumptionThread).start();
            }
        }).start();
    }

    /**
     * A transmission thread is responsible for sending data to the server.
     * It must be initialized with a valid open socket.
     */
    private class TransmissionThread implements Runnable {
        private Socket clientSocket;
        private BufferedWriter output;
        private BufferedReader input;

        public TransmissionThread(Socket clientSocket){
            this.clientSocket = clientSocket;

            try {
                this.output = new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()));
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        public void run(){
            ArrayList<SensorReading> latestReadings;

            //connect to data collection server (DCS), return on failed handshake
//            System.out.println("calling connectToServer()");
            this.connectToServer();
//            System.out.println("called connectToServer()");
//            if (!running) return;

            //transmit data continuously until stopped
            while (!Thread.currentThread().isInterrupted()){
                //auto reconnect in case of interruption
//                if (!running) this.connectToServer();
//                System.out.println("inside while");
                try {
                    latestReadings = new ArrayList<>();
                    sensorReadingQueue.drainTo(latestReadings);

                    for (int i = latestReadings.size() - 1; i >= 0; i--){
                        SensorReading reading = latestReadings.get(i);
                        output.write(reading.toJSONString() + "\n");
                        output.flush();
                    }
                    Thread.sleep(10);
                } catch (IOException | InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
        private void connectToServer(){
            try {

                System.out.println("connectToServer()");

                //send user ID
                output.write("ID," + userID + "\n");
                output.flush();

                //read in ACK
                String ackString = input.readLine();
                String[] ack = ackString.split(",");

                System.out.println(ackString);

                //expecting "ACK" with user ID echoed back as CSV string, e.g.: "ACK,0"
                if (!("ACK".equals(ack[0]) && ack[1].equals(userID))){
                    System.out.println("failed to receive correct ACK from DCS");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A consumption thread is responsible for receiving data, e.g. messages, from the server.
     * It must be initialized with a valid open socket.
     */
    private class ConsumptionThread implements Runnable {
        private Socket socket;
        private BufferedReader input;

        public ConsumptionThread(Socket socket){
            this.socket = socket;
        }

        public void run(){
            try {

                String inputLine;
                this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

                while ((inputLine = input.readLine()) != null){
                    Log.d("received notification: ", inputLine);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
