package bgu.spl.mics.application.services;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;

import java.util.LinkedList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {
    private static int ThraedNum;
    private final Camera camera;
    private int time;
    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("cameraService" + camera.getID());
        ThraedNum++;
        this.camera = camera;
        this.time = 0;
        // TODO Implement this
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        // TODO Implement this

        subscribeBroadcast(TickBroadcast.class ,(TickBroadcast tick) ->{
            time = tick.getTick() + camera.getFrequency(); // why time exiets
            LinkedList<DetectedObject> detectedObjects = camera.getDetectedObjectsAtTick(time);
            if(!detectedObjects.isEmpty())
            {
                DetectObjectsEvent event = new DetectObjectsEvent(time, detectedObjects);
                sendEvent(event); //returns Future???
                System.out.println("TickBroadcast" + event);
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class ,(TerminatedBroadcast terminate) ->{
            if(terminate.getTerminated().getClass() == TimeService.class)
            {
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class ,Call ->{
            
        });
        
    }
}
