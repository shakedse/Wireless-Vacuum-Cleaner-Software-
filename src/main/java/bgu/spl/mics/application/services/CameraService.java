package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectedObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;

import java.util.LinkedList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectedObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService 
{
    private final Camera camera;
    private int time;
    private final CountDownLatch latch; 
    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera, CountDownLatch latch) 
    {
        super("cameraService" + camera.getID());
        this.camera = camera;
        this.time = 0;
        this.latch = latch;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectedObjectsEvents.
     */
    @Override
    protected void initialize() {
        // TODO Implement this

        subscribeBroadcast(TickBroadcast.class ,(TickBroadcast tick) ->{
        int time = tick.getTick(); 
        DetectedObjectsEvent event = camera.activateTick(time - camera.getFrequency());//returns the events of the detected objects at the tick
        if(event != null)
        {  
            if (((DetectedObject) event.getDetectedObjects().get(0)).getID().equals("ERROR"))//if the detected object id is ERROR
            {
                String description = ((DetectedObject)event.getDetectedObjects().get(0)).getDescription();
                sendBroadcast(new CrashedBroadcast("Camera" + camera.getID() + ", because of: " + description));//send a broadcast that the camera crashed
                terminate();
            }
            else {
                sendEvent(event); 
            }
        }
        int last = camera.getList().size() - 1;
        if (camera.getList().get(last).getTime() <= time)
        {
            camera.statusDown();
            sendBroadcast(new TerminatedBroadcast(((Integer)camera.getID()).toString()));
            terminate();
        }
        });

        subscribeBroadcast(TerminatedBroadcast.class ,(TerminatedBroadcast terminate) ->{
            if(terminate.getTerminatedID().equals("TimeService") )//if the terminated MS is timeService - terminate me too
            {
                camera.statusDown();
                sendBroadcast(new TerminatedBroadcast(((Integer)camera.getID()).toString()));//tell everyone that the camera terminated itself
                terminate();
            }
        });

        subscribeBroadcast(CrashedBroadcast.class ,(CrashedBroadcast crashed) ->{
            terminate();
        });

        latch.countDown();
        
    }
}
