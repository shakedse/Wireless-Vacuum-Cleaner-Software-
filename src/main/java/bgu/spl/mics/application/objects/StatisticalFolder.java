package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.mics.MessageBusImpl;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    private static final StatisticalFolder instance = new StatisticalFolder();
    private int systemRunTime;
    private int numDetectedObjects;
    private int numTrackedObjects;
    private int numLandmarks;

    public StatisticalFolder()
    {
        systemRunTime = 0;
        numDetectedObjects = 0;
        numTrackedObjects = 0;
        numLandmarks = 0;
    }
    public static StatisticalFolder getInstance() {
        return instance;
    }

    // Synchronized method to increment systemRunTime
    public synchronized void incrementSystemRunTime() {
        systemRunTime++;
    }

    // Synchronized method to increment numDetectedObjects
    public synchronized void incrementNumDetectedObjects() {
        numDetectedObjects++;
    }

    // Synchronized method to increment numTrackedObjects
    public synchronized void incrementNumTrackedObjects() {
        numTrackedObjects++;
    }

    // Synchronized method to increment numLandmarks
    public synchronized void incrementNumLandmarks() {
        numLandmarks++;
    }
}
