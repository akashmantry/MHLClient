package edu.umass.cs.MHLClient.structures;

import java.util.concurrent.ArrayBlockingQueue;

import edu.umass.cs.MHLClient.sensors.SensorReading;

/**
 * This class is a blocking queue of {@link SensorReading sensor readings}.
 * It defines a queue of capacity {@link #QUEUE_CAPACITY} containing sensor readings
 * that will be sent to the server.
 *
 * @author Erik Risinger
 *
 * @see ArrayBlockingQueue
 * @see SensorReading
 */
public class BlockingSensorReadingQueue extends ArrayBlockingQueue<SensorReading> {
    private static final int QUEUE_CAPACITY = 5000;
    public BlockingSensorReadingQueue(){
        super(QUEUE_CAPACITY);
    }
}
