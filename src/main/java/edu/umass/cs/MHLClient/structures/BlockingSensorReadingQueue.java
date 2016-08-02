package edu.umass.cs.MHLClient.structures;

import java.util.concurrent.ArrayBlockingQueue;

import edu.umass.cs.MHLClient.sensors.SensorReading;

/**
 * This class is a blocking queue of {@link SensorReading sensor readings}. It
 * has no important functionality but to limit the capacity of the blocking queue.
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
